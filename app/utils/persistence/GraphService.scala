package utils.persistence

import play.api.libs.json.Format

/**
 * User: andy
 * Date: 4/02/12
 */

trait GraphService[Node] {

  def root: Node

  def getNode[T <: Node](id: Int)(implicit m: ClassManifest[T], f: Format[T]): Option[T]

  def allNodes[T <: Node](implicit m: ClassManifest[T], f: Format[T]): List[T]

  def relationTargets[T <: Node](start: Node, rel: String)(implicit m: ClassManifest[T], f: Format[T]): List[Node]

  def findNodeByExactMatch[T <: Node](indexName: String, key: String, value: String)(implicit m: ClassManifest[T], f: Format[T]): Seq[T]


  def saveNode[T <: Node](t: T)(implicit m: ClassManifest[T], f: Format[T]): T

  def indexNode[T <: Node](model: T, indexName: String, key: String, value: String)

  def createRelationship(start: Node, rel: String, end: Node)
}