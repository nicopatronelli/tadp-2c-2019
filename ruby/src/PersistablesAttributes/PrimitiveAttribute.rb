require_relative '../ModuleValidations'
require_relative '../ValidationExceptions'
require_relative '../OpenSymbol'
require_relative '../ModuleBoolean'
require 'tadb'

class PrimitiveAttribute
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
      raise TypeValidationError.new(self, attr_value(an_instance).class) unless attr_value(an_instance).class.ancestors.include? self.type
    end
  end

  # Se llama en table.insert
  def save_attr!(an_instance, attr_persistibles_hash)
    attr_persistibles_hash[named] = an_instance.instance_variable_get(named.to_attr)
  end

  # Se llama en table.all_entries
  def load_attr(an_instance, entry)
    an_instance.instance_variable_set(named.to_attr, entry[named])
  end

  def delete!(an_instance)
    # Do nothing: Los atributos primitivos estan en el mismo registro que charmander, así que se borran
    # automáticamente al borrar charmander (I), no necesito darles un tratamiento especial
    # ni cascadear el borrado
  end

end