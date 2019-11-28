package TADPQuest

case class Heroe(baseStats: Stats, trabajo: Option[Trabajo], inventario: Inventario = Inventario()) {

  def stats: Stats = { // Modifica los stats base aplicando los modificadores con fold
    val modificadores  = trabajo match {
      case Some(trabajo) => List(trabajo, inventario)
      case None => List(inventario)
    }
    // val modificadores: List[Modifier] = inventario :: trabajo.toList // Si el trabajo es None, trabajo.toList retorna una lista vacía
    modificadores.foldLeft(baseStats) { (accum, modificador) => modificador.recalcularStats(accum, this) }
  }

  // Getters directos para los stats finales (ya modificados)
  def hp: Int = stats.hp
  def fuerza: Int = stats.fuerza
  def velocidad: Int = stats.velocidad
  def inteligencia: Int = stats.inteligencia

  def cambiarTrabajo(nuevoTrabajo: Option[Trabajo]): Heroe = copy(trabajo = nuevoTrabajo)

  def statPrincipal(): Option[Stat] = trabajo.map(_.statPrincipal())

  def valorStatPrincipal(): Int = statPrincipal() match {
    case Some(stat) => stats.valor(stat)
    case None => throw NoStatPrincipalException(s"El heroe $this no tiene trabajo, así que no tiene un stat principal")
  }

  def agregarItem(item: Item): Heroe = {
    if ( item.cumpleRestriccion(this) ) copy(inventario = inventario.agregarItem(item))
    else this // Si no cumple con la restriccion, retorno el heroe como estaba
  }

  def cantidadItemsEquipados: Int = inventario.cantidadDeItems

  /*
    def diferenciaDeStats(otroHeroe: Heroe): Stats =
      Stats(
        stats.hp - otroHeroe.stats.hp,
        stats.fuerza - otroHeroe.stats.fuerza,
        stats.velocidad - otroHeroe.stats.velocidad,
        stats.inteligencia - otroHeroe.stats.inteligencia
      )
  */
}







