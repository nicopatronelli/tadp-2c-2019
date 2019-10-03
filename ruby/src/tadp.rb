require 'tadb'

# Define métodos y atributos de instancia de una clase persistible
module Crud
  def id
    @id # El atributo @id es a nivel instancia (no clase)
  end

  def save!
    if false #self.class.exists_id? self.id

    else # Si no existe el id, entonces inserto un nuevo registro
      attr_persistibles_hash = {}
      self.class.attr_persistibles.each do
        |attr| attr_persistibles_hash[attr] = self.instance_variable_get("@" + attr.to_s)
      end
      # table es un método de clase que retorna el atributo de clase @@table
      id = self.class.table.insert(attr_persistibles_hash) # Cuando insertamos un registro se retorna el id generado para el hash
      @id = id # Creamos la variable de instancia (atributo) id
    end
  end

  def refresh!
    # Obtenemos la versión más reciente guardada en disco del objeto
    last_saved_instance = self.class.find_by_id(self.id).first
    self.class.attr_persistibles.each do |attr|
      self.instance_variable_set("@".concat(attr.to_s, ""), last_saved_instance.send(attr))
    end
  end

  def forget!
    self.class.table.delete(id) # Borramos el objeto de la tabla (archivo JSON)
    @id = nil
  end

  def validate!

  end

  private
  def update!
    # No tocamos el id, pues justamente estamos actualizando el objeto
  end

end

# Una clase es persistible si extiende el modulo Persistible
module Persistible

  #include Crud # ERROR: Agrega los métodos save!, etc... a la clase y no a la instancia
  @@attr_persistibles = Array.new #Atributo de clase
  #Agregamos @id como atributo persistible
  @@attr_persistibles.push(:id)
  #self.singleton_class.include Crud #Tampoco funciona

  def has_one(type, hash)
    include Crud # Dentro de def, self es la instancia de la clase que extiende Persistible, así que funciona
    attr_accessor hash[:named] # Creamos accessors para el atributo persistible
    @@attr_persistibles.push(hash[:named]) # Agregamos el atributo a un array de atributos persistibles
    # Crea la "tabla" (json) con el nombre de la clase# La clase tiene un atributo que guarda una referencia a la tabla de la clase
    # Esta línea debería ir fuera del método has_one (arriba de todo) para no ejecutarla por cada vez
    # que se llama a has_one, pero en ese contexto self es el modulo, así que self.name => Persistible.
    @@table = TADB::DB.table(self.name)
  end

  def attr_persistibles
    @@attr_persistibles
  end

  def table
    @@table
  end

  def exists_id?(id)
    self.find_by_id(id) != []
  end

  def all_instances
    # Estamos programando muy genéricamente, estamos muy en bolas, el IDE no puede autocompletar
    # los atributos de new porque no sabe que clase estamos instanciando (estamos parados en Persistible)
    # En realidad, si uso new con argumentos, estoy asumiendo que la clase persistible tiene definido
    # un método initialize, lo cuál no es necesariamente cierto.

    # Asumiendo que la clase persistible tiene con constructor sin parámetros
    # => Mapeo cada entrada a una instancia de la clase
    table.entries.map do |entry| # entry es un hash atributoPersistible-valor
      i = self.new # Creo una nueva instancia del objeto
      # Por cada atributo persistible
      attr_persistibles.each do |attr|
        i.instance_variable_set("@" + attr.to_s, entry[attr])
      end
      i
    end
  end

  # TODO: Redefinir el método que acompaña a method_missing
  def method_missing(name, *args, &block)
    if name.to_s.start_with? "find_by"
      sub_message = name.to_s.gsub("find_by_", "") # Obtenemos el nombre del mensaje que acompaña a find_by_mensaje
      condition = args[0]
      self.all_instances.select do |instance|
        # pikachu.send("id".to_s) == 5 (el id que me pasan por parámetro)
        instance.send(sub_message.to_s) == condition
      end
    else
      super
    end
  end
end

class Table
  def initialize(table_name)
    @table_name = table_name
    @real_table = TADB::DB.table(table_name)
  end

  def insertar
    @real_table.insert
  end

  def recuperar

  end
end
