require_relative 'Pokemon'

describe "Tests TADP Metaprogramacion ORM" do
  before(:each) do
    Pokemon.table.clear
  end

  describe "has_one" do
    it "has_one genera accessors para los atributos persistibles" do
      pikachu = Pokemon.new
      pikachu.level = 25
      pikachu.evolution = "Raichu"
      expect(pikachu.level).to eq(25)
      expect(pikachu.evolution).to eq ("Raichu")
    end
  end

  describe "Clase persistible (no instancia)" do
    it "el atributo @type de la clase Pokemon no es persistible" do
      expect(Pokemon.attr_persistibles_symbols).not_to include(:type)
    end

    it "una clase persistible me sabe decir sus atributos persistibles" do
      expect(Pokemon.attr_persistibles_symbols).to eq ([:id, :level, :evolution, :wild])
    end

    it "una clase persistible NO entiende los mensajes de instancia save!, refresh! y forget!" do
      expect{Pokemon.save!}.to raise_exception(NoMethodError)
      expect{Pokemon.refresh!}.to raise_exception(NoMethodError)
      expect{Pokemon.forget!}.to raise_exception(NoMethodError)
    end
  end

  describe "Instancia de clase persistible" do
    let(:pikachu){
      Pokemon.builder(25, "Raichu", "Electrico", false) # El atributo type no es persistible
    }

    it "Una instancia de una clase persistible entiende los mensajes save!, refresh! y delete!" do
      expect([:save!, :refresh!, :forget!].all? {|msg| pikachu.respond_to? msg}).to eq true
    end

    it "save! guarda una instancia de memoria a disco" do
      expect(Pokemon.all_instances.size).to eq 0 # No hay registros en la tabla Pokemon
      pikachu.save!
      expect(Pokemon.all_instances.size).to eq 1 # Hay un registro en la tabla Pokemon
      charmander = Pokemon.builder(15, "Charmeleon", "Fuego", true)
      charmander.save!
      expect(Pokemon.all_instances.size).to eq 2 # Hay un registro en la tabla Pokemon
    end

    # TODO: Para revisar el update!
    it "save! actualiza una instancia guardada al volver a invocarlo en lugar de insertar un nuevo registro" do
      pikachu.save!
      pikachu.save!
      # El resultado en el JSON es: (Notar que hay un solo registro/fila)
      # {"id":"86548cd6-50f0-4226-90aa-6658fa931112","level":25,"evolution":"Raichu"}
      pikachu.level = 35
      pikachu.save!
      # El resultado final en el JSON es: (Se actualiza el mismo registro, no se inserta uno nuevo)
      # {"id":"86548cd6-50f0-4226-90aa-6658fa931112","level":35,"evolution":"Raichu"}
    end

    it "refresh! trae la ultima version de un objeto de disco a memoria" do
      pikachu.save!
      pikachu.evolution = "SuperRaichu"
      expect(pikachu.evolution).to eq "SuperRaichu"
      pikachu.refresh!
      # evolution debe ser "Raichu" porque hicimos pikachu.refresh! luego de cambiarlo a "SuperRaichu" y no hicimos save!
      expect(pikachu.evolution). to eq "Raichu"
      # Al hacer el refresh, no debemos "tocar" los atributos no persistibles en memoria (como type)
      expect(pikachu.type).to eq "Electrico"
    end

    it "forget! elimina la instancia persistida en disco (JSON)" do
      pikachu.save!
      expect(pikachu.id).not_to be_nil
      pikachu.forget!
      expect(pikachu.id).to eq nil
      expect(Pokemon.all_instances.size).to eq 0 # No hay registros en la tabla Pokemon
    end
  end

  describe "Clase persistible como tabla" do
    it "El nombre de la tabla asociada a una clase persistible es el nombre de la clase misma" do
      expect(Pokemon.table.table_class.name).to eq Pokemon.name
    end
  end

  describe 'Probamos all_instances de una clase persistible' do
    it 'el metodo all_instances retorna todas las instancias persistidas' do
      pikachu = Pokemon.builder(25, "Raichu", "Eléctrico", false)
      charmander = Pokemon.builder(15, "Charmeleon", "Fuego", true)
      squartle = Pokemon.builder(10, "Wartortle", "Agua", true)
      pikachu.save!
      charmander.save!
      # No guardamos a squartle, así que sólo hay dos registros en la tabla Pokemon
      expect(Pokemon.all_instances.size).to eq 2
    end

    it 'el metodo all_instances retorna objetos funcionales (instancias reales de la clase persistida)' do
      pikachu = Pokemon.builder(25, "Raichu", "Eléctrico", false)
      pikachu.save!
      pikachu_saved = Pokemon.find_by_id(pikachu.id).first
      # Puedo enviarle mensajes a pikachu_saved, el objeto recuperado de disco
      pikachu_saved.level = 35
      pikachu_saved.evolution = "SuperRaichu"
      expect(pikachu_saved.level).to eq 35
      expect(pikachu_saved.evolution).to eq "SuperRaichu"
    end
  end

  describe 'find_by' do
    it 'find_by_evolution' do
      pikachu = Pokemon.builder(25, "Raichu", "Eléctrico", false)
      charmander = Pokemon.builder(15, "Charmeleon", "Fuego", true)
      squartle = Pokemon.builder(10, "Wartortle", "Agua", true)
      pikachu.save!
      charmander.save!
      pikachu_saved = Pokemon.find_by_evolution("Raichu").first
      charmander_saved = Pokemon.find_by_evolution("Charmeleon").first
      # Seteamos a mano el atributo type porque no es persistible (no se guarda)
      pikachu_saved.type = "Eléctrico"
      charmander_saved.type = "Fuego"
      expect(pikachu).to eq pikachu_saved
      expect(charmander).to eq charmander_saved
    end
  end

  describe "Persistible.exists_id?" do
    it "exists_id? :id" do
      pikachu = Pokemon.builder(25, "Raichu", "Eléctrico", false)
      pikachu.save!
      expect(Pokemon.exists_id? pikachu.id).to eq true
    end
  end

  #### HASTA ACÁ ANDA TODO
  describe "Validaciones" do
    let(:pikachu){
      Pokemon.builder(25, "Raichu", "Electrico", false)
    }

    it 'se deben pasar todas las validaciones de todos los atributos persistibles' do
      expect(pikachu.validate!).to eq true
    end

    it 'se lanza la excepcion TypeValidationError al validar un objeto con un atributo
      cuyo tipo declarado no coincide con el tipo seteado' do
      # El atributo level esta declarado de tipo Integer y le guardamos un String
      pikachu.level = "Un nivel"
        expect{pikachu.validate!}.to raise_exception(TypeValidationError)
    end

    it 'se lanza la excepcion ToValidationError cuando un atributo con la
      validacion to tiene seteado un valor superior al permitido' do
      # has_one Integer, named: :level, from: 10 , to: 100, ...
      pikachu.level = 200 # 200 > 100 -> lanza la excepción
      expect{pikachu.validate!}.to raise_exception(ToValidationError)
      pikachu.level = 50 # 50 < 100 -> Permitido
      expect(pikachu.validate!).to eq true
    end

    it 'se lanza la excepcion FromValidationError cuando un atributo con la
      validacion from tiene seteado un valor inferior al permitido' do
      # has_one Integer, named: :level, from: 10 , to: 100, ...
      pikachu.level = 5 # 5 < 10 -> lanza la excepción
      expect{pikachu.validate!}.to raise_exception(FromValidationError)
      pikachu.level = 15 # 15 > 10 -> Permitido
      expect(pikachu.validate!).to eq true
    end

    it 'se lanza la excepcion NoBlankValidationError cuando un atributo con la
      validacion no_blank en true contiene una cadena vacia' do
      # has_one String, named: :evolution, no_blank: true
      pikachu.evolution = ""
      expect{pikachu.validate!}.to raise_exception(NoBlankValidationError)
    end

    it 'se omite la validacion no_blank en un atributo cuando se le pasa el flag en false' do
      # Abrimos Pokemon y modificamos el atributo evolution con no_blank en false
      class Pokemon
        has_one String, named: :evolution, no_blank: false
      end
      pikachu.evolution = ""
      expect(pikachu.validate!).to eq true
    end

    it 'se lanza la excepcion ValidateBlockError cuando un atributo con la
      validacion validate no satisface el bloque pasado por parametro' do
      # Abrimos Pokemon y le agregamos un atributo speed con un validate
      class Pokemon
        has_one Integer, named: :level, validate: proc{level > 20}
      end
      pikachu.level = 10 # 10 < 20 -> No cumple el bloque
      expect{pikachu.validate!}.to raise_exception(ValidateBlockError)
      pikachu.level = 25 # 25 > 20 -> Pasa el bloque
      expect(pikachu.validate!).to eq true
    end

    it 'se carga un atributo con su valor default al llamar a validate!' do
      class Pokemon
        has_one Integer, named: :level, default: 15
      end
      pikachu.level = nil
      pikachu.validate!
      expect(pikachu.level).to eq 15
    end
  end

end