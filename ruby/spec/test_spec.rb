describe "has_one" do
  let(:pikachu){
    Pokemon.new
  }

  it "has_one incluye accessors" do
    pikachu.level = 25
    pikachu.evolution = "Raichu"
    expect(pikachu.level).to eq(25)
    expect(pikachu.evolution).to eq ("Raichu")
  end

  it "has_one pisa un atributo con el mismo nombre y no hay chequeos de tipo" do

  end
end

describe "Clase persistible (no instancia)" do
  it "el atributo @type de Pokemon no es persistible" do
    expect(Pokemon.attr_persistibles).not_to include(:type)
  end

  it "una clase persistible me sabe decir sus atributos persistibles" do
    expect(Pokemon.attr_persistibles).to eq ([:id, :level, :evolution, :wild])
  end

  it "una clase persistible NO entiende los mensajes de instancia save!, refresh! y forget!" do
    expect{Pokemon.save!}.to raise_exception(NoMethodError)
    expect{Pokemon.refresh!}.to raise_exception(NoMethodError)
    expect{Pokemon.forget!}.to raise_exception(NoMethodError)
  end
end

describe "Instancia de clase persistible" do
  let(:pikachu){
    Pokemon.new
  }
  it "Una instancia de una clase persistible entiende los mensajes save!, refresh! y delete!" do
    expect([:save!, :refresh!, :forget!].all? {|msg| pikachu.respond_to? msg}).to eq true
  end
end

describe "Guardar instancias con save!" do
  it "save! actualiza una instancia guardada al volver a invocarlo en lugar de insertar un nuevo registro" do
    pikachu = Pokemon.builder(25, "Raichu", "Electrico", false) # El atributo type no es persistible
    pikachu.save!
    pikachu.save!
    # El resultado en el JSON es: (Notar que hay un solo registro/fila)
    # {"id":"86548cd6-50f0-4226-90aa-6658fa931112","level":25,"evolution":"Raichu"}
    pikachu.level = 35
    pikachu.save!
    # El resultado final en el JSON es: (Se actualiza el mismo registro, no se inserta uno nuevo)
    # {"id":"86548cd6-50f0-4226-90aa-6658fa931112","level":35,"evolution":"Raichu"}
  end
end

describe "refresh!" do
  it "refresh! trae la ultima version de un objeto de disco a memoria" do
    pikachu = Pokemon.builder(25, "Raichu", "Electrico", false) # type no es persistible
    pikachu.save!
    pikachu.evolution = "SuperRaichu"
    expect(pikachu.evolution).to eq "SuperRaichu"
    pikachu.refresh!
    # evolution debe ser "Raichu" porque hicimos pikachu.refresh! luego de cambiarlo a "SuperRaichu"
    expect(pikachu.evolution). to eq "Raichu"
    # Al hacer el refresh, no debemos "tocar" los atributos no persistibles en memoria (como type)
    expect(pikachu.type).to eq "Electrico"
  end
end


describe "forget!: Eliminar objetos persistidos" do
  let(:pikachu){
    Pokemon.builder(25, "Raichu", "Electrico", false)
  }

  it 'should ' do
    pikachu.save!
    expect(pikachu.id).not_to be_nil
    pikachu.forget!
    expect(pikachu.id).to eq nil
    # TODO: Faltaría chequear que el objeto se elimina del JSON yéndolo a buscar y mostrando que es nil
  end
end

describe "Clase persistible como tabla" do
  it "El nombre de la tabla asociada a una clase persistible es el nombre de la clase misma" do
    expect(Pokemon.table.instance_variable_get(:@name)).to eq Pokemon.name
  end
end

describe 'Obtenemos todas las instancias persistidas de una clase' do
  it 'el metodo all_instances retorna todas las instancias persistidas' do
    pikachu = Pokemon.builder(25, "Raichu", "Eléctrico", false)
    charmander = Pokemon.builder(15, "Charmeleon", "Fuego", true)
    squartle = Pokemon.builder(10, "Wartortle", "Agua", true)
    pikachu.save!
    charmander.save!
    # No guardamos a squartle, así que sólo hay dos registros en la tabla Pokemon
    expect(Pokemon.all_instances.size).to eq 2
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
    #expect(Pokemon.all_instances).to eq [pikachu_saved, charmander_saved]
  end
end

describe "Persistible.exists_id?" do
  it "exists_id? :id" do
    pikachu = Pokemon.builder(25, "Raichu", "Eléctrico", false)
    pikachu.save!
    expect(Pokemon.exists_id? pikachu.id).to eq true
  end
end