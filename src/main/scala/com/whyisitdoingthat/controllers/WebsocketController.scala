package com.whyisitdoingthat.controllers

import org.json4s.JsonAST.{JNull, JField, JString, JObject}
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
      private def uuidJson: JObject = "uid" -> uuid

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
        case message @ JsonMessage(JObject(JField("action", JString("getUID")) :: fields)) => {
          val json: JValue = message.content
          log.info(s"WS <- $json")
          this.writeToYou(uuidJson)
        }

        // add "trello" card
        case message @ JsonMessage(JObject(JField("action", JString("addCard")) :: fields)) => {
          val json: JValue = message.content
          log.info(s"WS <- $json")

          val cardJson: JValue = json findField {
            case JField("card", _) => true
            case _ => false
          }

          this.writeToAll(cardJson)
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
