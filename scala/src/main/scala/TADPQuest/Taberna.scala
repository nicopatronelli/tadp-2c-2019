package TADPQuest

object Taberna {
  type Criterio = (Equipo, Equipo) => Boolean
  private val misionPeligrosa = Mision(
    List(PelearContraMonstruo(20), PelearContraMonstruo(30)), CofreDeOro(1000)
  )
  private val misionParaLadronLider = Mision(
    List(ForzarPuerta, RobarTalisman(TalismanMaldito)), NuevoItem(TalismanMaldito)
  )
  private val misionLarga = Mision(
    List(PelearContraMonstruo(10), ForzarPuerta, PelearContraMonstruo(40)),
    NuevoHeroe(Heroe(Stats(100, 50, 60, 110), Mago, Inventario()))
  )
  val tablon: List[Mision] = List(misionPeligrosa, misionParaLadronLider, misionLarga)

  def elegirMision(criterio: Criterio): Boolean = ???

  def entrenar(equipo: Equipo): Equipo = ???
}