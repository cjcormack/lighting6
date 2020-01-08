var module = angular.module('TrackService', ['LightingAPIService', 'LightingWebSocketService']);

module.factory('TrackService', ['$q', '$rootScope', 'LightingAPIService', 'LightingWebSocketService', function ($q, $rootScope, LightingAPIService, LightingWebSocketService) {
	var trackCachedPromise = null;

	var trackListeners = [];

	var currentTrackDetails = null;

	LightingWebSocketService.attachSocketListener('uT', function (data) {
		$rootScope.$apply(function () {
			notifyTrackLister(data);
		});
	});

	function notifyTrackLister(data) {
		currentTrackDetails = data;

		var arr = trackListeners;
		for (var i = 0; i < arr.length; i++) {
			arr[i](data);
		}
	}

	var Service = {};

	Service.getTrackDetails = function (forceReload) {
		if (!trackCachedPromise || forceReload) {
			trackCachedPromise = LightingWebSocketService.trackDetails();

			trackCachedPromise.then(function (trackDetails) {
				currentTrackDetails = trackDetails;
			});
		}

		return trackCachedPromise;
	};

	Service.attachTrackListener = function (callback) {
		trackListeners.push(callback);
	};
	Service.detachTrackListener = function (callback) {
			var index = trackListeners.indexOf(callback);

			if (index > -1) {
				trackListeners.splice(index, 1);
			}
	};

	Service.currentTrackDetails = function () {
		return currentTrackDetails;
	};

	return Service;
}]);
