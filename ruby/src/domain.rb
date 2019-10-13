require_relative '../src/persistent'

#
# DOMAIN
#

class College
  include Persistent

  has_one String, named: :name, default: "MIT"
end

class Grade
  include Persistent

  has_one Numeric, named: :value, no_blank: :true, from: 1, to: 10
end

module Person
  extend PersistentModule

  has_one String, named: :fullname, no_blank: :true
end

class Student
  include Person
  include Persistent

  has_one Grade, named: :grade
  has_one College, named: :college, default: College.new, validate: proc{ name.length > 2 }
  has_many Grade, named: :grades, no_blank: :false, validate: proc{ value > 1 }, default: []
end

class AssistantProfessor < Student
  has_one String, named: :type
end

