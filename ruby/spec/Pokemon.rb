require_relative '../src/tadp'

class Boolean < Object
#  attr_accessor :value

#  def initialize(value)
#    @value = value
#  end
end

class Pokemon
  extend Persistible # extend para traer los métodos de clase has_one y attr_persistibles
  has_one Integer, named: :level
  has_one String, named: :evolution
  has_one Boolean, named: :wild
  attr_accessor :type # El tipo de Pokemon (ej: "Fuego") -> Este atributo NO es persistible

  def self.builder(level, evolution, type, wild)
    p = Pokemon.new
    p.level = level
    p.evolution = evolution
    p.type = type
    p.wild = wild
    return p
  end

  def ==(other_pokemon)
    id == other_pokemon.id &&
    level == other_pokemon.level &&
    evolution == other_pokemon.evolution &&
    wild == other_pokemon.wild
  end
end
