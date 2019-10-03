require 'tadb'

module Crud
  def id
    @id
  end

  def save!
    table = TADB::DB.table(self.class.name) # Crea la "tabla" (json) Person
    hash = {}
    self.instance_variables.each do
    |attribute| hash[attribute] = self.instance_variable_get(attribute)
      #table.attr.to_s.gsub("@", "")
    end
    id = table.insert(hash) # Cuando insertamos un registro se retorna el id generado para el hash
    self.instance_variable_set(:@id, id) # Creamos la variable de instancia (atributo) id
  end

  def refresh!
    if self.id.nil?
      raise RuntimeError, "El objeto aun no fue persistido"
    else
      # refresh
    end
  end

  def forget!
    TADB::DB.table(self.class.name).delete self.id
    @id = nil
  end

  def validate!
    true
  end

end

module Persistible
  attr_writer :has_one_fields
  attr_writer :has_many_fields

  def has_one_fields
    @has_one_fields || (@has_one_fields = Hash.new)
  end

  def has_many_fields
    @has_many_fields || (@has_many_fields = Hash.new)
  end

  def has_one(type, hash)
    attr_accessor hash[:named]
    has_one_fields[hash[:named]] = type
  end

  def has_many(type, hash)
    attr_accessor hash[:named]
    has_many_fields[hash[:named]] = type
  end

  def all_instances

  end

end

class Person
  extend Persistible
  include Crud
  has_one String, named: :first_name
  has_one String, named: :last_name
  has_one Numeric, named: :age
  attr_accessor :city
end

#
# Boolean Type
#
module Boolean end
class TrueClass;  include Boolean end
class FalseClass; include Boolean end
#
#
