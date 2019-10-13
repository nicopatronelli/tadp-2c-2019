module Entity

  # Cada vez que se incluya el mixin se va a ejecutar esto al inicio
  def self.extended(base)
    ancestors = base.ancestors.select { |ancestor| (ancestor.is_a? PersistentModule) || (ancestor.is_a? Persistent) }
    ancestors.delete(base)
    ancestors.each { |ancestor| base.merge_attr(ancestor, base) }
  end

  # Sobreescribe el metodo inherited en la clase que incluya el mixin
  def inherited(subclass)
    self.merge_attr(self, subclass)
  end

  def merge_attr(from, to)
    to.merge_fields_with_attr("has_one_fields", from.has_one_fields)
    to.merge_fields_with_attr("has_many_fields", from.has_many_fields)
    to.merge_fields_with_attr("validations", from.validations)
    to.merge_fields_with_attr("defaults", from.defaults)
  end

end
