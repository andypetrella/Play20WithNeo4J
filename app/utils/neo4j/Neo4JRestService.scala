package utils.neo4j

/**
 * User: andy
 * Date: 31/01/12
 */

import dispatch._
import models.Model
import dispatch.HttpExecutor
import utils.dispatch.PlayJsonDispatchHttp._

trait Neo4JRestService {

  val neoRest = :/("localhost", 7474)
  val neoRestBase = neoRest / "db" / "data"
  val neoRestNode = neoRestBase / "node"
  val neoRestRel = neoRestBase / "relationship"
  val neoRestCypher = neoRestBase / "cypher"

  def selfRestUriToId(uri: String) = uri.substring(uri.lastIndexOf('/') + 1).toInt

  def neoRestNodeIndex(indexName: String) = neoRestBase / "index" / "node" / indexName

  def neoRestNodeById(id: Int) = neoRestNode / id.toString

  def neoRestRelById(id: Int) = neoRestRel / id.toString

  lazy val root:{val id:Int } = Http(neoRestBase <:< Map("Accept" -> "application/json") >! {
    jsValue => new {
      val id = selfRestUriToId((jsValue \ "reference_node").as[String])
    }
  })

}