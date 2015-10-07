/*
  Base protocol handler. Gets user UUID when subsocket is ready
 */
ProtocolHandler = Base.extend({
  constructor : function(socket) {
    var self = this;
    this.base();

    this.request = {
      url: "/ws",
      logLevel: 'debug',
      contentType : "application/json",
      closeAsync: true,
      transport: 'websocket',
      fallbackTransport: 'long-polling'
    };

    this.uid = null;

    //TODO: handle protocol failure

    this.request.onOpen = function(response) {
      console.log('Atmosphere connected using ' + response.transport);
      console.log("What is our Atmosphere UID?");
      self.sendCommand({'action': 'getUID'})
    };

    this.socket = socket;
    this.subSocket = null;
  },

  onMessage: function(rs) {
    var self = this;

    console.log(rs);
    var message = rs.responseBody;
    console.log('ws -> ' + message);

    try {
      var json = jQuery.parseJSON(message);
    } catch (e) {
      console.log('This doesn\'t look like a valid JSON, bro: ', message);
    }

    if (json.uid) {
      self.uid = json.uid;
    }

    return json;
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
      var json = self.onMessage(rs);

      if (json.card && json.card.uid != self.uid) {
        var card = json.card;
        $('#' + card.listId).append(
            '<div id="card' +  card.no + '"class="card well">' +
            card.text + ' from user ' + json.card.uid + '</div>'
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
    card.uid = this.uid;

    var message = {
      'action': "addCard",
      'card': card
    };
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
    this.uid = null;

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

    var card = {
      no: this.totalTrelloCards(),
      text: 'A card with some text #' + this.totalTrelloCards(),
      listId: listId
    };
    $('#' + listId).append('<div id="card' +  card.no + '"class="card well">' +  card.text + '</div>');
    this.protocol.addCard(card);
  }
});
