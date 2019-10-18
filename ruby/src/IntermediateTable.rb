class IntermediateTable
  attr_reader :real_table

  def initialize(name)
    @real_table = TADB::DB.table(name)
  end

  def insert(an_instance, sub_instance, id_instance, id_sub_instance)
    insert_intermediate_hash = {}
    insert_intermediate_hash["id_" + an_instance.class.name.downcase] = id_instance
    insert_intermediate_hash["id_" + sub_instance.class.name.downcase] = id_sub_instance
    real_table.insert(insert_intermediate_hash) # Retorna el id del registro insertado en la tabla intermedia
  end

  def delete!(id_intermediate) #OK
    real_table.delete(id_intermediate)
  end

  def all_entries
    real_table.entries
  end

  def clear
    if real_table.entries.size > 0
      real_table.clear
    end
  end

end
