package TADPQuest

class Heroe(var baseStats: Stats, var trabajo: Trabajo, var inventario: Inventario) {
  def stats: Stats = ???
  def agregarItem(item: Item): Nothing = {
    inventario.agregarItem(item)
  }
  println("asd")
}

case class Stats(var hp: Int = 1, var fuerza: Int = 1, var velocidad: Int = 1, var inteligencia: Int = 1)

trait Trabajo {
  def apply(stats: Stats): Stats
}

class Guerrero extends Trabajo {
  override def apply(stats: Stats): Stats = ???
}

class Inventario(var cabeza: Casco, var armadura: Armadura, var arma: Arma, var talismanes: List[Talisman]) {
  def agregarItem(item: Item): Nothing = ???
}

trait Item {
  def apply(stats: Stats): Stats
  def cumpleRestriccion(heroe: Heroe): Boolean
}

class Casco extends Item {
  override def apply(stats: Stats): Stats = ???
  override def cumpleRestriccion(heroe: Heroe): Boolean = ???
}

class Armadura extends Item {
  override def apply(stats: Stats): Stats = ???
  override def cumpleRestriccion(heroe: Heroe): Boolean = ???
}

class Arma extends Item {
  override def apply(stats: Stats): Stats = ???
  override def cumpleRestriccion(heroe: Heroe): Boolean = ???
}

class Talisman extends Item {
  override def apply(stats: Stats): Stats = ???
  override def cumpleRestriccion(heroe: Heroe): Boolean = ???
}

