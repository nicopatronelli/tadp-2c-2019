require 'tadb'
require_relative 'OpenSymbol'

class Table
  attr_reader :real_table, :table_class

  def initialize(a_class)
    @real_table = TADB::DB.table(a_class.name)
    @table_class = a_class
  end

  def insert(an_instance)
    # Me armo el hash con los atributos persistibles y después lo inserto
    attr_persistibles_hash = {}
    an_instance.class.attr_persistibles_symbols.each do |attr_sym|
      attr_persistibles_hash[attr_sym] = an_instance.instance_variable_get(attr_sym.to_attr)
    end
    real_table.insert(attr_persistibles_hash) # Retorna el id asignado al insertar
  end

  def read(id)
    table_class.find_by_id(id).first
  end

  def all_entries
    # Estamos programando genéricamente el IDE no puede autocompletar
    # los atributos de new porque no sabe que clase estamos instanciando (estamos parados en Persistible)
    # En realidad, si uso new con argumentos, estoy asumiendo que la clase persistible tiene definido
    # un método initialize, lo cuál no es necesariamente cierto.

    # Asumiendo que la clase persistible tiene constructor sin parámetros
    # Mapeo cada entrada a una instancia de la clase
    real_table.entries.map do |entry| # entry es un hash atributoPersistible-valor
      an_instance = table_class.new # Creo una nueva instancia del objeto
      # Por cada atributo persistible
      table_class.attr_persistibles_symbols.each do |attr_sym|
        an_instance.instance_variable_set(attr_sym.to_attr, entry[attr_sym])
      end
      an_instance
    end
  end

  def delete(id)
    real_table.delete(id) # Borramos el objeto de la tabla (archivo JSON)
  end

  def clear
    real_table.clear
  end
end
