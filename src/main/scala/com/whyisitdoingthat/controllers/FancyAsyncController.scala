package com.whyisitdoingthat.controllers

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.SessionSupport
import org.scalatra._
import org.scalatra.json.{JacksonJsonSupport, JValueResult}
import org.slf4j.LoggerFactory
import org.scalatra.atmosphere._

import scala.concurrent.ExecutionContext.Implicits.global

class FancyAsyncController extends ScalatraServlet with JValueResult with JacksonJsonSupport with SessionSupport with AtmosphereSupport   {
  private final val log = LoggerFactory.getLogger(getClass)

  implicit protected val jsonFormats: Formats = DefaultFormats

  atmosphere("/") {
    new AtmosphereClient {
      def receive: AtmoReceive = {
        case TextMessage(data: String) => {
          log.info(s"WS -> $data")
          log.info(s"WS <- $data")
          this.send(data)
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
