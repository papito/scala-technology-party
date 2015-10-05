IndexViewModel = BaseViewModel.extend({
  constructor : function() {
    "use strict";
    this.base();
    console.log('Initializing index view model');

    this.totalTrelloCards = ko.observable(1);
  },

  /*
   "TRELLO"
   */

  addNewCard: function(listId) {
    var cardNo = this.totalTrelloCards();
    $('#' + listId).append('<div id="card' + cardNo + '"class="card well">A card with some text #' + cardNo + '</div>');
    this.totalTrelloCards(cardNo + 1);
  }
});
