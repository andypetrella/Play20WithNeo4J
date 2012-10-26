package controllers

import play.api._
import play.api.data._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("playRoutes")(
        //groups
        controllers.routes.javascript.Groups.j_all,
        controllers.routes.javascript.Groups.j_one,
        controllers.routes.javascript.Groups.j_users,

        controllers.routes.javascript.Groups.create,
        controllers.routes.javascript.Groups.addUsers,


        //users
        controllers.routes.javascript.Users.j_all,
        controllers.routes.javascript.Users.j_one,
        controllers.routes.javascript.Users.j_knows,

        controllers.routes.javascript.Users.create,
        controllers.routes.javascript.Users.userKnows
      )
    ).as("text/javascript")
  }

}
