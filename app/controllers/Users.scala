package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json._
import models.{User, Model}
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms._


/**
 *
 * User: noootsab
 * Date: 26/02/12
 * Time: 12:06
 */
object Users extends Controller {

  def j_all = Action {
    implicit request => {
      Ok(toJson(Model.all[User]))
    }
  }

  def j_knows(id: Int) = Action {
    implicit request => {
      Ok(toJson(Model.one[User](id) map (u => toJson(u.known)) getOrElse (JsUndefined("none"))))
    }
  }

  def j_one(id: Int) = Action {
    implicit request => {
      Ok(Model.one[User](id) map (toJson(_)) getOrElse (JsUndefined("none")))
    }
  }


  def create = Action {
    implicit request => {
      Form[User](
        mapping(
          "firstname" -> nonEmptyText
        )(
          (firstName: String) => User(null.asInstanceOf[Int], firstName)
        )(
          (user: User) => Some(user.firstName)
        )
      ).bindFromRequest().fold(
        f => BadRequest("Missing parameter"),
        (user: User) => {
          Ok(toJson(user.save))
        }
      )
    }
  }

  def userKnows(userId: Int) = Action {
    implicit request =>
      Form(
        mapping(
          "knows" -> list[Int](number)
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
          Model.one[User](userId) map (u =>
            l foreach ( u iKnow _ )
          )
          Ok("linked")
        }
        )
  }

}
