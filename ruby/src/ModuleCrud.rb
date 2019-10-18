require_relative 'OpenSymbol'
# Define métodos y atributos de instancia de una clase persistible
module Crud
  attr_reader :id # El atributo @id es a nivel instancia (no clase)

  def save!
    validate!
    if !self.class.exists_id? id # Si no existe el id, entonces inserto un nuevo registro ...
      self.class.table.insert(self)
    else # ... y si existe el id, entonces es un update
      update!
    end
  end

  def refresh!
    # Chequeamos que el objeto haya sido persistido antes
    if self.class.exists_id? id
      # Obtenemos la versión más reciente de la instancia guardada en disco
      last_saved_instance = self.class.table.read(self.id)
      self.class.all_attr_persistibles.each do |attr|
        self.instance_variable_set(attr.named.to_attr, last_saved_instance.send(attr.named))
      end
    else
      raise ObjectNotPersistedError.new
    end
  end

  def forget!
    self.class.table.delete!(self)
    @id = nil
  end

  def validate!
    self.class.all_attr_persistibles.each do |attr| # Por cada atributo persistible ...
      # 1ero cargamos el valor por default (si corresponde)
      attr.set_default_value(self)
      # 2do validamos que el tipo del atributo coincida con el declarado
      attr.validate_type!(self)
      # 3ero, chequeamos el resto de las validaciones que posea el atributo
      attr.validations.each do |validation_name, validation_arg|
        # level.send(:to, 100, pikachu.instance_variable_get(:level))
        attr.send(validation_name, validation_arg, self)
      end
    end
    return true # Si paso todas las validaciones (no lanzó una excepción) retorno true
  end

  #private
  def update!
    forget!
    save!
  end

end

class ObjectNotPersistedError < StandardError
  def message
    "No puede enviarse el mensaje refresh! a una instancia que nunca fue persistida"
  end
end