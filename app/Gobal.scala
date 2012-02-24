import models._
import play.api._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Model.register[User]("users")
    Model.register[Group]("groups")
  }
}