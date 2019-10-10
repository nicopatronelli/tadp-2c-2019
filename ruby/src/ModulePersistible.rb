require_relative 'ModuleCrud'
require_relative 'AtributoPersistible'
require_relative 'Table'

# Una clase es persistible si extiende el modulo Persistible
module Persistible
  attr_reader :attr_persistibles, :table

  def has_one(type, hash)
    if attr_persistibles.nil?
      init_attr_persistibles
      include Crud # Dentro de def, self es la instancia de la clase que extiende Persistible, así que funciona
      create_table_class
    end
    attr_named = hash[:named]
    create_accessors(attr_named)
    delete_attribute_if_already_exists(attr_named)
    add_attr_persistible(hash, type)
  end

  def all_instances
    table.all_entries
  end

  # TODO: Redefinir el método que acompaña a method_missing
  def method_missing(name, *args, &block)
    if name.to_s.start_with? "find_by"
      sub_message = name.to_s.gsub("find_by_", "") # Obtenemos el nombre del mensaje que acompaña a find_by_mensaje
      condition = args[0]
      all_instances.select do |instance|
        # pikachu.send("id".to_s) == 5 (el id que me pasan por parámetro)
        instance.send(sub_message.to_s) == condition
      end
    else
      super
    end
  end

  # Retorna el nombre (símbolo) de cada atributo persistible de la clase
  def attr_persistibles_symbols
    attr_persistibles.map do |attr|
      attr.named
    end
  end

  def exists_id?(id)
    self.find_by_id(id) != []
  end

  private

  def add_attr_persistible(hash, type)
    attr_persistibles.push(AtributoPersistible.new(type, hash))
  end

  def delete_attribute_if_already_exists(attr_named)
    if attr_persistibles_symbols.include? attr_named
      # Si ya se definio un atributo con ese nombre, lo eliminamos (prevalece el más reciente)
      attr_persistibles.select! { |attr| attr.named != attr_named }
    end
  end

  def create_accessors(attr_named)
    # Creamos accessors para el atributo persistible
    attr_accessor attr_named
  end

  def create_table_class
    # Crea la "tabla" (json) con el nombre de la clase
    @table = Table.new(self)
  end

  def init_attr_persistibles
    # Array de atributos persistibles (los "marcados" con has_one)
    @attr_persistibles = Array.new
    #Agregamos @id como atributo persistible
    attr_persistibles.push(AtributoPersistible.new(String, {named: :id}))
  end

end

