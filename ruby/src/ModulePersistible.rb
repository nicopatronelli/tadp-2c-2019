require_relative 'ModuleCrud'
require_relative 'PersistablesAttributes/PrimitiveAttribute'
require_relative 'PersistablesAttributes/ComplexAttribute'
require_relative 'PersistablesAttributes/CollectionAttribute'
require_relative 'Table'
require_relative 'OpenArray'

# Una clase es persistible si extiende el modulo Persistible
module Persistible
  attr_reader :table

  def has_one(type, hash_info_attr) #OK
    prepare_to_add_attr_persistible(hash_info_attr)
    if is_primitive?(type)
      add_persistable_primitive_attr(type, hash_info_attr)
    else
      add_persistable_complex_attr(type, hash_info_attr)
    end
  end

  def has_many(type, hash_info_attr)
    prepare_to_add_attr_persistible(hash_info_attr)
    add_persistable_collection_attr(type, hash_info_attr)
  end

  def all_instances #OK
    table.all_entries
  end

  def attr_persistibles #OK
    @attr_persistibles
  end

  # Si all es true (por defecto) se retornan TODOS los atributos persistibles de la clase,
  # incluyendo los que provienen de sus ancestros (por herencia y mixines). Si es all es
  # false se retornan solo los atributos persistibles declarados en la propia clase.
  def all_attr_persistibles(all = true) #OK
    if all
      attr_persistibles_all = attr_persistibles
      # Si la superclase hace extend ModulePersistible entonces ya responde a :attr_persistibles,
      # pero puede que no tenga ningun atributo persistible (marcado con has_one), en cuyo caso
      # retornará nil, así que debemos preguntar primero. Nunca puede retornar [].
      ancesorts_list = ancestors.drop(1) # Descarto la singleton class
      i = 0
      while ancesorts_list[i].respond_to? :attr_persistibles
        if !ancesorts_list[i].attr_persistibles.nil?
          attr_persistibles_all += ancesorts_list[i].attr_persistibles.select do |attr_sup|
            !attr_persistibles_symbols.include? attr_sup.named
          end
        end
        i = i + 1
      end
      attr_persistibles_all
    else
      attr_persistibles
    end
  end

  def all_attr_persistables_simples
    all_attr_persistibles.select do |attr|
      (attr.is_a? PrimitiveAttribute) || (attr.is_a? ComplexAttribute)
    end
  end

  def all_attr_persistables_compounds
    all_attr_persistibles.select do |attr|
      attr.is_a? CollectionAttribute
    end
  end

  # find_by_<message>(condition)
  def method_missing(name, *args, &block) #OK
    if name.to_s.start_with? "find_by"
      sub_message = name.to_s.gsub("find_by_", "") # Obtenemos el nombre del mensaje que acompaña a find_by_mensaje
      condition = args[0]
      all_instances.select do |instance|
        # pikachu.send("id".to_sym) == 5 (el id que me pasan por parámetro)
        instance.send(sub_message.to_sym) == condition
      end
    else
      super
    end
  end

  # Al hacer un override de method_missing debemos hacer un override de respond_to_missing? para
  # mantener la consistencia del mensaje respond_to?
  def respond_to_missing?(method, include_private = false) #OK
    method.to_s.start_with? "find_by_" || super
  end

  # Retorna el nombre (símbolo) de cada atributo persistible de la clase
  def attr_persistibles_symbols(all=false)
    if all
      all_attr_persistibles.map do |attr|
        attr.named
      end
    else
      @attr_persistibles.map do |attr|
        attr.named
      end
    end
  end

  def exists_id?(id) #OK
    self.find_by_id(id) != []
  end

  private
  # Auxiliar methods for has_one
  def is_primitive?(type)
    type == String || type.ancestors.include?(Numeric) || type.ancestors.include?(Boolean)
  end

  def prepare_to_add_attr_persistible(hash_info_attr)
    init_persistible_if_neccesary
    attr_named = hash_info_attr[:named]
    create_accessors(attr_named)
    delete_attribute_if_already_exists(attr_named)
  end

  def create_accessors(attr_named) # OK
    attr_accessor attr_named # Creamos accessors para el atributo persistible
  end

  def delete_attribute_if_already_exists(attr_named) # OK
    if attr_persistibles_symbols.include? attr_named
      # Si ya se definio un atributo con ese nombre en la MISMA CLASE, lo eliminamos (prevalece el más reciente)
      @attr_persistibles.select! { |attr| attr.named != attr_named }
    end
  end

  # Factory methods para los distintos tipos de atributos
  def add_persistable_primitive_attr(type, hash_info_attr) # OK
    @attr_persistibles.push(PrimitiveAttribute.new(type, hash_info_attr))
  end

  def add_persistable_complex_attr(type, hash_info_attr)
    @attr_persistibles.push(ComplexAttribute.new(type, hash_info_attr))
  end

  def add_persistable_collection_attr(type, hash_info_attr)
    @attr_persistibles.push(CollectionAttribute.new(type, hash_info_attr))
  end

  # Initialization methods
  def init_persistible_if_neccesary #OK
    if @attr_persistibles.nil?
      init_persistables_attr
      include Crud # Dentro de def, self es la instancia de la clase que extiende Persistible, así que funciona
      create_table_class
    end
  end

  def init_persistables_attr # OK
    # Array de atributos persistibles (los "marcados" con has_one o has_many)
    @attr_persistibles = Array.new
    #Agregamos @id como atributo persistible
    @attr_persistibles.push(PrimitiveAttribute.new(String, {named: :id}))
  end

  def create_table_class # OK
    @table = Table.new(self) # Crea la "tabla" (json) con el nombre de la clase
  end

end