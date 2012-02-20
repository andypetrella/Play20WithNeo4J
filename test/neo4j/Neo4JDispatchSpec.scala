package neo4j

import org.specs2._
import dispatch._
import play.api.libs.json._
import utils.dispatch.PlayJsonDispatchHttp._
import utils.neo4j.Neo4JRestService

/**
 * User: apetrell
 */

object neo extends Neo4JRestService

class Neo4JDispatchSpec extends Specification {
  def is =
    "Service response " ^ {
      "Contains the reference node" ! {
        neo.root.id must beGreaterThanOrEqualTo(0)
      } ^
        "Contains the node url" ! {
          Http(neo.neoRestBase <:< Map("Accept" -> "application/json") >! {
            jsValue: JsValue => {
              (jsValue \ "node").as[String] must startingWith(neo.neoRest.to_uri.toString) and contain("/db/data/node")
            }
          })
        }
    }

}
