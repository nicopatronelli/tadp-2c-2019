import TADPQuest.Taberna._
import TADPQuest._
import org.scalatest.FlatSpec

import scala.util.Failure

class TabernaTests extends FlatSpec {
  def fixture = new {
    val mago: Heroe = Heroe(Stats(1000, 30, 90, 120), Option(Mago))
    val guerrero: Heroe = Heroe(Stats(1000, 110, 70, 50), Option(Guerrero))
    val ladron: Heroe = Heroe(Stats(1000, 10, 200, 70), Option(Ladron), Inventario()) // lider
    val equipo: Equipo = Equipo("MagoYGuerreroYLadron", List(mago, guerrero, ladron))
  }

  "Cuando el criterio es el de mayor pozo comun se" should "elegir la misionPeligrosa ya" +
    "que tiene recompensa de 1000 de oro (es la que mas oro da del tablon)" in {
    val misionElegida: Option[Mision] = fixture.equipo.elegirMision((e1, e2) => e1.pozoComun > e2.pozoComun, tablon)
    assert(misionElegida.get.equals(misionPeligrosa))
  }

  "Cuando el criterio es el de la misión que mayor fuerza agrega a los magos se" should "elegir" +
    "la misionFuerzaParaLosMagos" in {
    val misionElegida: Option[Mision] = fixture.equipo.elegirMision(
      (e1, e2) =>
        e1.integrantesQueTrabajanComo(Mago)(0).fuerza > e2.integrantesQueTrabajanComo(Mago)(0).fuerza,
      tablon
    )
    assert(misionElegida.get.equals(misionFuerzaParaLosMagos))
  }

  "Si el entrenamiento es exitoso se" should "acumular las recompensas cobradas" in {
    val guerreroLider = Heroe(Stats(1000, 250, 50, 60), Option(Guerrero), Inventario())
    val equipoConGuerreroLider = fixture.equipo.obtenerMiembro(guerreroLider)
    val equipoEntrenado = equipoConGuerreroLider.entrenar(
      (e1, e2) => e1.pozoComun > e2.pozoComun,
      List(misionPeligrosa, misionLarga, misionMasPeligrosa))
    // misionPeligrosa da 1000 de oro y misionMasPeligrosa da 2000 de oro
    // misionLarga agrega un nuevo miembro, no da oro => 3000 de oro después de entrenar
    assert(equipoEntrenado.get.pozoComun.equals(3000))
  }

  "Si el entrenamiento incluye una mision que el equipo no puede realizar, el entrenamiento" should "fallar" in {
    val guerreroLider = Heroe(Stats(1000, 250, 50, 60), Option(Guerrero), Inventario())
    val equipoConGuerreroLider = fixture.equipo.obtenerMiembro(guerreroLider)
    val equipoEntrenado = equipoConGuerreroLider.entrenar(
      (e1, e2) => e1.pozoComun > e2.pozoComun,
      tablon)
    assert(equipoEntrenado.equals(
        Failure(
          NoSePuedeRealizarTareaException(RobarTalisman(TalismanMaldito)))
      )
    )
  }

  "Si el tablon de misiones está vacío, no" should "romper, sino devolver None" in {
    val tablonVacio: List[Mision] = List()
    val misionElegida = fixture.equipo.elegirMision((e1, e2) => e1.pozoComun > e2.pozoComun, tablonVacio)
    assert(misionElegida.equals(None))
  }

  "El equipo" should "ordenar las misiones de acuerdo al criterio elegido" in {
    val miTablon = List(misionPeligrosa, misionLarga, misionPeligrosa, misionMasPeligrosa, misionMasPeligrosa)
    val misionesOrdenadas = fixture.equipo.ordenarMisiones((e1, e2) => e1.pozoComun > e2.pozoComun, miTablon)
    val ordenEsperado = List(misionMasPeligrosa, misionMasPeligrosa, misionPeligrosa, misionPeligrosa, misionLarga)
    assert(misionesOrdenadas.equals(ordenEsperado))
//    println("El tablon original es: " + miTablon.map(_.nombre))
//    println("Las misiones ordenadas quedan: " + misionesOrdenadas.map(_.nombre))
  }

  "Si no hay misiones que ordenar, se" should "dsad" in {
    val miTablon = List()
    val misionesOrdenadas = fixture.equipo.ordenarMisiones((e1, e2) => e1.pozoComun > e2.pozoComun, miTablon)
    println("Las misiones ordenadas quedaron: " + misionesOrdenadas)
    //assert(misionesOrdenadas.equals(None))
    //    println("El tablon original es: " + miTablon.map(_.nombre))
    //    println("Las misiones ordenadas quedan: " + misionesOrdenadas.map(_.nombre))
  }

  "Entrenar con lista vacia" should "dsad" in {
    val miTablon = List()
    val equipoEntrenado = fixture.equipo.entrenar((e1, e2) => e1.pozoComun > e2.pozoComun, miTablon)
    println("Las misiones ordenadas quedaron: " + equipoEntrenado)
    //assert(misionesOrdenadas.equals(None))
    //    println("El tablon original es: " + miTablon.map(_.nombre))
    //    println("Las misiones ordenadas quedan: " + misionesOrdenadas.map(_.nombre))
  }

}
