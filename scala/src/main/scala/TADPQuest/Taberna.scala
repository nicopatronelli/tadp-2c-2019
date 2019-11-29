package TADPQuest

import scala.util.{Failure, Success, Try}

object Taberna {
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
  // Tablon de misiones
  val tablon: List[Mision] = List(
    misionPeligrosa, misionFuerzaParaLosMagos, misionParaLadronLider, misionLarga
  )
  // Otras misiones para tests
  val misionMasPeligrosa = Mision(
    List(PelearContraMonstruo(40), ForzarPuerta, PelearContraMonstruo(50)),
    CofreDeOro(2000)
  )

//  def elegirMisionFoldLeft(equipo: Equipo, criterio: Criterio): Try[Mision] = {
//    val misionPorDefecto = tablon(0)
//    tablon.foldLeft(Try(misionPorDefecto)){ (m1: Try[Mision], m2: Try[Mision]) =>
//      val e1 = m1.map(_.serRealizadaPor(equipo))
//      val e2 = m2.map(_.serRealizadaPor(equipo))
//      (e1, e2) match {
//        case (Success(oe1), Success(oe2)) =>
//          if (criterio(oe1, oe2)) m1 else m2
//        case (Success(oe1), Failure(oe2)) => m1
//        case (Failure(oe1), Success(oe2)) => m2
//        case (Failure(oe1), Failure(oe2)) => Failure(oe1)
//      }
//    }
//  }

//  def elegirMision2(equipo: Equipo, criterio: Criterio): Try[Mision] = {
//    for {
//      m1 <- tablon
//      m2 <- tablon
//      if m1 != m2
//      e1 = equipo.realizarMision(m1)
//      e2 = equipo.realizarMision(m2)
//      if criterio(e1, e2)
//    } yield m1
//  }
//
//    tablon.reduceLeft{ (m1, m2) =>
//      val e1 = equipo.realizarMision(m1)
//      val e2 = equipo.realizarMision(m2)
//
//      if (criterio(e1.get, e2.get)) Try(m1) else Try(m2)
//    }
//  }

}