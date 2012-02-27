package models

import scala.collection.mutable
import play.api.libs.json.Format
import reflect.ClassManifest
import utils.persistence.graph

/**
 * User: andy
 * Date: 2/02/12
 */
abstract class Model[A <: Model[A]] {
  val id:Int;

  def save(implicit m:ClassManifest[A], f:Format[A]):A = graph.saveNode[A](this.asInstanceOf[A])

  def index(indexName:String, key:String, value:String)(implicit m:ClassManifest[A], f:Format[A]) {
    graph.indexNode[A](this.asInstanceOf[A], indexName, key, value)
  }

}

object Model {
  val models:mutable.Map[String, ClassManifest[_ <: Model[_]]] = new mutable.HashMap[String, ClassManifest[_ <: Model[_]]]()

  def register[T <: Model[_]](kind:String)(implicit m:ClassManifest[T]) {
    models.put(kind, m)
  }
  
  def all[T <: Model[_]](implicit m:ClassManifest[T], f:Format[T]) = graph.allNodes[T]
  def one[T <: Model[_]](id:Int)(implicit m:ClassManifest[T], f:Format[T]) = graph.getNode[T](id)

  def kindOf[T <: Model[_]] (implicit m:ClassManifest[T]):String = models.find(_._2.equals(m)).get._1
}