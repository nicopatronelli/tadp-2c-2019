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

