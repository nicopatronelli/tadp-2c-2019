require_relative '../src/ModulePersistible'

class Pikachu
  extend Persistible
  has_one Integer, named: :level, from: 10 , to: 100, validate: proc{level * 2 > 25}, default: 15 #, not_equal_to: 20
  has_one String, named: :evolution, no_blank: true
  has_one Boolean, named: :wild
  attr_accessor :type # Este atributo NO es persistible

  def self.build_ash_pikachu
    pikachu = Pikachu.new
    pikachu.level = 25
    pikachu.evolution = "Raichu"
    pikachu.wild = false
    pikachu.type = "Electric"
    pikachu
  end

  def self.build_wild_pikachu
    pikachu = Pikachu.new
    pikachu.level = 15
    pikachu.evolution = "Raichu"
    pikachu.wild = true
    pikachu.type = "Electric"
    pikachu
  end

end

class Bulbasaur
  extend Persistible
  has_one Integer, named: :level
  has_one String, named: :level
end

class Squartle
  extend Persistible
  has_one Integer, named: :level
end

class Evolution
  extend Persistible
  has_one String, named: :value
end

class Charmander
  extend Persistible
  has_one Evolution, named: :evolution
end

class Attack
  extend Persistible
  has_one String, named: :name
end

class Pidgeotto
  extend Persistible
  has_many Attack, named: :attacks, intermediate_table_name: :Pidgeotto_Attacks
end

class Psyduck
  extend Persistible
  has_one Integer, named: :level
  has_one Evolution, named: :evolution
  has_many Attack, named: :attacks, intermediate_table_name: :Psyduck_Attacks
  attr_accessor :type

  def self.build_a_complete_psyduck
    psyduck = Psyduck.new
    psyduck.level = 20
    evo = Evolution.new
    evo.value = "Golduck"
    psyduck.evolution = evo
    coletazo = Attack.new
    coletazo.name = "Coletazo"
    hipnosis = Attack.new
    hipnosis.name = "Hipnosis"
    psyduck.attacks = [coletazo, hipnosis]
    psyduck.type = "Agua y Psiquico"
    psyduck
  end
end

class Pokemon
  extend Persistible
  has_one Integer, named: :level, from: 10 , to: 100, validate: proc{level * 2 > 25}, not_equal_to: 20
  has_one String, named: :evolution, no_blank: true
  has_one Boolean, named: :wild
  attr_accessor :type # Este atributo NO es persistible

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
    type == other_pokemon.type
  end
end

class Abra
  extend Persistible
  has_one String, named: :evolution, no_blank: false # No se chequea si evolution es ""
  has_one Integer, named: :level

  def self.build(evolution, level)
    abra = Abra.new
    abra.evolution = evolution
    abra.level = level
    abra
  end
end