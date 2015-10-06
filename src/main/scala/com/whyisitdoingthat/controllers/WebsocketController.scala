package com.whyisitdoingthat.controllers

import org.json4s.JsonAST.{JField, JString, JObject}
import org.json4s.{JsonDSL, JValue, DefaultFormats, Formats}
import org.scalatra.SessionSupport
import org.scalatra._
import org.scalatra.json.{JacksonJsonSupport, JValueResult}
import org.slf4j.LoggerFactory
import org.scalatra.atmosphere._
import JsonDSL._

import scala.concurrent.ExecutionContext.Implicits.global

class WebsocketController extends ScalatraServlet with JValueResult with JacksonJsonSupport with SessionSupport with AtmosphereSupport   {
  private final val log = LoggerFactory.getLogger(getClass)

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere("/") {
    new AtmosphereClient {
      private def uuidJson: JValue = compact(render("uuid" -> uuid))

      private def writeToYou(jsonMessage: JValue): Unit = {
        log.info(s"WS (you) -> $jsonMessage")
        this.send(jsonMessage)
      }

      private def writeToAll(jsonMessage: JValue): Unit = {
        val jsonOut = jsonMessage merge uuidJson
        log.info(s"WS (ALL) -> $jsonMessage")
        log.info(s"WS (ALL) -> $uuidJson")
        log.info(s"WS (ALL) -> $jsonOut")
        this.broadcast(jsonOut)
      }

      override def receive: AtmoReceive = {
        case TextMessage("uuid") => {
          log.info(s"WS <- uuid")
          this.writeToYou(uuidJson)
        }

        case j @ JsonMessage(JObject(JField("action", JString("addCard")) :: fields)) => {
          log.info(s"WS <- ${j.content}")
          this.writeToAll(j.content)
        }

        case Connected =>
          log.info("Client connected")

        case Disconnected(disconnector, Some(error)) =>
          log.info("Client disconnected ")

        case Error(Some(error)) =>
          // FIXME - what is the difference with the hanler "error" handler?
          error.printStackTrace()
      }
    }
  }
}
