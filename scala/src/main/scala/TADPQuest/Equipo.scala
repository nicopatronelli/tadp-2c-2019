package TADPQuest
import scala.util.{Success, Failure, Try}

case class Equipo(nombre: String, integrantes: List[Heroe] = List(), pozoComun: Int = 0) {
  type Criterio = Heroe => Int

  // Ante una lista vacia o un empate, retorna None
  def mejorHeroeSegun(criterio: Criterio): Option[Heroe] = Try(integrantes.maxBy(criterio)) match {
    case Success(heroe)
      // Marca error de tipo si pongo Some(heroe), pero corre
      if esMaximoUnico(criterio(heroe), integrantes.map(criterio)) => Option(heroe)
    case _ => None
  }

  def obtenerItem(nuevoItem: Item): Equipo = {
    // Pongo cada heroe en una tupla (HeroeSinItem, IncrementoStatPrincipal)
    val heroesConIncremento: List[(Heroe, Int)] = heroesConIncrementoDeStatPrincipal(nuevoItem)
    if ( algunoTieneIncrementoPositivo(heroesConIncremento) ) {
      // Me quedo con el heroe de mayor incremento
      val heroeConMaximoIncremento = heroesConIncremento.maxBy( heroeConIncremento => heroeConIncremento._2 )._1
      // Reemplazo el heroe por el mismo heroe con el item equipado
      reemplazarMiembro(heroeConMaximoIncremento, heroeConMaximoIncremento.agregarItem(nuevoItem))
    } else {
      vender(nuevoItem)
    }
  }

  def obtenerMiembro(nuevoIntegrante: Heroe): Equipo = copy(integrantes = nuevoIntegrante :: integrantes)

  def reemplazarMiembro(integranteReemplazado: Heroe, nuevoIntegrante: Heroe): Equipo =
    Try(copy(integrantes = integrantes.map {
      case integrante if integrante.equals(integranteReemplazado) => nuevoIntegrante
      case integrante => integrante
    })).getOrElse(this)

  def lider(): Option[Heroe] = {
    val criterioStatPrincipal: Criterio = _.valorStatPrincipal()
    mejorHeroeSegun(criterioStatPrincipal)
  }

  def vender(item: Item): Equipo = copy(pozoComun = pozoComun + item.valor())

  def integrantesQueTrabajanComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filter(_.trabajo.get.equals(trabajoBuscado))

  def integrantesQueNoTrabajenComo(trabajoBuscado: Trabajo): List[Heroe] =
    integrantes.filterNot(_.trabajo.get.equals(trabajoBuscado))

  def cantidadDeItemsTotales: Int = integrantes.map(_.cantidadItemsEquipados).sum

  private def esMaximoUnico(element: Int, list: List[Int]): Boolean =
    // Saca el entero de la lista y se fija si el maximo de la lista es menor al elemento extraido
    list.diff(List(element)).max < element

  private def algunoTieneIncrementoPositivo(heroesConIncremento: List[(Heroe, Int)]): Boolean =
    heroesConIncremento.exists( incremento => incremento._2 > 0)

  private def heroesConIncrementoDeStatPrincipal(nuevoItem: Item): List[(Heroe, Int)] = {
    // Pongo cada heroe en una tupla (HeroeSinItem, HeroeConItem)
    val integrantesConNuevoItem: List[(Heroe, Heroe)] =
      integrantes.map( integrante => (integrante, integrante.agregarItem(nuevoItem)) )

    // Pongo cada heroe en una tupla (HeroeSinItem, IncrementoStatPrincipal)
    integrantesConNuevoItem.map( integrante =>
      (integrante._1, integrante._2.valorStatPrincipal() - integrante._1.valorStatPrincipal()) )
  }

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

}
