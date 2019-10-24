require_relative 'persistent_object'
require_relative 'persistent_module'
require_relative 'persistables_attributes/types/PrimitiveAttribute'

module Persistent
  # Como hace un include Persistent al usarlo los metodos que se agreguen son de clase
  def self.included(base)
    base.include PersistentObject # (Crud) incluye los metodos del module como metodos de instancia
    base.extend Persistible
  end
end
