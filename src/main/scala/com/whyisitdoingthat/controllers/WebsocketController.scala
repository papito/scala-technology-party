package com.whyisitdoingthat.controllers

import java.util.concurrent.Executors

import org.json4s.JsonAST.{JNull, JField, JString, JObject}
import org.json4s.{JsonDSL, JValue, DefaultFormats, Formats}
import org.scalatra.SessionSupport
import org.scalatra._
import org.scalatra.json.{JacksonJsonSupport, JValueResult}
import org.slf4j.LoggerFactory
import org.scalatra.atmosphere._
import JsonDSL._
import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{Success, Failure}

class WebsocketController extends ScalatraServlet with JValueResult with JacksonJsonSupport with SessionSupport with AtmosphereSupport   {
  private final val log = LoggerFactory.getLogger(getClass)

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere("/") {
    new AtmosphereClient {
      private def uuidJson: JObject = "uid" -> uuid
      private var abortFutures = false
      private val rand = scala.util.Random

      private def writeToYou(jsonMessage: JValue): Unit = {
        log.info(s"YOU -> $jsonMessage")
        this.send(jsonMessage)
      }

      private def writeToAll(jsonMessage: JValue): Unit = {
        log.info(s"ALL -> $jsonMessage")
        this.broadcast(jsonMessage, Everyone)
      }

      private def writeToRest(jsonMessage: JValue): Unit = {
        log.info(s"REST -> $jsonMessage")
        this.broadcast(jsonMessage)
      }

      override def receive: AtmoReceive = {
        case message @ JsonMessage(JObject(JField("action", JString("getUID")) :: _)) => {
          val json: JValue = message.content
          log.info(s"WS <- $json")
          this.writeToYou(uuidJson)
        }

        // add "trello" card
        case message @ JsonMessage(JObject(JField("action", JString("addCard")) :: _)) => {
          val json: JValue = message.content
          log.info(s"WS <- $json")

          val cardJson: JValue = json findField {
            case JField("card", _) => true
            case _ => false
          }

          this.writeToAll(cardJson)
        }

        // workers
        case message @ JsonMessage(JObject(JField("action", JString("startWorkers")) :: _)) => {
          val json: JValue = message.content
          log.info(s"WS <- $json")

          // start the party in a future, to keep this thread free to accept requests
          Future {
            startFuturesParty()
          }

          this.writeToYou("workersStarted" -> true)
        }

        case message @ JsonMessage(JObject(JField("action", JString("stopWorkers")) :: _)) => {
          val json: JValue = message.content
          log.info(s"WS <- $json")
          abortFutures = true
          this.writeToYou("workersStarted" -> false)
        }

        // unknown
        case message @ JsonMessage(AnyRef) => {
          val json: JValue = message.content
          log.info(s"WS <- $json")
          throw new Exception("Unknown JSON command")
        }

        case Connected =>
          log.info("Client connected")

        case Disconnected(_, Some(_)) =>
          log.info("Client disconnected ")

        case Error(Some(error)) =>
          error.printStackTrace()
      }

      private def startFuturesParty(): Unit = {
        abortFutures = false

        val numWorkers = sys.runtime.availableProcessors()
        val pool = Executors.newFixedThreadPool(numWorkers)
        implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(pool)

        // Create futures equal to the size of the thread pool.
        for (idx <- 1 to numWorkers) {
          inviteFutureToTheParty(idx)
        }
      }

      private def inviteFutureToTheParty(idx: Int): Future[JValue] = {
        val f: Future[JValue] = Future {
            log.info(s"Starting work on thread ${idx}")
            Thread.sleep(5000)
          log.info(s"Finished work on thread ${idx}")
           "status" -> "success"
        }

        f onComplete {
          case Success(json: JValue) => {
            this.writeToYou(json)
            if (!abortFutures) inviteFutureToTheParty(idx)
          }
          case Failure(t) => {
            log.error(t.getClass.getName)
            this.writeToYou("file" -> ("error" -> t.getClass.getName))
            if (!abortFutures) inviteFutureToTheParty(idx)
          }
        }

        f
      }
    }
  }
}
