require 'tadb'
require_relative 'metamodel'

module PersistentObject
  attr_reader :id

  def save!
    _save_has_one_fields
    _save_has_many_fields
  end

  def refresh!
    if was_persisted?
      _refresh_has_one_fields
      _refresh_has_many_fields
    else
      raise RuntimeError, "El objeto aun no fue persistido"
    end
  end

  def forget!
    self.class.table.delete self.id
    @id = nil
  end

  def validate!
    _validate_simple_types(self.class.has_one_fields)
    _validate_complex_types(self.class.has_many_fields)
  end

  def was_persisted?
    !id.nil?
  end

  private

  def _validate_simple_types(hash)
    hash.each do |variable, variable_class|
      element = self.instance_variable_get("@#{variable}") #send(variable) ##### instance varible get
      if element.class != variable_class
        raise RuntimeError, "#{variable} no es un #{variable_class}"
      end
      element.validate!
    end
  end

  def _validate_complex_types(hash)
    hash.each do |array_variable, variable_class|
      array_variable.each do |element|
        if element.class != variable_class
          raise RuntimeError, "#{array_variable} no es un #{variable_class}"
        end
        element.validate!
      end
    end
  end

  def _save_has_one_fields
    has_one_hash = Hash.new
    self.class.has_one_fields.each { |variable, _| __save_single_attribute(variable, has_one_hash) }
    __insert_row_in_table(has_one_hash)
  end

  def __save_single_attribute(attr_name, row_hash)
    value = self.instance_variable_get("@#{attr_name}")
    __save_and_push(attr_name.to_sym, value, row_hash)
  end

  def __insert_row_in_table(hash_row)
    if was_persisted?
      hash_row[:id] = self.id
      self.class.table.update(self.id, hash_row)
    else
      @id = self.class.table.insert(hash_row)
    end
  end

  def _save_has_many_fields
    self.class.has_many_fields.each { |variable, variable_class| __save_array_attribute(variable, variable_class) }
  end

  def __save_array_attribute(attr_name, type)
    array = self.instance_variable_get "@#{attr_name}"
    unless array.empty?
      array.each do |element|
        has_many_hash = Hash.new
        has_many_hash[self.class.to_s.to_sym] = self.id
        __save_and_push(attr_name.to_sym, element, has_many_hash)
        self.class.joint_table(type).insert has_many_hash
      end
    end
  end

  def __save_and_push(key, value, hash)
    value.save! # En caso de que sea un objeto persistible lo persisto
    hash[key] = value.id # Si no es un objeto persistible retorna el mismo objeto
  end

  def _refresh_has_one_fields
    self.class.has_one_fields.each { |variable, variable_class| __reset_single_attribute(variable, variable_class) }
  end

  def __reset_single_attribute(attr_name, type)
    row_hash = self.class.table.entries.find { |element| element[:id] == self.id  }
    self.instance_variable_set("@#{attr_name}", __convert_id_to_object(type, row_hash[attr_name]))
  end

  def _refresh_has_many_fields
    self.class.has_many_fields.each { |variable, items_class| __reset_array_attribute(variable, items_class) }
  end

  def __reset_array_attribute(attr_name, type)
    array_ids = self.class.joint_table(type).entries.select { |element| element[self.class.to_s.to_sym] == self.id }
    array_objects = array_ids.map { |element_id| __convert_id_to_object(type, element_id[attr_name]) }
    self.instance_variable_set("@#{attr_name}", array_objects)
  end

  def __convert_id_to_object(type, id)
    type.search_by_id(id).first # Si es un objeto persistible se busca el id en su tabla
  end

end


module PersistentModule
  @has_one_fields
  @has_many_fields

  def has_one_fields
    @has_one_fields ||= Hash.new
  end

  def has_many_fields
    @has_many_fields ||= Hash.new
  end

  def has_one(type, description)
    _set_new_attribute(description[:named], type, has_one_fields)
  end

  def has_many(type, description)
    _set_new_attribute(description[:named], type, has_many_fields)
  end

  def all_instances
    self_entries = []
    if self.is_a? Class
      self_entries = self.table.entries.map { |hash_entity| get_object_from_id(hash_entity[:id]) }
    end
    _add_descendants_instances_to self_entries
    self_entries
  end

  def find_by(function, return_value)
    all_instances.select { |object| object.send(function) == return_value }
  end

  def search_by_id(id)
    find_by(:id, id)
  end

  def method_missing(symbol, *args, &block)
    if symbol.to_s.start_with?("search_by_") && args.length == 1
      strategy = symbol.to_s.gsub("search_by_", "").to_sym # Sustituye search_by_ por ""
      find_by(strategy, args[0])
    else
      super
    end
  end

  def respond_to_missing?(sym, priv = false)
    sym.to_s.start_with?("search_by_") || super
  end

  def merge_has_one_fields(fields)
    @has_one_fields = self.has_one_fields.merge(fields)
  end

  def merge_has_many_fields(fields)
    @has_many_fields = self.has_many_fields.merge(fields)
  end

  def descendants
    ObjectSpace.each_object(Class).select { |klass| klass < self }
  end

  def table
    TADB::DB.table self
  end

  def get_object_from_id(id)
    object = self.new
    object.instance_variable_set("@id", id)
    object.refresh!
    object
  end

  private

  def _set_new_attribute(attr_name, type, attributes_hash)
    attr_symbol = attr_name.to_sym
    attr_accessor attr_symbol
    attributes_hash[attr_symbol] = type
  end

  def _add_descendants_instances_to(instances_array)
    descendants.each do |subclass|
      self_entries_ids = instances_array.map { |object| object.id }
      subclass.table.entries.each do |row|
        unless self_entries_ids.include? row[:id]
          instances_array.push(subclass.get_object_from_id(row[:id]))
        end
      end
    end
  end

end

module Entity

  # Cada vez que se incluya el mixin se va a ejecutar esto al inicio
  def self.extended(base)
    ancestors = base.ancestors.select { |ancestor| (ancestor.is_a? PersistentModule) || (ancestor.is_a? Persistent) }
    ancestors.delete(base)
    ancestors.each do |ancestor|
      base.merge_has_one_fields(ancestor.has_one_fields)
      base.merge_has_many_fields(ancestor.has_many_fields)
    end
  end

  # Sobreescribe el metodo inherited en la clase que incluya el mixin
  def inherited(subclass)
    subclass.merge_has_one_fields(self.has_one_fields)
    subclass.merge_has_many_fields(self.has_many_fields)
  end

  def joint_table(type)
    TADB::DB.table"#{self}_#{type}"
  end

end

module Persistent
  def self.included(base)
    base.include PersistentObject # incluye los metodos del module como metodos de instancia
    base.extend PersistentModule
    base.extend Entity # incluye los metodos del module como metodos de clase (dentro de la singleton_class/autoclase)
  end
end

