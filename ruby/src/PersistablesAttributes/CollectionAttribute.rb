require_relative '../ModuleValidations'
require_relative '../ValidationExceptions'
require_relative '../OpenSymbol'
require_relative '../ModuleBoolean'
require_relative '../IntermediateTable'
require_relative '../OpenString'

class CollectionAttribute
  include Validations
  attr_reader :named, :type, :validations, :intermediate_table

  def initialize(type, hash)
    @named = hash[:named] # attacks
    @type = type # Attack
    @default_value = hash[:default]
    hash.delete(:named)
    hash.delete(:default)
    # Creamos la tabla intermedia del atributo. Estamos creando la tabla de TADB
    # pero debería ser una instancia de nuestra Table
    @intermediate_table = IntermediateTable.new(hash[:intermediate_table_name])
    hash.delete(:intermediate_table_name)
    @validations = hash
  end

  # El valor default para una coleccion (has_many) es para la colección, no para sus elementos:
  # ej: has_many Attack, named: attacks, default: [Attack.new.name("placaje"), Attack.new.name("topetazo")]
  def set_default_value(an_instance) #OK
    arr_actual_value = get_actual_value(an_instance)
    if arr_actual_value.nil?
      an_instance.instance_variable_set(named.to_attr, @default_value)
    end
  end

  # an_instance: charmander
  # Estoy parado en el atributo compuesto Attacks
  def validate_type!(an_instance) #OK
    # No hace falta chequear por id, porque un atributo compuesto nunca puede ser el id
    arr = get_actual_value(an_instance)
    arr.each do |sub_instance|
      sub_instance.validate!
    end
  end

  # Redefino validate para atributos compuestos (arrays)
  # an_instance: charmander
  def validate(val_arg, an_instance)
    arr = get_actual_value(an_instance)
    arr.each do |sub_instance|
      raise ValidateBlockError.new(self.named, val_arg, attr_value(an_instance)) unless sub_instance.instance_eval(&val_arg)
    end
  end

  def delete!(an_instance)
    arr = an_instance.instance_variable_get(named.to_attr)
    arr.each do |sub_instance|
      sub_instance.forget!
    end
    # Borramos las entradas correspondientes de la tabla intermedia
    sub_instance = arr.first
    intermediate_ids_var_name = "@__" + an_instance.class.name.downcase + "_" + sub_instance.class.name.downcase + "s__ids"
    ids_intermediate = an_instance.instance_variable_get(intermediate_ids_var_name)
    ids_intermediate.each do |id|
      intermediate_table.delete!(id)
    end
    # "Limpiamos" la variable de instancia que guarda los ids de la tabla intermedia
    #an_instance.instance_variable_set(intermediate_ids_var_name, Array.new)
  end

  def set_intermediate_ids_var(an_instance, sub_instance)
    # @__charmander_attacks__ids
    intermediate_ids_var_name = "@__" + an_instance.class.name.downcase + "_" + sub_instance.class.name.downcase + "s__ids"
    puts "El nombre de la variable de instancia para guardar los ids de la tabla intermedia es #{intermediate_ids_var_name}"
    if (an_instance.instance_variable_get(intermediate_ids_var_name)).nil?
      an_instance.instance_variable_set(intermediate_ids_var_name, Array.new)
    end
    intermediate_ids_var_name
  end

  def load_attr(an_instance, entry)
    # Tengo que hacer un join entre la tabla Charmander, Charmander_Attacks y Attack para traerme
    # solo los ataques (atributos complejos) de charmander
    # 1. Me traigo todas las entradas de la tabla intermedia Charmander_Attacks
    # intermediate_table.all_entries
    # 2. Cada entry en la tabla intermedia es un hash de la forma:
    #   {":id_charmander":2,":id_attack":5,"id":1}
    # Así que me quedo solamente con las entries cuyo id_charmander sea el de la instancia
    # actual
    entries_of_an_instance = intermediate_table.all_entries.select do |entry|
      entry[("id_" + an_instance.class.name.downcase).to_sym] == an_instance.instance_variable_get(:@id)
    end
    # 3. Me armo un array con los id de los ataques de la instancia actual (char_char)
    sub_instance_ids = entries_of_an_instance.map do |entry|
      entry[("id_" + type.name.downcase).to_sym]
    end
    # 4.
    sub_instances_arr = sub_instance_ids.map do |sub_instance_id|
      type.find_by_id(sub_instance_id).first
    end
    # N. Seteo el array de objetos ataque a la instancia charmander
    an_instance.instance_variable_set(named.to_attr, sub_instances_arr)
    ## Falta setear la variable de instancia de charmander @__charmander_attacks_ids para
    # tener la misma instancia charmander que la original
    intermediate_ids_var_name = set_intermediate_ids_var(an_instance, sub_instances_arr.first)
    puts "Los id intermedios son: #{intermediate_ids_var_name.to_s}"
    intermediate_ids = entries_of_an_instance.map do |entry|
      entry[:id]
    end
    an_instance.instance_variable_set(intermediate_ids_var_name, intermediate_ids)
  end

  private
  def get_actual_value(an_instance)
    an_instance.instance_variable_get(named.to_attr)
  end

end