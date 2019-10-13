class Validator
  def no_blank(activated, object)
    raise RuntimeError, "El atributo no puede ser nil o estar vacio" if activated && (object.nil? || object == "")
    self
  end

  def from(from, number)
    raise RuntimeError, "#{number} es menor que #{from}" if number < from
    self
  end

  def to(to, number)
    raise RuntimeError, "#{number} es mayor que #{to}" if number > to
    self
  end

  def validate(block, object)
    raise RuntimeError, "El atributo es rechazado por el bloque" unless object.instance_eval(&block)
    self
  end

  # Si se solicita una validacion no definida lo ignora
  def method_missing(symbol, *args, &block) self end
  def respond_to_missing?(sym, priv = false) true end
end

