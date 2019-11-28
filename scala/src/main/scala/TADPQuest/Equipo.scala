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

  def vender(item: Item): Equipo = copy(pozoComun = pozoComun + item.valor())

  def integrantesQueTrabajanComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filter(_.trabajo.get.equals(trabajoBuscado))

  def integrantesQueNoTrabajenComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filterNot(_.trabajo.get.equals(trabajoBuscado))

  def cantidadDeItemsTotales: Int = integrantes.map(_.cantidadItemsEquipados).sum

  def elegirHeroePara(tarea: Tarea): Try[Heroe] = {
    // debería reutilizar el metodo mejorHeroeSegun si es posible
    Try( this.integrantes.maxBy{ heroe => tarea.facilidad(heroe, this) } )
  }

//  def realizarMision2(mision: Mision, equipo: Equipo): Try[Equipo] = {
//    for {
//      tarea <- mision.tareas
//      heroeElegido <- elegirHeroePara(tarea)
//      heroeDespuesDeTarea <- Try(tarea.serRealizadaPor(heroeElegido))
//      nuevoEquipo <- Try(equipo.reemplazarMiembro(heroeElegido, heroeDespuesDeTarea))
//      equipoRecompensado <- Try(Recompensa.cobrarRecompensa(nuevoEquipo, mision.recompensa))
//    } yield equipoRecompensado
//  }

  def realizarMision(mision: Mision): Try[Equipo] = {
    // Los efectos de realizar una tarea deben efecutarse de inmediato (antes de pasar a la
    // siguiente tarea) en el heroe (por eso uso fold)
    val equipoInicial = this
    val equipoPostMision: Try[Equipo] = mision.tareas.foldLeft(Try(equipoInicial)) { (equipo, tarea) =>
        equipo.flatMap(_.realizarTarea(tarea))
    }
    def cobrarRecompensa: Try[Equipo] = {
      equipoPostMision.map(e => Recompensa.cobrarRecompensa(e, mision.recompensa)): Try[Equipo]
    }
    cobrarRecompensa
  }

  // Un equipo entiende el mensaje realizarTarea y luego éste delega en el integrante
  // con mayor facilidad su realización (el equipo decide qué integrante realiza cada tarea)
//  def realizarTarea(tarea: Tarea): Try[Equipo] = {
//    val posibleHeroe = elegirHeroePara(tarea)
//    posibleHeroe match {
//      case Success(heroeElegido) => {
//        val heroeDespuesDeTarea = tarea.serRealizadaPor(heroeElegido)
//        Success(reemplazarMiembro(heroeElegido, heroeDespuesDeTarea))
//      }
//      case Failure(e) => Failure(e)
//    }
//  }

  def realizarTarea(tarea: Tarea): Try[Equipo] = {
    elegirHeroePara(tarea).map(heroeElegido => {
      val heroeDespuesDeTarea = tarea.serRealizadaPor(heroeElegido)
      reemplazarMiembro(heroeElegido, heroeDespuesDeTarea)
    })
  }

  private def cantidadDeIntegrantesConStatPrincipalIgualA(valorStat: Int): Int = {
    integrantes.map(_.valorStatPrincipal()).count( _ == valorStat )
  }
}
