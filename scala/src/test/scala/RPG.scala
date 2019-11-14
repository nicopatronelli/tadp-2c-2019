import TADPQuest._

package RPG {

  /* TRABAJOS */

  object Guerrero extends Trabajo {
    override def statPrincipal(): Stat = Fuerza
    override def apply(stats: Stats): Stats = Stats(stats.hp + 10, stats.fuerza + 15, stats.velocidad, stats.inteligencia - 10)
  }

  object Mago extends Trabajo {
    override def statPrincipal(): Stat = Inteligencia
    override def apply(stats: Stats): Stats = Stats(stats.hp, stats.fuerza - 20, stats.velocidad, stats.inteligencia + 20)
  }

  object Ladron extends Trabajo {
    override def statPrincipal(): Stat = Velocidad
    override def apply(stats: Stats): Stats = Stats(stats.hp - 5, stats.fuerza, stats.velocidad + 10, stats.inteligencia)
  }

  /* ITEMS */

  object CascoVikingo extends Casco {
    override def apply(stats: Stats): Stats = stats.copy(hp = stats.hp + 10)
    override def cumpleRestriccion(heroe: Heroe): Boolean = heroe.baseStats.fuerza > 30
  }

  object ArmaduraEleganteSport extends Armadura {
    override def apply(stats: Stats): Stats = stats.copy(hp = stats.hp - 30, velocidad = stats.velocidad + 30)
    override def cumpleRestriccion(heroe: Heroe): Boolean = true
  }

  object ArcoViejo extends Arma {
    override def apply(stats: Stats): Stats = stats.copy(fuerza = stats.fuerza + 2)
    override def cumpleRestriccion(heroe: Heroe): Boolean = true
    override def requiereDosManos(): Boolean = true
  }

  object EspadaDeLaVida extends Arma {
    override def apply(stats: Stats): Stats = stats.copy(fuerza = stats.hp)
    override def cumpleRestriccion(heroe: Heroe): Boolean = true
    override def requiereDosManos(): Boolean = false
  }

  object EscudoAntiRobo extends Arma {
    override def apply(stats: Stats): Stats = stats.copy(hp = stats.hp + 20)
    override def cumpleRestriccion(heroe: Heroe): Boolean =
      heroe.trabajo match {
        case Ladron => false
        case _ if heroe.baseStats.fuerza < 20 => false
        case _ => true
      }
    override def requiereDosManos(): Boolean = false
  }

  object PalitoMagico extends Arma {
    override def apply(stats: Stats): Stats = stats.copy(fuerza = stats.fuerza + 2)
    override def cumpleRestriccion(heroe: Heroe): Boolean =
      heroe.trabajo match {
        case Mago => true
        case Ladron if heroe.baseStats.inteligencia > 30 => true
        case _ => false
      }
    override def requiereDosManos(): Boolean = false
  }

  object TalismanMaldito extends Talisman {
    override def apply(stats: Stats): Stats = Stats()
    override def cumpleRestriccion(heroe: Heroe): Boolean = true
  }

}

