package TADPQuest

trait Trabajo extends Modifier {
  def statPrincipal(): Stat
}

/*** TRABAJOS (DOMINIO) ***/

object Guerrero extends Trabajo {
  override def statPrincipal(): Stat = Fuerza
  override def recalcularStats(stats: Stats, heroe: Heroe): Stats = Stats(stats.hp + 10, stats.fuerza + 15, stats.velocidad, stats.inteligencia - 10)
}

object Mago extends Trabajo {
  override def statPrincipal(): Stat = Inteligencia
  override def recalcularStats(stats: Stats, heroe: Heroe): Stats = Stats(stats.hp, stats.fuerza - 20, stats.velocidad, stats.inteligencia + 20)
}

object Ladron extends Trabajo {
  override def statPrincipal(): Stat = Velocidad
  override def recalcularStats(stats: Stats, heroe: Heroe): Stats = Stats(stats.hp - 5, stats.fuerza, stats.velocidad + 10, stats.inteligencia)
}
