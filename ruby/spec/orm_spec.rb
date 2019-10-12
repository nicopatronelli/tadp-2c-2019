require 'rspec'
require 'tadb'
require_relative '../src/domain'

describe 'orm_test' do
  
  before(:all) do; end # Do nothing
  after(:all) do; end # Do nothing

  after(:each) do
    TADB::DB.clear_all
  end

  let(:estudiante) {
    estudiante = Student.new
    estudiante.fullname = "Juan Perez"
    estudiante.grade = Grade.new
    estudiante.grade.value = 8
    estudiante.grades.push(Grade.new)
    estudiante.grades.last.value = 2
    estudiante.grades.push(Grade.new)
    estudiante.grades.last.value = 3
    estudiante
  }

  let(:estudiante_con_una_nota) {
    estudiante_con_una_nota = Student.new
    estudiante_con_una_nota.fullname = "Lionel Messi"
    estudiante_con_una_nota.grade = Grade.new
    estudiante_con_una_nota.grade.value = 5
    estudiante_con_una_nota
  }

  let(:estudiante_ayudante) {
    estudiante_ayudante = AssistantProfessor.new
    estudiante_ayudante.fullname = "Diego Maradona"
    estudiante_ayudante.grade = Grade.new
    estudiante_ayudante.grade.value = 2
    estudiante_ayudante.grades.push(Grade.new)
    estudiante_ayudante.grades.last.value = 2
    estudiante_ayudante.type = "Ayudante TP"
    estudiante_ayudante
  }

  context 'nuevos data types' do
    it 'true y false son Boolean' do
      expect(true).to be_a(Boolean)
      expect(false).to be_a(Boolean)
    end
  end

  context 'cuando descarto un objeto con forget!' do
    it 'se descarta su id y se elimina de la db' do
      estudiante.save!
      estudiante_persistido = Student.search_by_id(estudiante.id).first
      expect(estudiante.id).not_to be_nil
      expect(estudiante_persistido).not_to be_nil
      estudiante.forget!
      estudiante_persistido = Student.search_by_id(estudiante.id).first
      expect(estudiante.id).to be_nil
      expect(estudiante_persistido).to be_nil
    end
  end

  context 'cuando persisto un estudiante con su nota' do
    it 'el estudiante y la nota se persisten en sus tablas' do
      estudiante.save!
      estudiante_persistido = Student.search_by_id(estudiante.id).first
      nota_persistida = Grade.search_by_id(estudiante.grade.id).first
      expect(estudiante_persistido.id).to eq(estudiante.id)
      expect(nota_persistida.id).to eq(estudiante.grade.id)
    end

    it 'devuelve el valor de la nueva nota cuando se modifica y se le hace un refresh!' do
      estudiante.save!
      grade = estudiante.grade
      grade.value = 5
      grade.save!
      estudiante.refresh!
      expect(estudiante.grade.value).to eq(5)
    end
  end

  context 'cuando persisto un estudiante con muchas notas' do
    it 'las notas se persisten correctamente' do
      estudiante.save!
      original_grades = estudiante.grades.clone.map { |grade| grade.id }
      estudiante_persistido = Student.search_by_id(estudiante.id).first
      persisted_grades = estudiante_persistido.grades.map { |grade| grade.id }
      expect(persisted_grades).to eq(original_grades)
    end

    it 'los datos se recuperan luego de hacer refresh!' do
      estudiante.save!
      original_fullname = estudiante.fullname.clone
      original_grades = estudiante.grades.clone.map { |grade| grade.id }
      estudiante.fullname = "Otro nombre"
      estudiante.grades = []
      estudiante.refresh!
      refreshed_fullname = estudiante.fullname
      refreshed_grades = estudiante.grades.map { |grade| grade.id }
      expect(refreshed_fullname).to eq(original_fullname)
      expect(refreshed_grades).to eq(original_grades)
    end
  end

  context 'cuando persisto varios estudiantes con sus notas' do
    it 'se recuperan todas las instancias correctamente' do
      estudiantes = [estudiante, estudiante_con_una_nota, estudiante_ayudante]
      nombres = estudiantes.map { |student| student.fullname }
      notas = estudiantes.map { |student| student.grade.value }
      listas_de_notas = estudiantes.map { |student| student.grades.map { |grade| grade.value } }
      estudiantes.each { |student| student.save! }
      estudiantes_persistidos = Student.all_instances
      nombres_persistidos = estudiantes_persistidos.map { |student| student.fullname }
      notas_persistidas = estudiantes_persistidos.map { |student| student.grade.value }
      listas_de_notas_persistidas = estudiantes_persistidos.map { |student| student.grades.map { |grade| grade.value } }
      expect(nombres_persistidos).to eq(nombres)
      expect(notas_persistidas).to eq(notas)
      expect(listas_de_notas_persistidas).to match_array(listas_de_notas)
    end
  end

  context 'cuando solicito las instancias de Person' do
    it 'se recuperan todas las instancias de las clases que la implementan' do
      estudiantes = [estudiante, estudiante_con_una_nota, estudiante_ayudante]
      nombres = estudiantes.map { |student| student.fullname }
      estudiantes.each { |student| student.save! }
      nombres_persistidos = Person.all_instances.map { |student| student.fullname }
      expect(nombres_persistidos).to match_array(nombres)
    end
  end

  context 'cuando intento persistir un objeto con un atributo de tipo distinto al declarado' do
    it 'lanza una excepcion' do
      estudiante.grade = nil
      estudiante.save!
      estudiante.grade = Grade.new
      estudiante.grade.value = "string invalido"
      expect{ estudiante.save! }.to raise_error(RuntimeError)
      estudiante.grade = Grade.new
      estudiante.grade.value = 0
      estudiante.fullname = 5
      expect{ estudiante.save! }.to raise_error(RuntimeError)
    end
  end

  context 'cuando persisto un objeto con validaciones' do
    it 'lanza una excepcion si tiene atributos en blanco' do
      grade = Grade.new
      grade.value = nil
      expect{ grade.save! }.to raise_error(RuntimeError)
      estudiante.fullname = ""
      expect{ estudiante.save! }.to raise_error(RuntimeError)
    end

    it 'lanza una excepcion si un numero esta fuera del rango' do
      grade = Grade.new
      grade.value = 10
      grade.save!
      grade.value = -1
      expect{ grade.save! }.to raise_error(RuntimeError)
      grade.value = 11
      expect{ grade.save! }.to raise_error(RuntimeError)
    end

    it 'lanza una excepcion si la validacion del bloque falla' do
      college = College.new
      college.name = "BA"
      estudiante.college = college
      expect{ estudiante.save! }.to raise_error(RuntimeError)
    end

    it 'lanza una excepcion si algun elemento de la lista no cumple la validacion' do
      estudiante.save!
      estudiante.grades.last.value = 1
      expect{ estudiante.save! }.to raise_error(RuntimeError)
    end
  end

  context 'cuando persisto un objeto con un valor default' do
    it 'se persiste con el valor indicado si esta seteado en nil' do
      college = College.new
      expect(college.name).to eq("MIT")
      college.name = nil
      college.save!
      expect(college.name).to eq("MIT")
    end
  end

  end
