require_relative '../src/ModulePersistible'
require_relative '../src/ModuleBoolean'

# Clase Cliente (para tests)
class Pokemon
  extend Persistible # extend para traer has_one como metodo de clase
  has_one Integer, named: :level, from: 10 , to: 100, not_equal_to: 20
  has_one Float, named: :porcentaje, default: 2.5
  has_one String, named: :evolution, no_blank: true
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

  def self.build_pikachu
    pikachu = Pokemon.new
    pikachu.level = 25
    pikachu.evolution = "Raichu"
    pikachu.type = "Electrico"
    pikachu.wild = false
    return pikachu
  end

  def ==(other_pokemon)
    id == other_pokemon.id &&
    level == other_pokemon.level &&
    evolution == other_pokemon.evolution &&
    wild == other_pokemon.wild
    type == other_pokemon.type
  end
end