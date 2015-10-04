package com.whyisitdoingthat.controllers

class IndexController extends ScalaTechnologyPartyStack {

  before() {
    contentType = "text/html"
  }

  get("/") {
    ssp("index", "layout" -> "")
  }
}
