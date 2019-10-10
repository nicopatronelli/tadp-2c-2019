# Define métodos y atributos de instancia de una clase persistible
module Crud
  attr_reader :id # El atributo @id es a nivel instancia (no clase)

  # Si no existe el id, entonces inserto un nuevo registro
  def save!
    if self.validate!
      @id = self.class.table.insert(self) # Cuando insertamos un registro se retorna el id generado para el hash
    else
      raise "No se pudo guardar la instancia."
    end
  end

  def refresh!
    # Obtenemos la versión más reciente de la instancia guardada en disco
    last_saved_instance = self.class.table.read(self.id)
    self.class.attr_persistibles_symbols.each do |attr|
      self.instance_variable_set("@".concat(attr.to_s, ""), last_saved_instance.send(attr))
    end
  end

  def forget!
    self.class.table.delete(id)
    @id = nil
  end

  def validate!
    self.class.attr_persistibles.each do |attr| # Por cada atributo persistible ...
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
    return true
  end

  private
  def update!
    # No tocamos el id, pues justamente estamos actualizando el objeto
  end

end
