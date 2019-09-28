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

  end

  def forget!

  end

end

module Persistible
  def has_one(type, hash)
    attr_accessor hash[:named]
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
