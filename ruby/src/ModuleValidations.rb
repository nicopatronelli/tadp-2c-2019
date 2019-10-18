require_relative 'OpenSymbol'

# Para un atributo primitivo, valen todas las validaciones.
# Para un atributo complejo, solo validate (además de default, que no es una validacion)
# Para un atributo compuesto (de has_many), solo validate (además de default, que no es una validacion)
# Nota: Para atributo complejo y compuesto también podría valer no_blank
module Validations
  # Dentro de los métodos, self es la instancia de la clase que hace include de Validations:
  # en nuestro caso, es precisamente un atributo persistible (instancia de AtributoPersistible)
  private

  def attr_value(an_instance) # Método auxiliar
    #an_instance.instance_variable_get("@" + self.named.to_s)
    an_instance.instance_variable_get(named.to_attr)
  end

  def from(val_arg, an_instance)
    raise FromValidationError.new(self.named, val_arg, attr_value(an_instance)) unless attr_value(an_instance) >= val_arg
  end

  def to(val_arg, an_instance)
    raise ToValidationError.new(self.named, val_arg, attr_value(an_instance)) unless attr_value(an_instance) <= val_arg
  end

  def no_blank(val_arg, an_instance)
    if val_arg
      # Dado que no permito persistir valores en nil (hago un chequeo en validate_type como 1er validación) no pregunto por nil, solo por ""
      raise NoBlankValidationError.new(self.named, val_arg, attr_value(an_instance)) unless !(attr_value(an_instance) == "")
    end
  end

  def not_equal_to(val_arg, an_instance)
    raise NotEqualToValidationError.new(self.named, val_arg, attr_value(an_instance)) unless val_arg != attr_value(an_instance)
  end

  def validate(val_arg, an_instance)
    raise ValidateBlockError.new(self.named, val_arg, attr_value(an_instance)) unless an_instance.instance_eval(&val_arg)
  end
end

