require 'tadb'
require_relative 'OpenSymbol'
require_relative 'IntermediateTable'

class Table
  attr_reader :real_table, :table_class

  def initialize(a_class)
    @real_table = TADB::DB.table(a_class.name)
    @table_class = a_class
  end

  # private
  # Inserta una instancia completa, incluyendo sus atributos primitivos y sus atributos
  # complejos, cascadeando la inserción.
  def insert_instance(an_instance)
    # Me armo el hash con los atributos persistibles SIMPLES (has_one) y después lo inserto
    attr_persistibles_hash = {}
    an_instance.class.all_attr_persistables_simples.each do |attr|
        attr.save_attr!(an_instance, attr_persistibles_hash)
      end
    id = real_table.insert(attr_persistibles_hash) # Retorna el id asignado al insertar
    an_instance.instance_variable_set(:@id, id) # Seteamos el id obtenido a la instancia
    id # Retornamos el id (se utiliza en insert)
  end

  # Inserta un atributo compuesto cascadeando
  def insert_many(an_instance, id_instance)
    an_instance.class.all_attr_persistables_compounds.each do |attr|
      arr = an_instance.instance_variable_get(attr.named.to_attr)
      intermediate_ids_var_name = attr.set_intermediate_ids_var(an_instance, arr.first)
      arr.each do |sub_instance|
        #id_sub_instance = sub_instance.class.table.insert(sub_instance) # Llamada recursiva
        id_sub_instance = sub_instance.save! # Llamada recursiva
        sub_instance.instance_variable_set(:@id, id_sub_instance) # Seteamos el id obtenido a la sub_instancia
        id_intermediate = attr.intermediate_table.insert(an_instance, sub_instance, id_instance, id_sub_instance)
        an_instance.instance_variable_set(intermediate_ids_var_name, an_instance.instance_variable_get(intermediate_ids_var_name).push(id_intermediate))
      end
    end
  end

  def insert(an_instance)
    id_instancia = insert_instance(an_instance)
    # Recién después de insertar la instancia (charmander) y obtener su id, puedo insertar en la tabla intermedia
    insert_many(an_instance, id_instancia)
    id_instancia # Retornamos el id de la instancia insertada originalmente
  end

  def read(id)
    table_class.find_by_id(id).first
  end

  def all_entries
    # Asumiendo que la clase persistible tiene constructor sin parámetros
    # Mapeo cada entrada a una instancia de la clase
    real_table.entries.map do |entry| # entry es un hash atributoPersistible-valor
      an_instance = table_class.new # Creo una nueva instancia del objeto
      # Por cada atributo persistible
      an_instance.class.all_attr_persistibles.each do |attr|
        attr.load_attr(an_instance, entry)
      end
      an_instance
    end
  end

  def delete!(an_instance) # OK
    an_instance.class.all_attr_persistibles.each do |attr|
      attr.delete!(an_instance)
    end
    real_table.delete(an_instance.instance_variable_get(:@id))
  end

  def clear # OK
    if real_table.entries.size > 0
      real_table.clear
    end
  end

  def name #OK
    table_class.name
  end

  private


end
