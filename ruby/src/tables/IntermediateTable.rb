class IntermediateTable
  attr_reader :real_table

  def initialize(name)
    @real_table = TADB::DB.table(name)
  end

  def insert(an_instance, sub_instance, id_instance, id_sub_instance)
    intermediate_hash = {}
    intermediate_hash["id_" + an_instance.class.name.downcase] = id_instance
    intermediate_hash["id_" + sub_instance.class.name.downcase] = id_sub_instance
    real_table.insert(intermediate_hash) # Retorna el id del registro insertado en la tabla intermedia
  end

  def delete!(id_intermediate)
    real_table.delete(id_intermediate)
  end

  def all_entries
    real_table.entries
  end

  def clear
    real_table.clear if real_table.entries.size > 0
  end

end
