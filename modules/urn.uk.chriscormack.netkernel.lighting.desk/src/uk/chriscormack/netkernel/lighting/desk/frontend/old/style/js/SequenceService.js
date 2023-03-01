var module = angular.module('SequenceService', ['LightingAPIService', 'LightingWebSocketService']);

module.factory('SequenceService', ['$q', '$rootScope', 'LightingAPIService', 'LightingWebSocketService', function($q, $rootScope, LightingAPIService, LightingWebSocketService) {
  var sequenceListCachedPromise = null;

  var sequencePromiseCache = [];

  var Service = {};

  Service.getSequenceList = function(forceReload) {
    if (sequenceListCachedPromise === undefined || (forceReload !== undefined && forceReload)) {
      sequenceListCachedPromise = LightingAPIService.getSequenceList();
    }

    return sequenceListCachedPromise;
  };

  Service.getSequence = function(id) {
    if (sequencePromiseCache[id] === undefined) {
      sequencePromiseCache[id] = LightingAPIService.getSequence(id);
    }

    return sequencePromiseCache[id];
  };

  Service.activate = function(id) {
    return LightingAPIService.activateSequence(id);
  };

  return Service;
}]);
