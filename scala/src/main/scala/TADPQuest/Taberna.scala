package TADPQuest

import scala.util.Try

object Taberna {
  type Criterio = (Equipo, Equipo) => Boolean
  val misionPeligrosa = Mision(
    List(PelearContraMonstruo(20), PelearContraMonstruo(30)),
    CofreDeOro(1000)
  )
  val misionFuerzaParaLosMagos = Mision(
    List(ForzarPuerta, RescatarPrincesa),
    IncrementarFuerzaALosMagos(200)
  )
  val misionParaLadronLider = Mision(
    List(ForzarPuerta, RobarTalisman(TalismanMaldito)),
    NuevoItem(TalismanMaldito)
  )
  val misionLarga = Mision(
    List(PelearContraMonstruo(10), ForzarPuerta, PelearContraMonstruo(40)),
    NuevoHeroe(Heroe(Stats(100, 50, 60, 120), Option(Mago), Inventario()))
  )
  val tablon: List[Mision] = List(
    misionPeligrosa, misionFuerzaParaLosMagos, misionParaLadronLider, misionLarga
  )

  /*
  def elegirMision(equipo: Equipo, criterio: Criterio): Mision = {
    tablon.reduceLeft{ (m1, m2) =>
      val e1 = equipo.realizarMision(m1).get // todo: REFACTOR -> Try(Mision)
      val e2 = equipo.realizarMision(m2).get
      if (criterio(e1, e2)) m1 else m2
    }
  }
*/


    //  def elegirMision(equipo: Equipo, criterio: Criterio): Try[Mision] = {
//    for {
//      m1 <- tablon
//      m2 <- tablon
//      e1 <- equipo.realizarMision(m1)
//      e2 <- equipo.realizarMision(m2)
//      if criterio(e1, e2)
//      m <- Try(m2) if !criterio(e1,e2)
//    } yield m
//
//    tablon.reduceLeft{ (m1, m2) =>
//      val e1 = equipo.realizarMision(m1)
//      val e2 = equipo.realizarMision(m2)
//
//      if (criterio(e1.get, e2.get)) Try(m1) else Try(m2)
//    }
//  }

  /*
  def entrenar(equipo: Equipo): Try[Equipo] = {
    val equipoInicial = equipo
    tablon.foldLeft(Try(equipoInicial)){
      (eq,mision) => eq.flatMap(_.realizarMision(mision))
    }: Try[Equipo]
  }
*/

}