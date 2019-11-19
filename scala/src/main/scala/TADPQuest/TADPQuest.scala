package TADPQuest
import scala.util.{Success, Failure, Try}

case class Heroe(baseStats: Stats, trabajo: Trabajo, inventario: Inventario) {

  def stats: Stats = {
    // Modifica los stats base aplicando los modificadores con fold
    val modificadores: List[Modifier] = List(trabajo, inventario)
    modificadores.foldLeft(baseStats) { (accumulator, nextElement) => nextElement.apply(accumulator) }
  }

  def trabajo(nuevoTrabajo: Trabajo): Heroe = copy(trabajo = nuevoTrabajo)
  def statPrincipal(): Stat = trabajo.statPrincipal()
  def valorStatPrincipal(): Int = stats.valor(statPrincipal())

  def agregarItem(item: Item): Heroe = {
    // Si no cumple con la restriccion, retorno el heroe como estaba
    if ( item.cumpleRestriccion(this) ) copy(inventario = inventario.agregarItem(item))
    else this
  }

/*
  def diferenciaDeStats(otroHeroe: Heroe): Stats =
    Stats(
      stats.hp - otroHeroe.stats.hp,
      stats.fuerza - otroHeroe.stats.fuerza,
      stats.velocidad - otroHeroe.stats.velocidad,
      stats.inteligencia - otroHeroe.stats.inteligencia
    )
*/

}

case class Stats(hp: Int = 1, fuerza: Int = 1, velocidad: Int = 1, inteligencia: Int = 1) {
  require( hp > 0 , "La Stat HP no puede ser negativa" )
  require( fuerza > 0 , "La Stat Fuerza no puede ser negativa" )
  require( velocidad > 0 , "La Stat Velocidad no puede ser negativa" )
  require( inteligencia > 0 , "La Stat Inteligencia no puede ser negativa" )

  def valor(stat: Stat): Int = stat match {
    case HP => hp
    case Fuerza => fuerza
    case Velocidad => velocidad
    case Inteligencia => inteligencia
  }
}

// Enum para el Stat Principal
trait Stat
object HP extends Stat
object Fuerza extends Stat
object Velocidad extends Stat
object Inteligencia extends Stat

trait Modifier {
  // Modificador que retorna nuevos Stats
  def apply(stats: Stats): Stats
}

trait Trabajo extends Modifier {
  def statPrincipal(): Stat
}

trait Item extends Modifier {
  def cumpleRestriccion(heroe: Heroe): Boolean
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

  def apply(stats: Stats): Stats = {
    // En caso de que sea un arma de dos manos, retorna una sola
    val armas = if (manos._1 == manos._2) (manos._1, null) else manos

    // Modifica los stats base aplicando los modificadores de cada item con fold
    // Si el item es null continua iterando con el acumulador
    val items: List[Item] = List(cabeza, armadura, armas._1, armas._2)
    val mid_stats: Stats = items.foldLeft(stats) { (accumulator, nextItem) => Try(nextItem.apply(accumulator)).getOrElse(accumulator) }

    // Modifica los stats obtenidos anteriormente aplicando los modificadores de cada talisman con fold
    talismanes.foldLeft(mid_stats) { (accumulator, nextItem) => Try(nextItem.apply(accumulator)).getOrElse(accumulator) }
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

case class Equipo(nombre: String, integrantes: List[Heroe] = List(), ganancias: Int = 0) {
  type Criterio = Heroe => Int

  // Ante una lista vacia o un empate, retorna None
  def mejorHeroeSegun(criterio: Criterio): Option[Heroe] = Try(integrantes.maxBy(criterio)) match {
    case Success(heroe)
      if esMaximoUnico(criterio(heroe), integrantes.map(criterio)) => Some(heroe)
    case _ => None
  }

  def obtenerItem(nuevoItem: Item): Equipo = {
    // Pongo cada heroe en una tupla (HeroeSinItem, IncrementoStatPrincipal)
    val heroesConIncremento: List[(Heroe, Int)] = heroesConIncrementoDeStatPrincipal(nuevoItem)
    if ( algunoTieneIncrementoPositivo(heroesConIncremento) ) {
      // Me quedo con el heroe de mayor incremento
      val heroeConMaximoIncremento = heroesConIncremento.maxBy( heroeConIncremento => heroeConIncremento._2 )._1
      // Reemplazo el heroe por el mismo heroe con el item equipado
      reemplazarMiembro(heroeConMaximoIncremento, heroeConMaximoIncremento.agregarItem(nuevoItem))
    } else {
      vender(nuevoItem)
    }
  }

  def obtenerMiembro(nuevoIntegrante: Heroe): Equipo = copy(integrantes = nuevoIntegrante :: integrantes)

  def reemplazarMiembro(integranteReemplazado: Heroe, nuevoIntegrante: Heroe): Equipo =
    Try(copy(integrantes = integrantes.map {
      case integrante if integrante.equals(integranteReemplazado) => nuevoIntegrante
      case integrante => integrante
    })).getOrElse(this)

  def lider(): Option[Heroe] = {
    val criterioStatPrincipal: Criterio = _.valorStatPrincipal()
    mejorHeroeSegun(criterioStatPrincipal)
  }

  def vender(item: Item): Equipo = copy(ganancias = ganancias + item.valor())

  private def esMaximoUnico(element: Int, list: List[Int]): Boolean =
    // Saca el entero de la lista y se fija si el maximo de la lista es menor al elemento extraido
    list.diff(List(element)).max < element

  private def algunoTieneIncrementoPositivo(heroesConIncremento: List[(Heroe, Int)]): Boolean =
    heroesConIncremento.exists( incremento => incremento._2 > 0)

  private def heroesConIncrementoDeStatPrincipal(nuevoItem: Item): List[(Heroe, Int)] = {
    // Pongo cada heroe en una tupla (HeroeSinItem, HeroeConItem)
    val integrantesConNuevoItem: List[(Heroe, Heroe)] =
      integrantes.map( integrante => (integrante, integrante.agregarItem(nuevoItem)) )

    // Pongo cada heroe en una tupla (HeroeSinItem, IncrementoStatPrincipal)
    integrantesConNuevoItem.map( integrante =>
      (integrante._1, integrante._2.valorStatPrincipal() - integrante._1.valorStatPrincipal()) )
  }

}

