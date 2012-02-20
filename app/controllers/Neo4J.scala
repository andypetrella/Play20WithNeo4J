package controllers

import play.api.mvc._
import play.api.data._
import play.api.libs.json._
import play.api.libs.json.Json._
import dispatch._
import utils.dispatch.PlayJsonDispatchHttp._
import utils.neo4j.Neo4JRestService

object Neo4J extends Controller {

  object neo extends Neo4JRestService

  def root = Action {
    val node = Http(neo.neoRestNodeById(neo.root.id) <:< Map("Accept" -> "application/json") >! {
      jsValue =>
        (jsValue \ "self").as[String]
    })
    Ok(node)
  }

  def node(id: Int) = Action {
    val node = Http(neo.neoRestNodeById(id) <:< Map("Accept" -> "application/json") >! {
      jsValue => (jsValue \ "self").as[String]
    })
    Ok(node)
  }

  def createNode = Action {
    val node = Http((neo.neoRestNode POST) <:< Map("Accept" -> "application/json") >! {
      jsValue =>
        (jsValue \ "self").as[String]
    })
    Ok(node)
  }

  def createNodeWithProperties = Action {
    val props = toJson(Map("prop1" -> "value1", "prop2" -> "value2"))

    val (node, data: JsObject) = Http(
      (neo.neoRestNode <<(stringify(props), "application/json"))
        <:< Map("Accept" -> "application/json")
        >! {
        jsValue =>
          ((jsValue \ "self").as[String], (jsValue \ "data").as[JsObject])
      })
    Ok("" + node + " -- " + data.toString)
  }

  def relationship(id: Int) = Action {
    val (rel, start, end) = Http(neo.neoRestRelById(id) <:< Map("Accept" -> "application/json") >! {
      jsValue =>
        ((jsValue \ "self").as[String],
          (jsValue \ "start").as[String],
          (jsValue \ "end").as[String])
    })
    Ok("" + start + " - [" + rel + "] - " + end)
  }

  def createRelationship = Action {
    val create_relationship = Http(neo.neoRestNodeById(neo.root.id) <:< Map("Accept" -> "application/json") >! {
      jsValue =>
        (jsValue \ "create_relationship").as[String]
    })

    val node = Http((neo.neoRestNode POST) <:< Map("Accept" -> "application/json") >! {
      jsValue =>
        (jsValue \ "self").as[String]
    })

    val props = JsObject(Seq(
      "to" -> JsString(node),
      "type" -> JsString("TEST")
    ))

    val (rel, data: JsObject) = Http(
      (url(create_relationship) <<(stringify(props), "application/json"))
        <:< Map("Accept" -> "application/json")
        >! {
        jsValue =>
          ((jsValue \ "self").as[String], (jsValue \ "data").as[JsObject])
      })

    Ok(rel)
  }


}