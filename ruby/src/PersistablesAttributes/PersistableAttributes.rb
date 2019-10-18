require_relative 'PrimitiveAttribute'
require_relative 'ComplexAttribute'
require_relative 'CollectionAttribute'

class PersistableAttributes

  def initialize
    @persistable_attrs = Array.new
  end

  def get
    @persistable_attrs
  end

  def add(attr)
    @persistable_attrs.push(attr)
  end

  def all_attr_persistibles(a_class)
    attr_persistibles_all = get
    # Si la superclase hace extend ModulePersistible entonces ya responde a :attr_persistibles,
    # pero puede que no tenga ningun atributo persistible (marcado con has_one), en cuyo caso
    # retornará nil, así que debemos preguntar primero. Nunca puede retornar [].
    ancesorts_list = a_class.ancestors.drop(1) # Descarto la singleton class
    i = 0
    while ancesorts_list[i].respond_to? :attr_persistibles
      if !ancesorts_list[i].attr_persistibles.nil?
        attr_persistibles_all += ancesorts_list[i].attr_persistibles.select do |attr_sup|
          !persistables_attr_symbols.include? attr_sup.named
        end
      end
      i = i + 1
    end
    attr_persistibles_all
  end

  def persistables_attr_symbols(a_class)
      all_attr_persistibles(a_class).map do |attr|
        attr.named
    end
  end

  def all_attr_persistables_simples(a_class)
    all_attr_persistibles(a_class).select do |attr|
      (attr.is_a? PrimitiveAttribute) || (attr.is_a? ComplexAttribute)
    end
  end

  def all_attr_persistables_compounds(a_class)
    all_attr_persistibles(a_class).select do |attr|
      attr.is_a? CollectionAttribute
    end
  end

end
