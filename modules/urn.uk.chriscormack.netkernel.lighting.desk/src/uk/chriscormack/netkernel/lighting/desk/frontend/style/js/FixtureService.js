var module = angular.module('FixtureService', ['LightingAPIService']);

module.factory('FixtureService', ['$q', '$rootScope', 'LightingAPIService', function($q, $rootScope, LightingAPIService) {
  var fixtureListCachedPromise = null;

  var fixturePromiseCache = [];
  var fixtureTypePromiseCache = [];

  var Service = {};

  Service.getFixtureList = function(forceReload) {
    if (fixtureListCachedPromise === undefined || (forceReload !== undefined && forceReload)) {
      fixtureListCachedPromise = LightingAPIService.getFixtureList();
    }

    return fixtureListCachedPromise;
  };

  Service.getFixture = function(fixtureName) {
    if (fixturePromiseCache[fixtureName] === undefined) {
      fixturePromiseCache[fixtureName] = LightingAPIService.getFixture(fixtureName);
    }

    return fixturePromiseCache[fixtureName];
  };

  Service.getFixtureType = function(fixtureType) {
    if (fixtureTypePromiseCache[fixtureType] === undefined) {
      fixtureTypePromiseCache[fixtureType] = LightingAPIService.getFixtureType(fixtureType);
    }

    return fixtureTypePromiseCache[fixtureType];
  };

  return Service;
}]);
