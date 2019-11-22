package TADPQuest

case class Heroe(baseStats: Stats, trabajo: Trabajo, inventario: Inventario = Inventario()) {

  def stats: Stats = { // Modifica los stats base aplicando los modificadores con fold
    val modificadores: List[Modifier] = List(trabajo, inventario)
    modificadores.foldLeft(baseStats) { (accum, modificador) => modificador.recalcularStats(accum) }
  }
  // Getters directos para los stats reales (ya modificados)
  def hp: Int = stats.hp
  def fuerza: Int = stats.fuerza
  def velocidad: Int = stats.velocidad
  def inteligencia: Int = stats.inteligencia

  def cambiarTrabajo(nuevoTrabajo: Trabajo): Heroe = copy(trabajo = nuevoTrabajo)

  def statPrincipal(): Stat = trabajo.statPrincipal()

  def valorStatPrincipal(): Int = stats.valor(statPrincipal())

  def agregarItem(item: Item): Heroe = {
    if ( item.cumpleRestriccion(this) ) copy(inventario = inventario.agregarItem(item))
    else this // Si no cumple con la restriccion, retorno el heroe como estaba
  }

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







