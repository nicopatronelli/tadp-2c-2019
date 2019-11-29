package TADPQuest

//trait Tarea extends (Heroe => Heroe)  { // Interfaz Tarea
trait Tarea   { // Interfaz Tarea
  def facilidad(heroe: Heroe, equipo: Equipo): Int
  // Me devuelve un nuevo heroe con los efectos producidos por realizar la tarea
  def serRealizadaPor(heroe: Heroe): Heroe
}

/*** TAREAS (DOMINIO) ***/

case class PelearContraMonstruo(hpAReducir: Int) extends Tarea {
  override def facilidad(heroe: Heroe, equipo: Equipo): Int = {
    heroe.trabajo match {
      case Some(Guerrero) if equipo.lider().contains(heroe) => 20
      case _ => 10
    }
  }

  override def serRealizadaPor(heroe: Heroe): Heroe = {
    println(s"La tarea RescatarPrincesa fue realizada por $heroe")
    heroe.stats.fuerza match {
      case valorDeFuerza if valorDeFuerza < 20 =>
        val nuevosStats = heroe.baseStats.copy(hp = heroe.baseStats.hp - hpAReducir)
        heroe.copy(baseStats = nuevosStats)
      case _ => heroe
    }
  }
}

case object ForzarPuerta extends Tarea {
  override def facilidad(heroe: Heroe, equipo: Equipo): Int = {
    val cantidadDeLadrones = equipo.integrantes.map{ _.trabajo}.map{
      case Some(Ladron) => 1
      case _ => 0
    }.sum
    heroe.stats.inteligencia + 10 * cantidadDeLadrones
  }

  override def serRealizadaPor(heroe: Heroe): Heroe = {
    heroe.trabajo match {
      case Some(Mago) | Some(Ladron) => heroe
      case _ =>
        val nuevosStats = heroe.baseStats.copy(hp = heroe.baseStats.hp - 5, fuerza = heroe.baseStats.fuerza + 1)
        heroe.copy(baseStats = nuevosStats)
    }
  }
}

case class RobarTalisman(talisman: Talisman) extends Tarea {
  override def facilidad(heroe: Heroe, equipo: Equipo): Int = {
    equipo.lider() match {
      case Some(Heroe(_,Some(Ladron),_)) => heroe.stats.velocidad
      case _ => throw NoSePuedeRealizarTareaException(this)
    }
  }

  override def serRealizadaPor(heroe: Heroe): Heroe = {
    val nuevoInventario: Inventario = heroe.inventario.agregarItem(talisman)
    heroe.copy(inventario = nuevoInventario)
  }
}

// Agrego una tarea nueva para probar la extensibilidad de la solución
case object RescatarPrincesa extends Tarea {
  override def facilidad(heroe: Heroe, equipo: Equipo): Int = {
    heroe.trabajo match {
      case Some(Guerrero) if heroe.fuerza > 100 => 50
      case Some(Ladron) if heroe.velocidad > 70 => 30
      case _ => equipo.lider().map(_.inteligencia).getOrElse(1) max 20
    }
  }

  override def serRealizadaPor(heroe: Heroe): Heroe = {
    println(s"La tarea RescatarPrincesa fue realizada por $heroe")
    val nuevosStats = heroe.baseStats.copy(
      hp = heroe.baseStats.hp + 150,
      inteligencia = heroe.baseStats.inteligencia * 2)
    heroe.copy(baseStats = nuevosStats)
  }
}

/***
 *  Opción con pattern matching
  */
object Tareas {

  def facilidad(tarea: Tarea, heroe: Heroe, equipo: Equipo): Int ={
    tarea match {
      case PelearContraMonstruo(_) => heroe.trabajo match {
        case Some(Guerrero) if equipo.lider().contains(heroe) => 20
        case _ => 10
      }
      case ForzarPuerta => {
        val cantidadDeLadrones = equipo.integrantes.map{ _.trabajo}.map{
          case Some(Ladron) => 1
          case _ => 0
        }.sum
        heroe.stats.inteligencia + 10 * cantidadDeLadrones
      }
      case RobarTalisman(_) => {
        equipo.lider() match {
          case Some(Heroe(_,Some(Ladron),_)) => heroe.stats.velocidad
          case _ => throw NoSePuedeRealizarTareaException(tarea)
        }
      }
      case RescatarPrincesa => {
        heroe.trabajo match {
          case Some(Guerrero) if heroe.fuerza > 100 => 50
          case Some(Ladron) if heroe.velocidad > 70 => 30
          case _ => equipo.lider().map(_.inteligencia).getOrElse(1) max 20
        }
      }
    }
  }

  def serRealizadaPor(tarea: Tarea, heroe: Heroe): Heroe = {
    tarea match {
      case PelearContraMonstruo(hpAReducir) => {
        heroe.fuerza match {
          case valorDeFuerza if valorDeFuerza < 20 =>
            val nuevosStats = heroe.baseStats.copy(hp = heroe.baseStats.hp - hpAReducir)
            heroe.copy(baseStats = nuevosStats)
          case _ => heroe
        }
      }
      case ForzarPuerta => {
        heroe.trabajo match {
          case Some(Mago) | Some(Ladron) => heroe
          case _ =>
            val nuevosStats = heroe.baseStats.copy(hp = heroe.baseStats.hp - 5, fuerza = heroe.baseStats.fuerza + 1)
            heroe.copy(baseStats = nuevosStats)
        }
      }
      case RobarTalisman(talisman) => {
        val nuevoInventario: Inventario = heroe.inventario.agregarItem(talisman)
        heroe.copy(inventario = nuevoInventario)
      }
      case RescatarPrincesa => {
        val nuevosStats = heroe.baseStats.copy(
          hp = heroe.baseStats.hp + 150,
          inteligencia = heroe.baseStats.inteligencia * 2)
        heroe.copy(baseStats = nuevosStats)
      }
    }
  }

}