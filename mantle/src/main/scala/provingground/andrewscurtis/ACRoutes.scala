package provingground.andrewscurtis

import provingground._
import FreeGroups._

import upickle.default.{write => uwrite, read => uread, _}

import akka.actor._

import akka.http._
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import ACFlow.{system, mat}

import ACMongo._

import ACBatch._

import StartData._

import Moves._

object ACRoutes {
  val thmEvolve  = path("theorem-evolution" / Segment / Segment){
    case (name, ppress) => {
      val pres = uread[Presentation](ppress)
      val thms = thmWeights(pres, name) map (uwrite[Stream[ACThm]])
      complete(thms)
    }
  }

  val thms = path("theorems" / Segment){name =>
    val thmsOptFut = getFutOptThms(name)
    val thms = thmsOptFut mapp (uwrite[FiniteDistribution[Presentation]])
    complete(thms)
  }

  val terms = path("terms" / Segment){name =>
    val thmsOptFDV = getFutOptFDV(name)
    val thms = thmsOptFDV mapp (uwrite[FiniteDistribution[Moves]])
    complete(thms)
  }

  val moveWeights = path("move-weights" / Segment){name =>
    val thmsOptFDM = getFutOptFDM(name)
    val thms = thmsOptFDM mapp (uwrite[FiniteDistribution[AtomicMove]])
    complete(thms)
  }

  val actors = path("actors"){
    val actors = getFutActors() map (uwrite[Vector[String]])
    complete(actors)
  }
  
  val getData = get {
    pathPrefix("data") (thms ~ thmEvolve ~ terms ~ moveWeights)
  }

  val start =
    post {
      path("start"){
        entity(as[String]) {d =>
          val startData = uread[StartData](d)
          val ref = startData.run
          complete(startData.name)
        }
      }
    }

  val quickstart =
    post {
      path("quickstart"){
        val ds = loadStartData()
        ds map (_.run)
        val names = ds map (_.name)
        complete(uwrite(names))
      }
  }
  
  val stop =
    post {
      path("stop"){
        FDhub.stop
        complete("stop requested")
      }
  }

  val acRoutes = pathPrefix("andrews-curtis")(getData ~ start ~ quickstart ~ stop)
}