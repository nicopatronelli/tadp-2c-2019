require_relative '../src/orm'

#
# DOMAIN
#

class Grade
  include Persistent

  has_one Numeric, named: :value
end

module Person
  extend PersistentModule

  has_one String, named: :fullname
end

class Student
  include Person
  include Persistent

  has_one Grade, named: :grade
  has_many Grade, named: :grades

  def initialize
    self.grades = []
  end

end

class AssistantProfessor < Student
  has_one String, named: :type
end

