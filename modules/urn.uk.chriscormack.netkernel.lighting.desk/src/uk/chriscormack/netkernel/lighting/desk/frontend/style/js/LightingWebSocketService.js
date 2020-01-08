var module = angular.module('LightingWebSocketService', []);

module.factory('LightingWebSocketService', ['$q', '$rootScope', function ($q, $rootScope) {
	var Service = {};

	var socketListeners = {};
	var connectionListeners = [];

	var currentChannelStatePromise = null;
	var trackDetailsPromise = null;

	var wsAddress = "ws://" + window.location.href.split("/")[2] + "/lighting/";

	function connectToWebSocket() {
		var newWs = new WebSocket(wsAddress);

		newWs.onopen = function () {
			for (var i = 0; i < connectionListeners.length; i++) {
				$rootScope.$apply(connectionListeners[i]('open'));
			}
		};

		newWs.onclose = function () {
			for (var i = 0; i < connectionListeners.length; i++) {
				$rootScope.$apply(connectionListeners[i]('close'));
			}
		}

		newWs.onerror = function () {
			for (var i = 0; i < connectionListeners.length; i++) {
				$rootScope.$apply(connectionListeners[i]('error'));
			}
		}

		newWs.onmessage = function (message) {
			listener(JSON.parse(message.data));
		};

		return newWs;
	}

	var ws = connectToWebSocket();

	function sendRequest(request) {
		ws.send(JSON.stringify(request));
	}

	function listener(messageObj) {
		if (messageObj.type == 'channelState') {
			if (currentChannelStatePromise != null) {
				currentChannelStatePromise.resolve(messageObj.data.channels);

				currentChannelStatePromise = null;
			}
		} else if (messageObj.type == 'trackDetails') {
			if (trackDetailsPromise != null) {

				trackDetailsPromise.resolve(messageObj.data);

				trackDetailsPromise = null;
			}
		} else if (socketListeners.hasOwnProperty(messageObj.type)) {
			var arr = socketListeners[messageObj.type];
			for (var i = 0; i < arr.length; i++) {
				$rootScope.$apply(arr[i](messageObj.data));
			}
		}
	}

	Service.sendRequest = function (type, data) {
		sendRequest({type: type, data: data});
	};

	Service.trackDetails = function () {
		var deferred = $q.defer();

		trackDetailsPromise = deferred;
		sendRequest({type: 'trackDetails'});

		setTimeout(function () {
			if (trackDetailsPromise != null) {
				trackDetailsPromise.reject('timeout');
				trackDetailsPromise = null;
			}
		}, 15000);

		return deferred.promise;
	};

	Service.channelState = function () {
		var deferred = $q.defer();

		currentChannelStatePromise = deferred;
		sendRequest({type: 'channelState'});

		setTimeout(function () {
			if (currentChannelStatePromise != null) {
				currentChannelStatePromise.reject('timeout');
				currentChannelStatePromise = null;
			}
		}, 15000);

		return deferred.promise;
	};

	Service.updateChannel = function (id, level) {
		sendRequest({type: 'updateChannel', data: {channel: {id: id, level: level, fadeTime: 0}}});
	};

	Service.setFixtureEnabled = function (name, enabled) {
		sendRequest({type: 'fixtureSetEnabled', data: {name: name, enabled: enabled}});
	};


	Service.attachSocketListener = function (type, callback) {
		if (!socketListeners.hasOwnProperty(type)) {
			socketListeners[type] = [];
		}
		socketListeners[type].push(callback);
	};

	Service.reconnect = function () {
		if (ws.readyState == WebSocket.CLOSED) {
			ws = connectToWebSocket();
		}
	}

	Service.attachConnectionListener = function (callback) {
		connectionListeners.push(callback);
	};

	setInterval(function () {
		if (ws.readyState == WebSocket.OPEN) {
			sendRequest({type: "ping"});
		}
	}, 10000);

	return Service;
}]);