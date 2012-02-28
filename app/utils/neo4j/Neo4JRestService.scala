package utils.neo4j

/**
 * User: andy
 * Date: 31/01/12
 */

import play.api.libs.json.Json._
import play.api.libs.json._
import models._
import dispatch._
import utils.dispatch.PlayJsonDispatchHttp._
import utils.dispatch.ModelDispatchHttp._
import utils.persistence.GraphService

trait Neo4JRestService extends GraphService[Model[_]] {

  //val neoRest = :/("localhost", 7474)
  val neoRest = :/("7455c1bc5.hosted.neo4j.org", 7000) as ("70246e3d8", "7c4d4b891")
  val neoRestBase = neoRest / "db" / "data"
  val neoRestNode = neoRestBase / "node"
  val neoRestRel = neoRestBase / "relationship"
  val neoRestCypher = neoRestBase / "cypher"

  def selfRestUriToId(uri: String) = uri.substring(uri.lastIndexOf('/') + 1).toInt

  def neoRestNodeIndex(indexName: String) = neoRestBase / "index" / "node" / indexName

  def neoRestNodeById(id: Int) = neoRestNode / id.toString

  def neoRestRelById(id: Int) = neoRestRel / id.toString

  def buildUrl(u: String) = url(u) as ("70246e3d8", "7c4d4b891")

  //WARN :: the name conforms is mandatory to avoid conflicts with Predef.conforms for implicits
  // see https://issues.scala-lang.org/browse/SI-2811
  implicit def conforms: (JsValue) => JsValue = {
    (_: JsValue) \ "data"
  }

  implicit def defaultResultsFilter: (JsValue) => Iterable[JsValue] = {
    jsValue: JsValue =>
    //the first list is the global result
    //underneath lists are composing all returnable
    //each JsValue is then a returnable
      (jsValue \ "data").as[List[List[JsValue]]].map {
        l => {
          //here only one returnable
          val v = l.head
          v \ "data"
        }
      }
  }

  override lazy val root: Model[_] = Http(neoRestBase <:< Map("Accept" -> "application/json") >! {
    jsValue => new Model() { val id:Int = selfRestUriToId((jsValue \ "reference_node").as[String])}
  })


  def getNode[T <: Model[_]](id: Int)(implicit m: ClassManifest[T], f: Format[T]): Option[T] = {
    try {
      Http(neoRestNodeById(id) <:< Map("Accept" -> "application/json") >^> (Some(_: T)))
    } catch {
      //todo check 404
      case x => None
    }
  }


  def allNodes[T <: Model[_]](implicit m: ClassManifest[T], f: Format[T]): List[T] = relationTargets(root, Model.kindOf[T])

  def relationSources[T <: Model[_]](target: Model[_], rel: String)(implicit m: ClassManifest[T], f: Format[T]): List[T] = {
    val cypher = """
      start x=node({ref})
      match s-[:{rel}]->x
      return s
      """
      .replaceAllLiterally("{ref}", target.id.toString)
      .replaceAllLiterally("{rel}", rel)

    val props = JsObject(Seq(
      "query" -> JsString(cypher),
      "params" -> JsObject(Seq())
    ))

    Http(neoRestCypher <<(stringify(props), "application/json") >^*> { (_: Iterable[T]).toList })
  }

  def relationTargets[T <: Model[_]](start: Model[_], rel: String)(implicit m: ClassManifest[T], f: Format[T]): List[T] = {
    val cypher = """
      start x=node({ref})
      match x-[:{rel}]->t
      return t
      """
      .replaceAllLiterally("{ref}", start.id.toString)
      .replaceAllLiterally("{rel}", rel)

    val props = JsObject(Seq(
      "query" -> JsString(cypher),
      "params" -> JsObject(Seq())
    ))
    Http(neoRestCypher <<(stringify(props), "application/json") >^*> { (_: Iterable[T]).toList })
  }

  def findNodeByExactMatch[T <: Model[_]](indexName: String, key: String, value: String)(implicit m: ClassManifest[T], f: Format[T]): Seq[T] = {
    //todo use it in other places ?
    //todo ... now we override the default
    implicit val defaultResultsFilter: (JsValue) => Iterable[JsValue] = {
      jsValue: JsValue =>
      //the first list is the global result
      //underneath lists are composing all returnable
      //each JsValue is then a returnable
        jsValue.as[Seq[JsValue]].map { _ \ "data" }
    }

    Http((neoRestNodeIndex(indexName) / key / value)
      <:< Map("Accept" -> "application/json") >^*> { (_:Iterable[T]).toSeq }
    )
  }

  def saveNode[T <: Model[_]](t: T)(implicit m: ClassManifest[T], f: Format[T]): T = {
    val (id: Int, property: String) = Http(
      (neoRestNode <<(stringify(toJson(t)), "application/json"))
        <:< Map("Accept" -> "application/json")
        >! {
        jsValue =>
          val id: Int = selfRestUriToId((jsValue \ "self").as[String])
          (id, (jsValue \ "property").as[String])
      }
    )

    //update the id property
    Http(
      (buildUrl(property.replace("{key}", "id")) <<(id.toString, "application/json") PUT)
        <:< Map("Accept" -> "application/json") >| //no content
    )

    val model = getNode[T](id).get

    //create the rel for the kind
    linkToRoot(Model.kindOf[T], model)

    model
  }

  def indexNode[T <: Model[_]](model: T, indexName: String, key: String, value: String) {
    val props = JsObject(Seq(
      "uri" -> JsString(neoRestNodeById(model.id).path),
      "key" -> JsString(key),
      "value" -> JsString(value)
    ))

    //the request
    Http(
      (neoRestNodeIndex(indexName) <<(stringify(props), "application/json"))
        <:< Map("Accept" -> "application/json")
        >! {
        jsValue =>
        //((jsValue \ "self").as[String], (jsValue \ "data").as[JsObject])
      })
  }

  def linkToRoot(rel: String, end: Model[_]) {
    createRelationship(root, rel, end)
  }

  def createRelationship(start: Model[_], rel: String, end: Model[_]) {
    //retrieve the creation rel url for the kind
    val createRelationship = Http(neoRestNodeById(start.id) <:< Map("Accept" -> "application/json") >! {
      jsValue => (jsValue \ "create_relationship").as[String]
    })

    //create the relationship 'rel' to the created node
    //the payload
    val props = JsObject(Seq(
      "to" -> JsString(neoRestNodeById(end.id).path),
      "type" -> JsString(rel)
    ))
    //the request
    Http(
      (buildUrl(createRelationship) <<(stringify(props), "application/json"))
        <:< Map("Accept" -> "application/json")
        >! {
        jsValue => //((jsValue \ "self").as[String], (jsValue \ "data").as[JsObject])
      })
  }
}