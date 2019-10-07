
#
# Boolean Type
module Boolean end
class TrueClass;  include Boolean end
class FalseClass; include Boolean end
#
#

# Para persistir composicion de objetos se persiste su ID,
# que para los objetos persistibles es un string y el resto es el mismo objeto
module Identity
  def id; self end
  def save!; self end
  def validate!; end
end

module Reachable
  def search_by_id(obj)
    [obj]
  end
end

class Object
  include Identity
  extend Reachable
end

#
#
class TADB::Table
  def update(id, _entry)
    self.delete id
    self.insert _entry
  end
end
#
#
