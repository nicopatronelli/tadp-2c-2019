require_relative 'help/require_module_persistible'

# Metodos de clase
module Persistible # Una clase es persistible si extiende el modulo Persistible
  attr_reader :table

  def self.extended(base)
    base._create_table_class
    base._init_persistables_attr
  end

  def has_one(type, hash_info_attr)
    _prepare_to_add_attr_persistible(hash_info_attr)
    if _is_primitive?(type)
      attr_persistibles.push(PrimitiveAttribute.new(type, hash_info_attr))
    else
      attr_persistibles.push(ComplexAttribute.new(type, hash_info_attr))
    end
  end

  def has_many(type, hash_info_attr)
    _prepare_to_add_attr_persistible(hash_info_attr)
    attr_persistibles.push(CompoundAttribute.new(type, hash_info_attr))
  end

  def all_instances
    table.all_entries
  end

  def attr_persistibles
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
        if not ancesorts_list[i].attr_persistibles.nil? # Si es nil, entonces no tiene atributos persistibles
          attr_persistibles_all += ancesorts_list[i].attr_persistibles.select do |attr_ancestor|
            not attr_persistibles_symbols.include? attr_ancestor.named
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
    all_attr_persistibles.select {|attr| attr.is_a? CompoundAttribute}
  end

  # find_by_<message>(condition)
  def method_missing(name, *args, &block) #OK
    if name.to_s.start_with? "find_by"
      sub_message = name.to_s.gsub("find_by_", "") # Obtenemos el nombre del mensaje que acompaña a find_by_mensaje
      condition = args[0]
      all_instances.select {|an_instance| an_instance.send(sub_message.to_sym) == condition}
    else
      super
    end
  end

  # Al hacer un override de method_missing debemos hacer un override de respond_to_missing? para
  # mantener la consistencia del mensaje respond_to?
  def respond_to_missing?(method, include_private = false) #OK
    (method.to_s.start_with? "find_by_") || super
  end

  def attr_persistibles_symbols(all=false)
    all ? all_attr_persistibles.map {|attr| attr.named} : attr_persistibles.map {|attr| attr.named}
  end

  def exists_id?(id)
    not id.nil?
  end

  #private
  def _is_primitive?(type)
    type == String || type.ancestors.include?(Numeric) || type.ancestors.include?(Boolean)
  end

  def _prepare_to_add_attr_persistible(hash_info_attr)
    attr_named = hash_info_attr[:named]
    create_accessors(attr_named)
    delete_attribute_if_already_exists(attr_named)
  end

  def create_accessors(attr_named)
    attr_accessor attr_named
  end

  def delete_attribute_if_already_exists(attr_named)
    # Si ya se definio un atributo con ese nombre en la MISMA CLASE, lo eliminamos (prevalece el más reciente)
    if attr_persistibles_symbols.include? attr_named
      @attr_persistibles.select! { |attr| attr.named != attr_named }
    end
  end

  def _create_table_class # OK
    @table = Table.new(self) # Instancio mi clase Table (es un wrapper de Table de TADB)
  end

  def _init_persistables_attr
    @attr_persistibles = [] # Atributo de la clase persistible (no de las instancias)
    #Agregamos @id como atributo persistible
    @attr_persistibles.push(PrimitiveAttribute.new(String, {named: :id}))
  end
end