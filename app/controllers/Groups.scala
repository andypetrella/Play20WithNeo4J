package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms._
import models.{User, Group, Model}


/**
 *
 * User: noootsab
 * Date: 26/02/12
 * Time: 12:06
 */
object Groups extends Controller {

  def j_all = Action {
    implicit request => {
      Ok(toJson(Model.all[Group]))
    }
  }

  def j_one(id: Int) = Action {
    implicit request => {
      Ok(Model.one[Group](id) map (toJson(_)) getOrElse (JsUndefined("none")))
    }
  }

  def j_users(id:Int) = Action {
    implicit request => {
      Ok(Model.one[Group](id) map { g => toJson(g.users) } getOrElse (JsUndefined("none")))
    }
  }

  

  def create = Action {
    implicit request => {
      Form[Group](
        mapping(
          "name" -> nonEmptyText
        )(
          (name: String) => Group(null.asInstanceOf[Int], name)
        )(
          (group: Group) => Some(group.name)
        )
      ).bindFromRequest().fold(
        f => BadRequest("Missing parameter"),
        (group: Group) => {
          Ok(toJson(group.save))
        }
      )
    }
  }

  def addUsers(groupId: Int) = Action {
    implicit request =>
      Form(
        mapping(
          "user" -> list[Int](number)
        )(
          (l: List[Int]) => (l.view) map {
            Model.one[User](_)
          } filter (_.isDefined) map {
            _.get
          } toList
        )(
          (l: List[User]) => Some(l map {
            _.id
          })
        )
      ) bindFromRequest() fold(
        f => BadRequest("Missing Parameter"),
        (l: List[User]) => {
          Model.one[Group](groupId) map (g =>
            l foreach ( g registerUser _ )
            )
          Ok("linked")
        }
        )
  }


}
