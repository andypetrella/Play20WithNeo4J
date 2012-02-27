package controllers

import play.api._
import play.api.data._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  // -- Javascript routing

  def javascriptRoutes = Action {
    println(Routes.javascriptRouter("playRoutes")(
      controllers.routes.javascript.Users.j_all,
      controllers.routes.javascript.Users.j_one
    ))
    Ok(
      Routes.javascriptRouter("playRoutes")(
        controllers.routes.javascript.Groups.j_all,
        controllers.routes.javascript.Groups.j_one,
        controllers.routes.javascript.Groups.j_users,


        controllers.routes.javascript.Users.j_all,
        controllers.routes.javascript.Users.j_one,
        controllers.routes.javascript.Users.j_knows,

        controllers.routes.javascript.Users.userKnows
      )
    ).as("text/javascript")
  }

}