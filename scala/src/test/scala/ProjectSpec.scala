import org.scalatest.{FreeSpec, Matchers}
import TADPQuest._

class ProjectSpec extends FreeSpec with Matchers {

  "Este proyecto" - {

    "cuando está correctamente configurado" - {
      "debería resolver las dependencias y pasar este test" in {
        val stats = new Stats
        stats.hp shouldBe 1
      }
    }
  }

}
