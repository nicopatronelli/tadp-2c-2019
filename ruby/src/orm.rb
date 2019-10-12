require 'tadb'
require_relative 'metamodel'

module PersistentObject
  attr_reader :id

  def save!
    validate!
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
    _validate_simple_types
    _validate_complex_types
  end

  def was_persisted?
    !id.nil?
  end

  private

  def _validate_simple_types
    self.class.has_one_fields.each do |variable, variable_class|
      _check_default(variable)
      value = self.instance_variable_get("@#{variable}")
      _check_type(value, variable_class)
      self.class.validations[variable].each { |validation_method| validation_method.call(value) }
    end
  end

  def _validate_complex_types
    self.class.has_many_fields.each do |array_variable, variable_class|
      _check_default(array_variable)
      array = self.instance_variable_get("@#{array_variable}")
      array.each do |element|
        _check_type(element, variable_class)
        self.class.validations[array_variable].each { |validation_method| validation_method.call(element) }
      end
    end
  end

  def _check_default(attr_name)
    value = self.instance_variable_get("@#{attr_name}")
    self.instance_variable_set("@#{attr_name}", self.class.defaults[attr_name]) if value.nil?
  end

  def _check_type(object, klass)
    raise RuntimeError, "#{object.class}(#{object.to_s}) no es un #{klass}" unless object.nil? || object.is_a?(klass)
    object.validate!
  end

  def _save_has_one_fields
    has_one_hash = Hash.new
    self.class.has_one_fields.each { |variable, _| __save_single_attribute(variable, has_one_hash) }
    __insert_row_in_table(has_one_hash)
  end

  def _save_has_many_fields
    self.class.has_many_fields.each { |variable, variable_class| __save_array_attribute(variable, variable_class) }
  end

  def __save_single_attribute(attr_name, row_hash)
    value = self.instance_variable_get("@#{attr_name}")
    __save_and_push(attr_name.to_sym, value, row_hash)
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

  def __insert_row_in_table(hash_row)
    if was_persisted?
      hash_row[:id] = self.id
      self.class.table.update(self.id, hash_row)
    else
      @id = self.class.table.insert(hash_row)
    end
  end

  def __save_and_push(key, value, hash)
    value.save! # En caso de que sea un objeto persistible lo persisto
    hash[key] = value.id unless value.nil? # Si no es un objeto persistible retorna el mismo objeto
  end

  def _refresh_has_one_fields
    self.class.has_one_fields.each { |variable, variable_class| __reset_single_attribute(variable, variable_class) }
  end

  def _refresh_has_many_fields
    self.class.has_many_fields.each { |variable, items_class| __reset_array_attribute(variable, items_class) }
  end

  def __reset_single_attribute(attr_name, type)
    row_hash = self.class.table.entries.find { |element| element[:id] == self.id  }
    self.instance_variable_set("@#{attr_name}", __convert_id_to_object(type, row_hash[attr_name]))
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

  def has_one_fields
    @has_one_fields ||= Hash.new
  end

  def has_many_fields
    @has_many_fields ||= Hash.new
  end

  def validations
    @validations ||= Hash.new
  end

  def defaults
    @defaults ||= Hash.new
  end

  def has_one(type, description)
    _set_default_value(description[:named], description[:default])
    _set_new_attribute(description[:named], type, has_one_fields)
    _set_attribute_validations(description[:named], description)
  end

  def has_many(type, description)
    _set_default_value(description[:named], description[:default])
    _set_new_attribute(description[:named], type, has_many_fields)
    _set_attribute_validations(description[:named], description)
  end

  def all_instances
    instances = []
    if self.is_a? Class
      instances = self.table.entries.map { |hash_entity| get_object_from_id(hash_entity[:id]) }
    end
    _add_descendants_instances_to instances
    instances
  end

  def find_by(function, return_value)
    all_instances.select { |object| object.send(function) == return_value }
  end

  def search_by_id(id)
    find_by(:id, id)
  end

  def get_object_from_id(id)
    object = self.new
    object.instance_variable_set("@id", id)
    object.refresh!
    object
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

  def merge_fields_with_attr(attr_name, fields)
    merge_hash = self.send(attr_name.to_sym).merge(fields)
    self.instance_variable_set("@#{attr_name}", merge_hash)
  end

  def table
    TADB::DB.table self
  end

  def joint_table(type)
    TADB::DB.table"#{self}_#{type}"
  end

  private

  def _set_default_value(attr_name, default_value)
    defaults[attr_name] = default_value
  end

  def _set_new_attribute(attr_name, type, attributes_hash)
    attr_symbol = attr_name.to_sym
    attr_reader_with_default attr_symbol, self.defaults[attr_name]
    attr_writer attr_symbol
    attributes_hash[attr_symbol] = type
  end

  def attr_reader_with_default(attr_symbol, default_value=nil)
    self.define_method(attr_symbol) do
      self.instance_variable_get("@#{attr_symbol.to_s}") || self.instance_variable_set("@#{attr_symbol.to_s}", default_value)
    end
  end

  def _set_attribute_validations(attr_name, hash_validations)
    validations[attr_name] = []
    hash_validations.each do |constraint, value|
      proc = Validator.new.method(constraint).curry.call(value)
      validations[attr_name].push(proc)
    end
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

  def descendants
    ObjectSpace.each_object(Class).select { |klass| klass < self && !klass.to_s.start_with?("#") }
  end

end

module Entity

  # Cada vez que se incluya el mixin se va a ejecutar esto al inicio
  def self.extended(base)
    ancestors = base.ancestors.select { |ancestor| (ancestor.is_a? PersistentModule) || (ancestor.is_a? Persistent) }
    ancestors.delete(base)
    ancestors.each { |ancestor| base.merge_attr(ancestor, base) }
  end

  # Sobreescribe el metodo inherited en la clase que incluya el mixin
  def inherited(subclass)
    self.merge_attr(self, subclass)
  end

  def merge_attr(from, to)
    to.merge_fields_with_attr("has_one_fields", from.has_one_fields)
    to.merge_fields_with_attr("has_many_fields", from.has_many_fields)
    to.merge_fields_with_attr("validations", from.validations)
    to.merge_fields_with_attr("defaults", from.defaults)
  end

end

module Persistent
  def self.included(base)
    base.include PersistentObject # incluye los metodos del module como metodos de instancia
    base.extend PersistentModule
    base.extend Entity # incluye los metodos del module como metodos de clase (dentro de la singleton_class/autoclase)
  end
end

class Validator
  def no_blank(activated, object)
    raise RuntimeError, "El atributo no puede ser nil o estar vacio" if activated && (object.nil? || object == "")
    self
  end

  def from(from, number)
    raise RuntimeError, "#{number} es menor que #{from}" if number < from
    self
  end

  def to(to, number)
    raise RuntimeError, "#{number} es mayor que #{to}" if number > to
    self
  end

  def validate(block, object)
    raise RuntimeError, "El atributo es rechazado por el bloque" unless object.instance_eval(&block)
    self
  end

  # Si se solicita una validacion no definida lo ignora
  def method_missing(symbol, *args, &block) self end
  def respond_to_missing?(sym, priv = false) true end
end


