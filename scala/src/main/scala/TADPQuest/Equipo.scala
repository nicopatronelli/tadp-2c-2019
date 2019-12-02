package TADPQuest
import scala.util.{Failure, Success, Try}

case class Equipo(nombre: String, integrantes: List[Heroe] = List(), pozoComun: Int = 0) {
  type CriterioHeroe = Heroe => Int
  // Debe retornar true si el resultado del 1er equipo es mejor que el 2do
  type CriterioMision = (Equipo, Equipo) => Boolean

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
    // Si hay un empate en el valor del Stat Principal, no hay un lider definido
    mejorHeroeSegun(criterioStatPrincipal) match {
      case Some(heroe)
        if cantidadDeIntegrantesConStatPrincipalIgualA(heroe.valorStatPrincipal()) == 1 => Some(heroe)
      case _ => None
    }
  }

  private def cantidadDeIntegrantesConStatPrincipalIgualA(valorStat: Int): Int =
    integrantes.map(_.valorStatPrincipal()).count(_ == valorStat)

  def vender(item: Item): Equipo = copy(pozoComun = pozoComun + item.valor())

  def integrantesQueTrabajanComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filter(_.trabajo.get.equals(trabajoBuscado))

  def integrantesQueNoTrabajenComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filterNot(_.trabajo.get.equals(trabajoBuscado))

  def cantidadDeItemsTotales: Int = integrantes.map(_.cantidadItemsEquipados).sum

  def elegirHeroePara(tarea: Tarea): Try[Heroe] =
    Try( integrantes.maxBy{ heroe => tarea.facilidad(heroe, this) } )

  def realizarTarea(tarea: Tarea): Try[Equipo] = {
    elegirHeroePara(tarea).map( heroeElegido => {
      val heroeDespuesDeTarea = tarea.serRealizadaPor(heroeElegido)
      reemplazarMiembro(heroeElegido, heroeDespuesDeTarea)
    })
  }

  def cobrarRecompensa(mision: Mision): Equipo = { // Retorna un "equipo recompensado"
    mision.recompensa match {
      case CofreDeOro(cantidadDeOro) => copy(pozoComun = pozoComun + cantidadDeOro)
      case NuevoItem(item)   => obtenerItem(item)
      case NuevoHeroe(heroe) => obtenerMiembro(heroe)
      case IncrementarFuerzaALosMagos(incremento) =>
        val magos = integrantesQueTrabajanComo(Mago)
        val magosMejorados = magos.map(m => m.copy(baseStats = m.baseStats.copy(fuerza = m.baseStats.fuerza + incremento)))
        val noMagos = integrantesQueNoTrabajenComo(Mago)
        copy(integrantes = noMagos ++ magosMejorados)
    }
  }

  def realizarMision(mision: Mision): Try[Equipo] = {
    val equipoInicial = Try(this)
    val equipoPostMision = mision.tareas.foldLeft(equipoInicial) { (equipo, tarea) =>
        equipo.flatMap(_.realizarTarea(tarea))
    }
    equipoPostMision.map(_.cobrarRecompensa(mision))
  }

  def elegirMision(criterio: CriterioMision, tablon: List[Mision]): Try[Mision] = {
    // (MisionRealizada, EquipoResultante)
    val resultadoEquipo: (Try[Mision], Equipo) = (Failure(NoSeEligioMisionException()), this)
    tablon.foldLeft(resultadoEquipo) { (mejorResultadoDeEquipo, siguienteMision) =>
      realizarMision(siguienteMision) match {
        case Success(equipo) if mejorResultadoDeEquipo._1.isFailure | criterio(equipo, mejorResultadoDeEquipo._2) =>
          (Success(siguienteMision), equipo)
        case _ => mejorResultadoDeEquipo
      }
    }._1
  }

  def entrenar(criterio: CriterioMision, tablon: List[Mision]): Try[Equipo] = {
    elegirMision(criterio, tablon).flatMap { mision =>
      val equipoPostMision = realizarMision(mision)
      val misionesRestantes = tablon.diff(List(mision)) // Descarto la misión que acabo de hacer
      if (misionesRestantes.nonEmpty) // Si quedan misiones por hacer sigo entrenando
        equipoPostMision.flatMap(_.entrenar(criterio, misionesRestantes))
      else
        equipoPostMision
    }
  }
}
