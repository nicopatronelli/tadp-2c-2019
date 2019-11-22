import TADPQuest._
import org.scalatest.FlatSpec

class SeleccionDeHeroeTests extends FlatSpec {
  def fixture = new {
    val guerrero: Heroe = Heroe(Stats(100, 150, 70, 50), Option(Guerrero), Inventario()) // Lider
    val mago: Heroe = Heroe(Stats(100, 50, 90, 120), Option(Mago), Inventario())
    val equipo: Equipo = Equipo("GuerreroYMago", List(guerrero, mago))
  }

  "Si hay un guerrero en el equipo" should "elegirlo para pelear contra un monstruo" in {
    val heroeElegido = fixture.equipo.elegirHeroePara(PelearContraMonstruo(5)).get
    assert(fixture.equipo.lider().get.equals(fixture.guerrero))
  }

  "Si el lider del equipo no es un ladrón no" should "elegirse ningún heroe para robar un talisman" in {
    val heroeElegido = fixture.equipo.elegirHeroePara(RobarTalisman(TalismanMaldito))
    assert(heroeElegido.isFailure)
  }

  "En caso de empate" should "elegirse al primer heroe de la lista de integrantes" in {
    val otroMago: Heroe = Heroe(Stats(100, 50, 90, 120), Option(Mago), Inventario())
    val equipoConOtroMago = fixture.equipo.obtenerMiembro(otroMago)
    // otroMago tiene los mismos valores que mago
    val heroeElegido = fixture.equipo.elegirHeroePara(ForzarPuerta).get
    // Se elige a magoSimple porque estaba primero en la lista
    assert(heroeElegido.equals(fixture.mago))
  }
}