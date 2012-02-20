package dispatch

import org.specs2._
import dispatch._
import utils.dispatch.PlayJsonDispatchHttp._
import play.api.libs.json._

/**
 * User: apetrell
 */

class JsonHandlerSpec extends Specification {
  def is =

    "The Json from Blogger " ^ {
      " contain a encoding property " ! {
        Http(url("http://ska-la.blogspot.com/feeds/posts/default?alt=json") <:< Map("Accept" -> "application/json") >! {
          jsValue: JsValue =>
            (jsValue \ "encoding").as[String] must_== "UTF-8"
        })
      } ^
        " contain a author sub property " ! {
          Http(url("http://ska-la.blogspot.com/feeds/posts/default?alt=json") <:< Map("Accept" -> "application/json") >! {
            jsValue: JsValue => {
              (jsValue \\ "author") must not size (0)
              ((((jsValue \\ "author") head) \\ "name" head) \ "$t").as[String] must contain("andy")
            }
          })
        }

    }


}
