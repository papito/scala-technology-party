ProtocolHandler = Base.extend({
  constructor : function(subSocket) {
    this.base();
    this.subSocket = subSocket;
  },

  sendCommand: function(cmd, handler) {
    console.log('ws < ' + cmd);
    this.subSocket.push(cmd);
  }
});

TrelloProtocolHandler = ProtocolHandler.extend({
  constructor : function(subSocket) {
    this.base(subSocket);
  },

  addCard: function(card) {
    this.sendCommand(JSON.stringify(card));
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

    this.socket = $.atmosphere;
    this.socketReady = false;

    this.request = {
      url: "/ws",
      contentType: "text/plain",
      logLevel: 'debug',
      transport: 'websocket',
      fallbackTransport: 'long-polling'
    };
    console.log('Initialized Atmosphere');

    this.request.onOpen = function(response) {
      console.log('Atmosphere connected using ' + response.transport);
      self.socketReady = true;
    };

    this.request.onReconnect = function(rq, rs) {
      self.socket.info("Reconnecting");
    };

    this.request.onMessage = function(rs) {
      console.log(rs);
      var message = rs.responseBody;
      console.log('ws > ' + message);

      try {
        var json = jQuery.parseJSON(message);
      } catch (e) {
        console.log('This doesn\'t look like a valid JSON object: ', message);
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

    this.subSocket = self.socket.subscribe(self.request);
  },

  /*
   "TRELLO"
   */

  addNewCard: function(listId) {
    this.totalTrelloCards(this.totalTrelloCards() + 1);

    var card = {};
    card.no = this.totalTrelloCards();
    card.text = 'A card with some text #' +  card.no;
    $('#' + listId).append('<div id="card' +  card.no + '"class="card well">' +  card.text + '</div>');

    var protocol = new TrelloProtocolHandler(this.subSocket);
    protocol.addCard(card);
  }
});
