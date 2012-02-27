package models

import utils.persistence.graph
import play.api.libs.json._

/**
 * User: andy
 * Date: 2/02/12
 */

case class Group(id: Int, name: String) extends Model[Group] {
  val KIND = "groups"

  def registerUser(user:User) {
    user.addToGroup(this)
  }

  def users : List[User] = {
    graph.relationSources[User](this, User.IS_IN)
  }
}

object Group {

  implicit object GroupFormat extends Format[Group] {
    def reads(json: JsValue): Group = Group(
      (json \ "id").asOpt[Int].getOrElse(null.asInstanceOf[Int]),
      (json \ "name").as[String]
    )

    def writes(g: Group): JsValue = JsObject(List(
      "_class_" -> JsString(Group.getClass.getName),
      "name" -> JsString(g.name)
    ) ::: (if (g.id != null.asInstanceOf[Int]) {
      List("id" -> JsNumber(g.id))
    } else {
      Nil
    }))
  }
}