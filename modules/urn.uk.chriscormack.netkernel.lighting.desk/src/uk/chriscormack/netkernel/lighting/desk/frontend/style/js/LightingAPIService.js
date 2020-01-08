var module = angular.module('LightingAPIService', ['ngResource']);

module.factory('LightingAPIService', ['$q', '$resource', function ($q, $resource) {
	var Service = {};

	Service.getFixtureList = function () {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/data/fixtures/list.json');


		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response.fixtures);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.getFixture = function (name) {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/data/fixtures/:name.json', {}, {
			get: {method: 'GET', params: {name: name}}
		});

		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.getFixtureType = function (fixtureType) {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/data/fixtureTypes/:fixtureType.json', {}, {
			get: {method: 'GET', params: {fixtureType: fixtureType}}
		});

		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.getSequenceList = function () {
		var deferred = $q.defer();

		// var resReq = $resource('/lighting/rest/sequences/list');
		//
		// resReq.get().$promise.then(
		// function (response) {
		// 	deferred.resolve(response.sequences);
		// }, function (error) {
		// 	deferred.reject(error);
		// }
		// );

		deferred.resolve([]);

		return deferred.promise;
	};

	Service.getSequence = function (id) {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/rest/sequences/:id/details', {}, {
			get: {method: 'GET', params: {id: id}}
		});

		resReq.get().$promise.then(
		function (response) {
			console.log(response);
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.activateSequence = function (id) {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/rest/sequences/:id/activate', {}, {
			get: {method: 'POST', params: {id: id}}
		});

		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.clearActiveSequence = function () {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/rest/sequences/clear', {}, {
			get: {method: 'POST'}
		});

		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.setTempo = function (tempo) {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/rest/sequences/setTempo', {}, {
			get: {method: 'POST', params: {tempo: tempo}}
		});

		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.pauseSequence = function () {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/rest/sequences/pause', {}, {
			get: {method: 'POST'}
		});

		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	Service.pauseSequence = function () {
		var deferred = $q.defer();

		var resReq = $resource('/lighting/rest/sequences/play', {}, {
			get: {method: 'POST'}
		});

		resReq.get().$promise.then(
		function (response) {
			deferred.resolve(response);
		}, function (error) {
			deferred.reject(error);
		}
		);

		return deferred.promise;
	};

	return Service;
}]);
