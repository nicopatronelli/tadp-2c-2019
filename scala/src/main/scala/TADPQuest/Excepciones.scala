package TADPQuest

case class NoSePuedeRealizarTareaException(tarea: Tarea) extends Exception
case class NoStatPrincipalException(mensaje: String) extends Exception
