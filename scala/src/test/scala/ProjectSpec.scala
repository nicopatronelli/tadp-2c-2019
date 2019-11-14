import org.scalatest.FlatSpec
import TADPQuest._
import RPG._

class ProjectSpec extends FlatSpec {

  def fixture = new {
    val baseStats: Stats = Stats(100,100,100,100)
    val guerreroSimple: Heroe = Heroe(baseStats, Guerrero, Inventario())
  }

  "Un guerrero simple" should "modificar sus stats finales solo por el trabajo" in {
    val guerreroSimpleStats = fixture.guerreroSimple.stats
    val expectedStats = Guerrero.apply(fixture.baseStats)
    assert(guerreroSimpleStats.equals(expectedStats))
  }

  "Un guerrero simple" should "poder cambiar de trabajo y obtener nuevas stats finales" in {
    val magoSimple = fixture.guerreroSimple.trabajo(Mago)
    val magoSimpleStats = magoSimple.stats
    val expectedStats = Mago.apply(fixture.baseStats)
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

}
