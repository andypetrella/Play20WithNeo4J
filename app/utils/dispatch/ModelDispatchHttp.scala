package utils.dispatch

/**
 * User: andy
 * Date: 12/02/12
 */

import dispatch._
import play.api.libs.json._
import play.api.libs.json.Json._
import models.Model


trait ImplicitModelHandlers {
  /** Add JSON-processing method >! to dispatch.Request */
  implicit def handlerToModelHandlers(r: HandlerVerbs) = new ModelHandlers(r)
  implicit def requestToModelHandlers(r: Request) = new ModelHandlers(r)
  implicit def stringToModelHandlers(r: String) = new ModelHandlers(new Request(r))
}

object ModelDispatchHttp extends ImplicitModelHandlers

class ModelHandlers(subject: HandlerVerbs) {

  /**Process response as Model Instance in block */
  def >^>[M <: Model[_], T](block: (M) => T)(implicit fmt: Format[M], filter: (JsValue) => JsValue = (j: JsValue) => j) = new PlayJsonHandlers(subject) >! {
    (jsValue) =>
      block(fromJson[M](filter(jsValue)))
  }

  def >^*>[M <: Model[_], T](block: (Iterable[M]) => T)(implicit fmt: Format[M], filter: (JsValue) => Iterable[JsValue] = (j: JsValue) => Seq(j)) = new PlayJsonHandlers(subject) >! {
    (jsValue) =>
      block(filter(jsValue).map(fromJson[M](_)))
  }
}