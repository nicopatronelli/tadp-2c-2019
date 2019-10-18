require_relative '../ModuleValidations'
require_relative '../ValidationExceptions'
require_relative '../OpenSymbol'
require_relative '../ModuleBoolean'
require 'tadb'

class ComplexAttribute
  include Validations
  attr_reader :named, :type, :validations

  def initialize(type, hash)
    @named = hash[:named]
    # Inicialmente había considerado a la validación de tipo como una validación más, pero
    # dado que debe hacerse antes que todas las otras, para no tener que filtrarla en validate!
    # prefiero tenerla en un campo a parte. Es importante tener presente que es una validación
    # obligatoria, pues mientras todas las demás validaciones son opcionales (from, to, no_blank, etc...)
    # la de tipo es insalvable, ya que el método has_one requiere el tipo del atributo como primer
    # parámetro
    # has_one String, named: :evolution, no_blank: true
    @type = type
    @default_value = hash[:default]
    hash.delete(:named)
    hash.delete(:default)
    @validations = hash
  end

  def set_default_value(an_instance) #OK
    actual_value = an_instance.instance_variable_get(named.to_attr)
    if actual_value.nil?
      an_instance.instance_variable_set(named.to_attr, @default_value)
    end
  end

  def validate_type!(an_instance) #OK
    if named != :id # No validamos el atributo id porque es propio del ORM, el usuario no puede setearlo (no existe para él)
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
    # Tengo que hacer un join entre la tabla Charmander y Evolution por el id de evolution en charmander
    instance_attr_complejo = instance_attr_complejo.class.find_by_id(entry[named]).first
    an_instance.instance_variable_set(named.to_attr, instance_attr_complejo)
  end

  def delete!(an_instance)
    sub_instance = an_instance.instance_variable_get(named.to_attr)
    sub_instance.forget!
  end

end