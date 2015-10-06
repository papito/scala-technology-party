/*
  Base protocol handler. Gets user UUID when subsocket is ready
 */
ProtocolHandler = Base.extend({
  constructor : function(socket) {
    var self = this;
    this.base();

    this.request = {
      url: "/ws",
      contentType: "text/plain",
      logLevel: 'debug',
      transport: 'websocket',
      fallbackTransport: 'long-polling'
    };

    this.uuid = null;

    this.request.onOpen = function(response) {
      console.log('Atmosphere connected using ' + response.transport);
      console.log("What is our Atmosphere UUID?");
      self.sendCommand("uuid", function() {
        self.subSocket.push(cmd);
      })
    };

    this.socket = socket;
    this.subSocket = null;
  },

  sendCommand: function(message) {
    var json = JSON.stringify(message);
    console.log('ws <- ' + json);
    this.subSocket.push(json);
  }
});
//-----------------------------------------------------------------------------

TrelloProtocolHandler = ProtocolHandler.extend({
  constructor : function(socket) {
    this.base(socket);
    var self = this;

    this.request.onReconnect = function(rq, rs) {
      self.socket.info("Reconnecting");
    };

    this.request.onMessage = function(rs) {
      console.log(rs);
      var message = rs.responseBody;
      console.log('ws -> ' + message);

      try {
        var json = jQuery.parseJSON(message);
      } catch (e) {
        console.log('This doesn\'t look like a valid JSON, bro: ', message);
      }

      if (json.uuid) {
        console.log("UUID: " + json.uuid);
        self.uuid = json.uuid;
      }

      if (json.card) {
        var card = json.card;
        $('#' + card.listId).append(
            '<div id="card' +  card.no + '"class="card well">' +
            card.text + ' from user ' + json.uuid + '</div>'
        );
      }
    };

    this.request.onClose = function(rs) {
      console.log("Closing connection")
    };

    this.request.onError = function(rs) {
      //FIXME: banner error
      console.log("Socket Error");
      console.log(rs);
    };

    this.subSocket = this.socket.subscribe(this.request);

  },

  addCard: function(card) {
    var message = {};
    message.action = "addCard";
    message.card = card;
    this.sendCommand(message);
  }
});
//-----------------------------------------------------------------------------

IndexViewModel = BaseViewModel.extend({
  constructor : function() {
    "use strict";
    var self = this;
    this.base();
    console.log('Initializing index view model');

    this.totalTrelloCards = ko.observable(0);
    this.uuid = null;

    console.log('Initialized Atmosphere');
    var socket = $.atmosphere;

    this.protocol =new TrelloProtocolHandler(socket);
    console.log('"Trello" protocol handler attached');
  },

  /*
   "TRELLO"
   */

  addNewCard: function(listId) {
    this.totalTrelloCards(this.totalTrelloCards() + 1);

    var card = {};
    card.no = this.totalTrelloCards();
    card.text = 'A card with some text #' +  card.no;
    card.listId = listId;
    $('#' + listId).append('<div id="card' +  card.no + '"class="card well">' +  card.text + '</div>');
    this.protocol.addCard(card);
  }
});
