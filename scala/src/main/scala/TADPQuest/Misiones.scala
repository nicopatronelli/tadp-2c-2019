package TADPQuest

import scala.util.Try

case class Mision(nombre: String, tareas: List[Tarea], recompensa: Recompensa)

trait Recompensa
case class CofreDeOro(cantidadDeOro: Int) extends Recompensa
case class NuevoItem(item: Item) extends Recompensa
case class NuevoHeroe(heroe: Heroe) extends Recompensa
case class IncrementarFuerzaALosMagos(incremento: Int) extends Recompensa

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
    }
  }
}

// Opción en objetos puro
//trait Recompensa {
//  def cobrarRecompensa(equipo: Equipo, recompensa: Recompensa): Equipo
//}
//case class CofreDeOro(cantidadDeOro: Int) extends Recompensa {
//  override def cobrarRecompensa(equipo: Equipo, recompensa: Recompensa): Equipo =
//    equipo.copy(pozoComun = equipo.pozoComun + cantidadDeOro)
//}
//case class NuevoItem(item: Item) extends Recompensa {
//  override def cobrarRecompensa(equipo: Equipo, recompensa: Recompensa): Equipo =
//    equipo.obtenerItem(item)
//}
//case class NuevoHeroe(heroe: Heroe) extends Recompensa {
//  override def cobrarRecompensa(equipo: Equipo, recompensa: Recompensa): Equipo =
//    equipo.obtenerMiembro(heroe)
//}
//case class IncrementarFuerzaALosMagos(incremento: Int) extends Recompensa {
//  override def cobrarRecompensa(equipo: Equipo, recompensa: Recompensa): Equipo = {
//    val magos = equipo.integrantesQueTrabajanComo(Mago)
//    val magosMejorados =
//      magos.map(m => m.copy(baseStats = m.baseStats.copy(fuerza = m.baseStats.fuerza + incremento)))
//    val noMagos = equipo.integrantesQueNoTrabajenComo(Mago)
//    equipo.copy(integrantes = noMagos ++ magosMejorados)
//  }
//}
//
