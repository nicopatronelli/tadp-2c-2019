package TADPQuest

trait Tarea { // Interfaz Tarea
  //def sePuedeRealizarPor(equipo: Equipo): Boolean = true // Por defecto, todas las tareas pueden realizarse
  def facilidad(heroe: Heroe, equipo: Equipo): Int
  // Me devuelve un nuevo heroe con los efectos producidos por realizar la tarea
  def serRealizadaPor(heroe: Heroe): Heroe
}

/*** TAREAS (DOMINIO) ***/

case class PelearContraMonstruo(hpAReducir: Int) extends Tarea {
  override def facilidad(heroe: Heroe, equipo: Equipo): Int = {
    heroe match {
      case Heroe(_,Some(Guerrero),_) if equipo.lider().contains(heroe) => 20
      case _ => 10
    }
  }

  override def serRealizadaPor(heroe: Heroe): Heroe = {
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

/*7
 Los heroes podrían tener un atributo que sea listado de misiones realizadas
 con exito para tener "estado". A diferencia del inventario y el trabajo, las misiones
 realizadas por un héroe no se guardan en el mismo (son externas).
 */
