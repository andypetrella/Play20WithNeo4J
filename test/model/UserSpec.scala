package model

import org.specs2.Specification
import models.User
import utils.persistence.graph
import play.api.test._
import play.api.test.Helpers._

class UserSpec extends Specification {
  var userId:Int = 0;
  
  def is =
    "Persist User" ^ {
      "is save with an id" ! {
        running(FakeApplication()) {
          val user:User = User(null.asInstanceOf[Int], "I'm you")
          val saved: User = graph.saveNode(user)
          userId = saved.id
          saved.id must beGreaterThanOrEqualTo(0)
        }
      } ^
        "can e retrieved easily" ! {
          running(FakeApplication()) {
            val retrieved = graph.getNode[User](userId)
            retrieved must beSome[User]
          }
        }
    }

}
