import org.scalatest.{FlatSpec, FunSpec}
import scala.util.{Success, Failure, Try}
import TADPQuest._

class ProjectSpec extends FlatSpec {

  def fixture = new {
    val baseStats: Stats = Stats(100,100,100,100)

    val guerreroSimple: Heroe = Heroe(baseStats, Guerrero, Inventario())
    val guerreroConCascoYArmadura: Heroe = Heroe(baseStats, Guerrero,
      Inventario(cabeza = CascoVikingo, armadura = ArmaduraEleganteSport))
    val ladronSimple: Heroe = Heroe(baseStats, Ladron, Inventario())

    val equipo: Equipo = Equipo("Los Patos Salvajes", List(
      guerreroSimple,
      guerreroConCascoYArmadura,
      ladronSimple
    ))
  }

  "Un guerrero simple" should "modificar sus stats finales solo por el trabajo" in {
    val guerreroSimpleStats = fixture.guerreroSimple.stats
    val expectedStats = Guerrero.recalcularStats(fixture.baseStats)
    assert(guerreroSimpleStats.equals(expectedStats))
  }

  "Un guerrero simple" should "poder cambiar de trabajo y obtener nuevas stats finales" in {
    val magoSimple = fixture.guerreroSimple.cambiarTrabajo(Mago)
    val magoSimpleStats = magoSimple.stats
    val expectedStats = Mago.recalcularStats(fixture.baseStats)
    assert(magoSimpleStats.equals(expectedStats))
  }

  "Un guerrero simple" should "poder agregar nuevos items y modificar sus stats finales" in {
    val guerreroConItems = fixture.guerreroSimple
      .agregarItem(CascoVikingo)
      .agregarItem(ArmaduraEleganteSport)
      .agregarItem(ArcoViejo)
    val actualStats = guerreroConItems.stats
    val expectedStats = Stats(90, 117, 130, 90)
    assert(actualStats.equals(expectedStats))
  }

  "Un arma de dos manos" should "ser la unica arma del guerrero" in {
    val guerreroConDosArmas = fixture.guerreroSimple
      .agregarItem(EspadaDeLaVida)
      .agregarItem(EscudoAntiRobo)
    assert( guerreroConDosArmas.inventario.manos.equals( (EspadaDeLaVida, EscudoAntiRobo) ) )

    val guerreroConUnArmaDeDosManos = guerreroConDosArmas.agregarItem(ArcoViejo)
    assert( guerreroConUnArmaDeDosManos.inventario.manos.equals( (ArcoViejo, ArcoViejo) ) )

    val guerreroConUnArma = guerreroConUnArmaDeDosManos.agregarItem(EspadaDeLaVida)
    assert( guerreroConUnArma.inventario.manos.equals( (EspadaDeLaVida, null) ) )
  }

  "Un item que no cumple las restricciones" should "no ser agregado al inventario del heroe" in {
    val guerreroSinArmas = fixture.guerreroSimple
      .agregarItem(PalitoMagico)
      .agregarItem(EspadaDeLaVida)
    assert( guerreroSinArmas.inventario.manos.equals( (EspadaDeLaVida, null) ) )
  }

  "El criterio de mas veloz" should "devolver el heroe con mayor velocidad del equipo" in {
    val criterioMasVeloz: Heroe => Int = _.stats.velocidad
    val heroeMasVeloz = fixture.equipo.mejorHeroeSegun(criterioMasVeloz).get
    assert( heroeMasVeloz.equals(fixture.guerreroConCascoYArmadura) )
  }

  "El criterio de mas veloz" should "devolver None si el equipo esta vacio" in {
    val equipoVacio = Equipo("Equipo vacio")
    val criterioMasVeloz: Heroe => Int = _.stats.velocidad
    val heroeMasVeloz = equipoVacio.mejorHeroeSegun(criterioMasVeloz)
    assert( heroeMasVeloz.isEmpty )
  }

  "Cuando se agrega un nuevo heroe al equipo" should "agregarlo a la lista de integrantes" in {
    val equipoVacio = Equipo("Equipo vacio")
    val equipoConIntegrante = equipoVacio.obtenerMiembro(fixture.guerreroSimple)
    assert( equipoConIntegrante.integrantes.equals( List(fixture.guerreroSimple) ) )
  }

  "Cuando se reemplaza un heroe por otro en un equipo" should "reemplazarlo en la lista de integrantes" in {
    val otroGuerreroSimple = fixture.guerreroSimple.copy()
    val equipoConMiembroRemplazado = fixture.equipo.reemplazarMiembro(fixture.ladronSimple, otroGuerreroSimple)
    val listaNuevosIntegrantes = List(
      fixture.guerreroSimple,
      fixture.guerreroConCascoYArmadura,
      otroGuerreroSimple
    )
    assert(equipoConMiembroRemplazado.integrantes.equals(listaNuevosIntegrantes))
  }

  "El líder de un equipo" should "ser el heroe con el mayor valor en su stat principal" in {
    val equipoConUnLider = Equipo("Equipo con un lider", List(
      fixture.ladronSimple,
      fixture.ladronSimple.copy(),
      fixture.guerreroSimple
    ))
    val lider = equipoConUnLider.lider().get
    assert(lider.equals(fixture.guerreroSimple))
  }

  "El líder de un equipo" should "ser None si el equipo no tiene integrantes o hay mas de un lider" in {
    val equipoVacio = Equipo("Equipo vacio")
    val liderEquipoVacio = equipoVacio.lider()
    assert(liderEquipoVacio.isEmpty)

    val lider = fixture.equipo.lider()
    assert(lider.isEmpty)
  }

  "Cuando un equipo obtiene un item" should "ser agregado al heroe al que le produzca el " +
    "mayor incremento en la main stat de su job" in {
    val equipoQueObtieneItem = fixture.equipo.obtenerItem(ArmaduraEleganteSport)
    val integranteQueObtieneItem = fixture.ladronSimple.agregarItem(ArmaduraEleganteSport)
    assert(equipoQueObtieneItem.integrantes.contains(integranteQueObtieneItem))
  }

  "Cuando un equipo obtiene un item" should "ser vendido si ningun heroe consigue aumentar su stat principal" in {
    val equipoQueObtieneItem = fixture.equipo.obtenerItem(PalitoMagico)
    assert(equipoQueObtieneItem.integrantes.equals(fixture.equipo.integrantes))
    assert(equipoQueObtieneItem.pozoComun.equals(1))
  }
}

class FacilidadTests extends FlatSpec {
  def fixture = new {
    val guerrero: Heroe = Heroe(Stats(100, 150, 70, 50), Guerrero, Inventario()) // Lider
    val ladron: Heroe = Heroe(Stats(100, 50, 90, 120), Ladron, Inventario())
    val otroLadron: Heroe = Heroe(Stats(100, 40, 70, 90), Ladron, Inventario())
    val equipo: Equipo = Equipo("GuerreroYDosLadrones", List(guerrero, ladron, otroLadron))
  }

  "Si el líder del equipo es un guerrero, la facilidad de éste para pelear contra " +
    "un monstruo" should "ser igual a 20" in {
    val facilidadLiderGuerrero = PelearContraMonstruo(5).facilidad(fixture.equipo.lider().get, fixture.equipo)
    assert(facilidadLiderGuerrero.equals(20))
  }

  "La facilidad para forzar una puerta " should "igual a la inteligencia del héroe más 10 por " +
    "cada ladrón en el equipo" in {
    val facilidadParaForzarPuerta = ForzarPuerta.facilidad(fixture.equipo.integrantes(0), fixture.equipo)
    // Inteligencia del guerrero + 10 por cada ladron en el equipo
    //  - El guerrero tiene 50 de inteligencia inicial pero por tener como trabajo Guerrero se reduce a 40
    //  - Como hay dos ladrones en el equipo, entonces: 40 + 10 * 2 = 60
    assert(facilidadParaForzarPuerta.equals(60))
  }

  "La facilidad para robar un talisman" should "igual a la velocidad del heroe" in {
    val soloLadrones = Equipo("Ladrones", List(fixture.ladron, fixture.otroLadron))
    val facilidadParaRobarUnTalisman = RobarTalisman(TalismanMaldito).facilidad(soloLadrones.lider().get, soloLadrones)
    assert(facilidadParaRobarUnTalisman.equals(soloLadrones.lider().get.velocidad))
  }
}

class SeleccionDeHeroeTests extends FlatSpec {
  def fixture = new {
    val guerrero: Heroe = Heroe(Stats(100, 150, 70, 50), Guerrero, Inventario()) // Lider
    val mago: Heroe = Heroe(Stats(100, 50, 90, 120), Mago, Inventario())
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
    val otroMago: Heroe = Heroe(Stats(100, 50, 90, 120), Mago, Inventario())
    val equipoConOtroMago = fixture.equipo.obtenerMiembro(otroMago)
    // otroMago tiene los mismos valores que mago
    val heroeElegido = fixture.equipo.elegirHeroePara(ForzarPuerta).get
    // Se elige a magoSimple porque estaba primero en la lista
    assert(heroeElegido.equals(fixture.mago))
  }
}

class MisionesTests extends FlatSpec {
  def fixture = new {
    val mago: Heroe = Heroe(Stats(100, 30, 90, 120), Mago) // lider
    val guerrero: Heroe = Heroe(Stats(100, 110, 70, 50), Guerrero)
    val ladron: Heroe = Heroe(Stats(100, 10, 50, 70), Ladron, Inventario())
    val equipo: Equipo = Equipo("MagoYGuerrero", List(mago, guerrero))
    val mision = Mision(
      tareas = List(PelearContraMonstruo(50), ForzarPuerta),
      recompensa = CofreDeOro(1000)
    )
  }

  "Un integrante de un equipo" should "verse afectado al realizar una misión" in {
    // Ambas tareas son realizadas por el mago:
    //  - PelearContraMonstruo le resta los 50 de hp que pasamos por parámetro
    //  - ForzarPuerta no tiene efecto contra los magos
    val equipoDespuesDeMision = fixture.equipo.realizarMision(fixture.mision)
    val hpMagoAntesDeMision = fixture.mago.stats.hp
    val hpMagoDespuesDeMision = equipoDespuesDeMision.get.integrantes(0).stats.hp
    assert(hpMagoAntesDeMision.equals(hpMagoDespuesDeMision + 50))
  }

  "Al realizar una mision exitosamente, el equipo" should "cobrar la recompensa" in {
    val pozoComunAntesDeMision = fixture.equipo.pozoComun
    val pozoComunDespuesDeMision = fixture.equipo.realizarMision(fixture.mision).get.pozoComun
    assert(pozoComunDespuesDeMision.equals(pozoComunAntesDeMision + 1000)) // La recompensa son 1000 de oro
  }

  "Si un equipo no puede realizar una tarea de la misión, la misión entera" should "fallar" in {
    // No se puede realizar la misión porque se necesita que el líder del equipo sea un ladrón para
    // robar un talismán
    val misionImposible = fixture.mision.copy(tareas = RobarTalisman(TalismanMaldito) :: fixture.mision.tareas)
    assert(fixture.equipo.realizarMision(misionImposible).equals(Failure(NoSePuedeRealizarTareaException(RobarTalisman(TalismanMaldito)))))
  }

  "Al robar un talisman" should "agregar el talisman robado a un integrante del equipo"

  "Los efectos producidos por varias tareas en una misma misión" should "acumularse" in {
    val equipoDeUno = Equipo("Solitario", List(fixture.ladron))
    val misionParaUnoSolo = Mision(
      List(PelearContraMonstruo(10), PelearContraMonstruo(20), ForzarPuerta, PelearContraMonstruo(50)),
      CofreDeOro(5000)
    )
    val equipoDeUnoPostMision = equipoDeUno.realizarMision(misionParaUnoSolo).get
    val ladronDespuesDeMision = equipoDeUnoPostMision.integrantes(0)
    // ladron arranca con 100 de hp pero se le reduce 5 por tener trabajo Ladrón -> 95
    // Pelear contra tres monstruos que le quitan 10 + 20 + 50 = 80 puntos de hp -> 95 - 80 = 15
    assert(ladronDespuesDeMision.hp.equals(15))
  }
}