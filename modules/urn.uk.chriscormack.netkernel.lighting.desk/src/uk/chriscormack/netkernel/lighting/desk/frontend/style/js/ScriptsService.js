var module = angular.module('ScriptsService', ['LightingAPIService', 'LightingWebSocketService']);

module.factory('ScriptsService', ['$q', '$rootScope', 'LightingAPIService', 'LightingWebSocketService', function ($q, $rootScope, LightingAPIService, LightingWebSocketService) {
	var scriptListCachedPromise = null;

	// var sequencePromiseCache = [];

	var Service = {};

	Service.getScriptList = function (forceReload) {
		if (scriptListCachedPromise === undefined || (forceReload !== undefined && forceReload)) {
			scriptListCachedPromise = LightingAPIService.getScriptList();
		}

		return scriptListCachedPromise;
	};

	Service.updateScript = function (id, details) {
		return LightingAPIService.updateScript(id, details);
	};

	Service.compileScript = function (script) {
		return LightingAPIService.compileScript(script);
	};

	Service.runScript = function (script) {
		return LightingAPIService.runScript(script);
	};

	return Service;
}]);
