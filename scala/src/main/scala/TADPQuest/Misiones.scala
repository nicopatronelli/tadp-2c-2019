package TADPQuest

case class Mision(tareas: List[Tarea], recompensa: Recompensa) {}

trait Recompensa
case class CofreDeOro(cantidadDeOro: Int) extends Recompensa
case class NuevoItem(item: Item) extends Recompensa
case class NuevoHeroe(heroe: Heroe) extends Recompensa
case class IncrementarFuerzaALosMagos(incremento: Int) extends Recompensa
// Otra opción es recibir el criterio de aplicación por parámetro

object Recompensa {
  def cobrarRecompensa(equipo: Equipo, recompensa: Recompensa): Equipo = { // Retorna un "equipo recompensado"
    recompensa match {
      case CofreDeOro(cantidadDeOro) => equipo.copy(pozoComun = equipo.pozoComun + cantidadDeOro)
      case NuevoItem(item) => equipo.obtenerItem(item)
      case NuevoHeroe(heroe) => equipo.obtenerMiembro(heroe)
      case IncrementarFuerzaALosMagos(incremento) => {
        val magos = equipo.integrantesQueTrabajanComo(Mago)
        val magosMejorados =
          magos.map(m => m.copy(baseStats = m.baseStats.copy(fuerza = m.baseStats.fuerza + incremento)))
        val noMagos = equipo.integrantesQueNoTrabajenComo(Mago)
        equipo.copy(integrantes = noMagos ++ magosMejorados)
      }
      case _ => equipo
    }
  }
}

