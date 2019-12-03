package TADPQuest
import TADPQuest.Taberna._
import scala.util.{Failure, Success, Try}

case class Equipo(nombre: String, integrantes: List[Heroe] = List(), pozoComun: Int = 0) {
  type CriterioHeroe = Heroe => Int

  def mejorHeroeSegun(criterio: CriterioHeroe): Option[Heroe] = Try(integrantes.maxBy(criterio)).toOption

  def obtenerItem(nuevoItem: Item): Equipo = {
    val incrementos: List[Int] = integrantes.map( _.incrementoDelStatPrincipalCon(nuevoItem) )
    if ( incrementos.forall( _ <= 0 ) ) {
      vender(nuevoItem)
    } else {
      val heroeConMayorIncremento: Heroe = integrantes.maxBy( _.incrementoDelStatPrincipalCon(nuevoItem) )
      reemplazarMiembro(heroeConMayorIncremento, heroeConMayorIncremento.agregarItem(nuevoItem))
    }
  }

  def obtenerMiembro(nuevoIntegrante: Heroe): Equipo = copy(integrantes = nuevoIntegrante :: integrantes)

  def reemplazarMiembro(integranteReemplazado: Heroe, nuevoIntegrante: Heroe): Equipo =
    copy(integrantes = integrantes.map {
      case integrante if integrante.equals(integranteReemplazado) => nuevoIntegrante
      case integrante => integrante
    })

  def lider(): Option[Heroe] = {
    val criterioStatPrincipal: CriterioHeroe = _.valorStatPrincipal()
    mejorHeroeSegun(criterioStatPrincipal) match {
      // Si hay un empate en el valor del Stat Principal, no hay un lider definido
      case Some(heroe)
        if cantidadDeIntegrantesConStatPrincipalIgualA(heroe.valorStatPrincipal()) == 1 => Some(heroe)
      case _ => None
    }
  }

  private def cantidadDeIntegrantesConStatPrincipalIgualA(valorStat: Int): Int = {
    integrantes.map(_.valorStatPrincipal()).count(_ == valorStat)
  }

  def vender(item: Item): Equipo = copy(pozoComun = pozoComun + item.valor())

  def integrantesQueTrabajanComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filter(_.trabajo.get.equals(trabajoBuscado))

  def integrantesQueNoTrabajenComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filterNot(_.trabajo.get.equals(trabajoBuscado))

  def cantidadDeItemsTotales: Int = integrantes.map(_.cantidadItemsEquipados).sum

  def elegirHeroePara(tarea: Tarea): Try[Heroe] = {
    // debería reutilizar el metodo mejorHeroeSegun si es posible
    Try( integrantes.maxBy{ heroe => tarea.facilidad(heroe, this) } )
  }

  def realizarTarea(tarea: Tarea): Try[Equipo] = { // OK
    elegirHeroePara(tarea).map(heroeElegido => {
      val heroeDespuesDeTarea = tarea.serRealizadaPor(heroeElegido)
      reemplazarMiembro(heroeElegido, heroeDespuesDeTarea)
    })
  }

  def cobrarRecompensa(mision: Mision): Equipo = {
    Recompensa.cobrarRecompensa(this, mision.recompensa)
  }

  def realizarMision(mision: Mision): Try[Equipo] = { // OK
    val equipoInicial = this
    val equipoPostMision = mision.tareas.foldLeft(Try(equipoInicial)) { (equipo, tarea) =>
        equipo.flatMap(_.realizarTarea(tarea))
    }
    val equipoRecompensado = equipoPostMision.map(_.cobrarRecompensa(mision))
    equipoRecompensado
  }

  type CriterioMision = (Equipo, Equipo) => Boolean
  def elegirMision2(criterio: CriterioMision, tablon: List[Mision]): Mision = { // OK
    tablon.reduceLeft{ (m1, m2) =>
      val resultadoEquipo1 = this.realizarMision(m1)
      val resultadoEquipo2 = this.realizarMision(m2)
      (resultadoEquipo1, resultadoEquipo2) match { // es una dupla de la forma (Try[Equipo], Try[Equipo])
        case (Success(e1), Success(e2)) =>
          if (criterio(e1, e2)) m1 else m2
        case (Success(_), Failure(_)) => m1
        case (Failure(_), Success(_)) => m2
        ///case (Failure(_), Failure(_)) => ???
      }
    }
  }

  def elegirMision(criterio: CriterioMision, tablon: List[Mision]): Option[Mision] = { // OK
    tablon.foldLeft(tablon.headOption){ (actualMision, proxMision) =>
      val resultadoEquipo1 = this.realizarMision(actualMision.get)
      val resultadoEquipo2 = this.realizarMision(proxMision)
      (resultadoEquipo1, resultadoEquipo2) match { // es una dupla de la forma (Try[Equipo], Try[Equipo])
        case (Success(e1), Success(e2)) =>
          if (criterio(e1, e2)) actualMision else Option(proxMision)
        case (Success(_), Failure(_)) => actualMision
        case (Failure(_), Success(_)) => Option(proxMision)
        case (Failure(_), Failure(_)) => None
      }
    }
  }

//  def ordenarMisiones(criterio: CriterioMision, tablon: List[Mision]): List[Mision] = { // OK
//    // Ordeno las misiones segun el criterio
//    val misionesElegidas: List[Mision] = List()
//    val misionesSegunCriterio = tablon.foldLeft(misionesElegidas) {
//      (misionesElegidasAccum, _) => elegirMision(criterio, tablon.diff(misionesElegidasAccum)) :: misionesElegidasAccum
//    }.reverse
//    misionesSegunCriterio
//  }

  def ordenarMisiones(criterio: CriterioMision, tablon: List[Mision]): List[Mision] = {
    // Ordeno las misiones segun el criterio
    val misionesElegidas: List[Mision] = List()
    val misionesSegunCriterio = tablon.foldLeft(misionesElegidas) {
      (misionesElegidasAccum, _) =>
        val misionElegida = elegirMision(criterio, tablon.diff(misionesElegidasAccum))
        misionElegida.toList ++ misionesElegidasAccum
    }.reverse
    misionesSegunCriterio
  }

  def entrenar(criterio: CriterioMision, tablon: List[Mision]): Try[Equipo] = {
    val misionesOrdenadas = ordenarMisiones(criterio, tablon)
    val equipoInicial = this
    misionesOrdenadas.foldLeft(Try(equipoInicial)) {
      (equipoEntrenado, siguienteMision) =>
        equipoEntrenado.flatMap(_.realizarMision(siguienteMision))
    }
  }

//  def entrenarDeprecada(criterio: CriterioMision, tablon: List[Mision]): Try[Equipo] = {
//    val proximaMision = elegirMision(criterio, tablon)
//    //println(s"Se eligió la misión: $proximaMision")
//    val equipoPostMision: Try[Equipo] = realizarMision(proximaMision)
//    val misionesRestantes = tablon.filterNot(m => m == proximaMision) // descarto la misión que acabo de hacer
//    if (misionesRestantes.nonEmpty) // Si quedan misiones por hacer sigo entrenando
//      equipoPostMision.flatMap(_.entrenarDeprecada(criterio, misionesRestantes))
//    else
//      equipoPostMision
//  }
}
