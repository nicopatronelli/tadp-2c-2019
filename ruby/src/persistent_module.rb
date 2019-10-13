require 'tadb'
require_relative 'validator'

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

