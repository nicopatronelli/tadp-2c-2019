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
  def elegirMisionDeprecada(criterio: CriterioMision, tablon: List[Mision]): Mision = { // OK
    tablon.reduceLeft{ (m1, m2) =>
      val e1 = this.realizarMision(m1).get // todo: REFACTOR -> Try(Mision)
      val e2 = this.realizarMision(m2).get
      if (criterio(e1, e2)) m1 else m2
    }
  }

  def elegirMision(criterio: CriterioMision, tablon: List[Mision]): Mision = { // OK
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

  def entrenar(criterio: CriterioMision, tablon: List[Mision]): Try[Equipo] = {
    val proximaMision = elegirMision(criterio, tablon)
    //println(s"Se eligió la misión: $proximaMision")
    var equipoPostMision: Try[Equipo] = realizarMision(proximaMision)
    val misionesRestantes = tablon.filterNot(m => m == proximaMision) // descarto la misión que acabo de hacer
    if (misionesRestantes.nonEmpty) // Si quedan misiones por hacer sigo entrenando
      equipoPostMision = equipoPostMision.flatMap(_.entrenar(criterio, misionesRestantes))
    equipoPostMision
  }

}
