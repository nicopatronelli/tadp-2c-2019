package TADPQuest
import scala.util.{Success, Failure, Try}

case class Equipo(nombre: String, integrantes: List[Heroe] = List(), pozoComun: Int = 0) {
  type Criterio = Heroe => Int

  def mejorHeroeSegun(criterio: Criterio): Option[Heroe] = Try(integrantes.maxBy(criterio)).toOption

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
    val criterioStatPrincipal: Criterio = _.valorStatPrincipal()
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

  private def cobrarRecompensa(mision: Mision): Equipo = {
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




  /*****  Entrenar V.2  ******/

  type CriterioMision = (Equipo, Equipo) => Boolean

  def elegirMision(tablon: List[Mision], criterio: CriterioMision): Mision = {
    tablon.reduceLeft { (m1, m2) =>
      val e1 = realizarMision(m1).get // todo: REFACTOR -> Try(Mision)
      val e2 = realizarMision(m2).get
      if (criterio(e1, e2)) m1 else m2
    }
  }

  def entrenar(tablon: List[Mision], criterio: CriterioMision): Try[Equipo] = {
    // Ordeno las misiones segun el criterio
    val misionesElegidas: List[Mision] = List()
    val misionesSegunCriterio = tablon.foldLeft(misionesElegidas) {
      (misionesElegidasAccum, _) => elegirMision(tablon.diff(misionesElegidasAccum), criterio) :: misionesElegidasAccum
    }.reverse

    // Realizo las misiones segun el orden de la lista
    misionesSegunCriterio.foldLeft(Try(this)) {
      (equipoEntrenado, siguienteMision) =>
        equipoEntrenado.flatMap(_.realizarMision(siguienteMision)) //.getOrElse(equipoEntrenado)
    }
  }

}
