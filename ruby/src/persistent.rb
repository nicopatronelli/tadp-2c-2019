require_relative 'metamodel'
require_relative 'persistent_object'
require_relative 'persistent_module'
require_relative 'entity'

module Persistent
  def self.included(base)
    base.include PersistentObject # incluye los metodos del module como metodos de instancia
    base.extend PersistentModule
    base.extend Entity # incluye los metodos del module como metodos de clase (dentro de la singleton_class/autoclase)
  end
end
