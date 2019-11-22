package TADPQuest

import scala.util.Try

trait Item extends Modifier {
  def cumpleRestriccion(heroe: Heroe): Boolean = true // Por defecto, todos los items se pueden equipar
  def valor(): Int = 1 // Por defecto los items valen 1
}

trait Casco extends Item
trait Armadura extends Item
trait Arma extends Item {
  def requiereDosManos(): Boolean
}
trait Talisman extends Item

case class Inventario(
                       cabeza: Casco = null,
                       armadura: Armadura = null,
                       manos: (Arma, Arma) = (null, null),
                       talismanes: List[Talisman] = List()
                     ) extends Modifier {

  def recalcularStats(stats: Stats): Stats = {
    // En caso de que sea un arma de dos manos, retorna una sola
    val armas = if (manos._1 == manos._2) (manos._1, null) else manos

    // Modifica los stats base aplicando los modificadores de cada item con fold
    // Si el item es null continua iterando con el acumulador
    val items: List[Item] = List(cabeza, armadura, armas._1, armas._2) ++ talismanes
    items.foldLeft(stats) { (accum, item) => Try(item.recalcularStats(accum)).getOrElse(accum) }
  }

  def agregarItem(item: Item): Inventario = {
    // Asigna el item segun su tipo
    item match {
      case item: Casco => copy(cabeza = item)
      case item: Armadura => copy(armadura = item)
      case item: Arma if item.requiereDosManos() => copy(manos = (item, item))
      case item: Arma => asignarArma(item)
      case item: Talisman => copy(talismanes = item :: talismanes)
      case _ => this
    }
  }

  private def asignarArma(arma: Arma): Inventario = {
    // Asigna el arma en la mano que este libre
    manos match {
      case (null, b) => copy(manos = (arma, b))
      case (a, null) => copy(manos = (a, arma))
      case _ => copy(manos = (arma, null))
    }
  }
}

/************ ITEMS (Dominio) *************/

// --- CASCOS/SOMBREROS ---

object CascoVikingo extends Casco {
  override def recalcularStats(stats: Stats): Stats = stats.copy(hp = stats.hp + 10)
  override def cumpleRestriccion(heroe: Heroe): Boolean = heroe.baseStats.fuerza > 30
}

object VinchaDelBufaloDeAgua extends Casco {
  override def recalcularStats(stats: Stats): Stats = ???
  //override def cumpleRestriccion(heroe: Heroe): Boolean = heroe.trabajo
}

// --- VESTIDOS/ARMADURAS ---

object ArmaduraEleganteSport extends Armadura {
  override def recalcularStats(stats: Stats): Stats = stats.copy(hp = stats.hp - 30, velocidad = stats.velocidad + 30)
}

// --- ARMAS/ESCUDOS ---

object ArcoViejo extends Arma {
  override def recalcularStats(stats: Stats): Stats = stats.copy(fuerza = stats.fuerza + 2)
  override def requiereDosManos(): Boolean = true
}

object EspadaDeLaVida extends Arma {
  override def recalcularStats(stats: Stats): Stats = stats.copy(fuerza = stats.hp)
  override def requiereDosManos(): Boolean = false
}

object EscudoAntiRobo extends Arma {
  override def recalcularStats(stats: Stats): Stats = stats.copy(hp = stats.hp + 20)
  override def cumpleRestriccion(heroe: Heroe): Boolean =
    heroe.trabajo match {
      case Ladron => false
      case _ if heroe.baseStats.fuerza < 20 => false
      case _ => true
    }
  override def requiereDosManos(): Boolean = false
}

object PalitoMagico extends Arma {
  override def recalcularStats(stats: Stats): Stats = stats.copy(fuerza = stats.fuerza + 2)
  override def cumpleRestriccion(heroe: Heroe): Boolean =
    heroe.trabajo match {
      case Mago => true
      case Ladron if heroe.baseStats.inteligencia > 30 => true
      case _ => false
    }
  override def requiereDosManos(): Boolean = false
}

// --- TALISMANES ----

object TalismanDeDedicacion extends Talisman {
  override def recalcularStats(stats: Stats): Stats = Stats()
}

object TalismanDelMinimalismo extends Talisman {
  override def recalcularStats(stats: Stats): Stats = Stats()
}

object TalismanMaldito extends Talisman {
  override def recalcularStats(stats: Stats): Stats = Stats()
}