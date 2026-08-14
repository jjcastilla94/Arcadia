(function (root) {
  'use strict';

  var MSG_SAVE = 'ARCADIA_SAVE';
  var MSG_GET_SAVE = 'ARCADIA_GET_SAVE';
  var RESPONSE_TYPES = ['ARCADIA_SAVE_RESPONSE', 'ARCADIA_GET_SAVE_RESPONSE'];

  var counter = 0;
  var pending = {};

  function nextId() {
    counter += 1;
    return 'arcadia-' + Date.now() + '-' + counter;
  }

  function send(message) {
    return new Promise(function (resolve, reject) {
      var messageId = nextId();
      var timer = setTimeout(function () {
        delete pending[messageId];
        reject(new Error('Arcadia: no response from player'));
      }, 10000);

      pending[messageId] = {
        resolve: resolve,
        reject: reject,
        timer: timer
      };

      root.parent.postMessage(Object.assign({}, message, { messageId: messageId }), root.location.origin);
    });
  }

  root.addEventListener('message', function (event) {
    var message = event.data;
    if (!message || typeof message !== 'object') return;
    if (RESPONSE_TYPES.indexOf(message.type) === -1) return;
    var entry = pending[message.messageId];
    if (!entry) return;

    clearTimeout(entry.timer);
    delete pending[message.messageId];

    if (message.success) {
      entry.resolve(message.data === undefined ? null : message.data);
    } else {
      entry.reject(new Error(message.error || 'Arcadia: operation failed'));
    }
  });

  var Arcadia = {
    save: function (data) {
      return send({ type: MSG_SAVE, data: data });
    },
    getSave: function () {
      return send({ type: MSG_GET_SAVE });
    }
  };

  root.Arcadia = Arcadia;
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = Arcadia;
  }
})(typeof window !== 'undefined' ? window : this);
