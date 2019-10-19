require_relative '../help/require_persistable_attr'

class ComplexAttribute < PersistableAttribute
  def validate_type!(an_instance) #OK
    if named != :id
      # Validamos el tipo del objeto (complejo) en sí mimso
      raise TypeValidationError.new(self, attr_value(an_instance).class) unless attr_value(an_instance).class.ancestors.include? self.type
      attr_value(an_instance).validate! # Cascadeamos la validación
    end
  end

  def save_attr!(an_instance, attr_persistibles_hash)
    # Cascadeo el insert hasta encontrar un atributo primitivo
    id_fk = type.table.insert(an_instance.instance_variable_get(named.to_attr))
    attr_persistibles_hash[named] = id_fk
  end

  def load_attr(an_instance, entry)
    instance_attr_complejo = type.new
    instance_attr_complejo = instance_attr_complejo.class.find_by_id(entry[named]).first
    an_instance.instance_variable_set(named.to_attr, instance_attr_complejo)
  end

  def delete!(an_instance)
    sub_instance = an_instance.instance_variable_get(named.to_attr)
    sub_instance.forget! # Se cascadea el delete
  end
end