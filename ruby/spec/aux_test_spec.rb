describe "Persistible.exists_id?" do
  it "exists_id? :id" do
    pikachu = Pokemon.builder(25, "Raichu", "Eléctrico", false)
    pikachu.save!
    expect(Pokemon.exists_id? pikachu.id).to eq true
  end
end
