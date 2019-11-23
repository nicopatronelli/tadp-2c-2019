import org.scalatest.FlatSpec
import TADPQuest._
import TADPQuest.Taberna._
import scala.util.Failure

class TabernaTests extends FlatSpec {
  def fixture = new {
    val mago: Heroe = Heroe(Stats(1000, 30, 90, 120), Option(Mago))
    val guerrero: Heroe = Heroe(Stats(1000, 110, 70, 50), Option(Guerrero))
    val ladron: Heroe = Heroe(Stats(1000, 10, 200, 70), Option(Ladron), Inventario()) // lider
    val equipo: Equipo = Equipo("MagoYGuerreroYLadron", List(mago, guerrero, ladron))
  }

  "Cuando el criterio es el de mayor pozo comun se" should "elegir la misionPeligrosa ya" +
    "que tiene recompensa de 1000 de oro" in {
    val misionElegida: Mision = elegirMision(fixture.equipo, (e1, e2) => e1.pozoComun > e2.pozoComun)
    assert(misionElegida.equals(misionPeligrosa))
  }

  "Cuando el criterio es el de la misión que mayor fuerza agrega a los magos se" should "elegir" +
    "la misionFuerzaParaLosMagos" in {
    val misionElegida: Mision = elegirMision(
      fixture.equipo,
      (e1, e2) =>
        e1.integrantesQueTrabajanComo(Mago)(0).fuerza > e2.integrantesQueTrabajanComo(Mago)(0).fuerza
    )
    assert(misionElegida.equals(misionFuerzaParaLosMagos))
  }

  "Entrenar a un equipo con una mision que no puede realizar" should "fallar" in {
    val guerreroLider = Heroe(Stats(100, 250, 50, 60), Option(Guerrero), Inventario())
    val equipoConGuerreroLider = fixture.equipo.obtenerMiembro(guerreroLider)
    assert(entrenar(equipoConGuerreroLider).equals(
      Failure(NoSePuedeRealizarTareaException(RobarTalisman(TalismanMaldito))))
    )
  }
}
