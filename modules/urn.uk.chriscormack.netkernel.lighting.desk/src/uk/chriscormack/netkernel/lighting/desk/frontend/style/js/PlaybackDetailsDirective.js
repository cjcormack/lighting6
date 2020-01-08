var module = angular.module('PlaybackDetailsDirective', ['TrackService', 'LightingWebSocketService']);

module.directive('playbackDetails', function () {
	return {
		restrict: 'A',
		templateUrl: '/lighting/style/partials/playbackDetails.html',
		scope: {},
		controller: function ($scope, TrackService,LightingWebSocketService) {
			$scope.trackDetails = {};

			$scope.$parent.$watch("connected", function(n) {
				if (n) {
					var tdp = TrackService.getTrackDetails();
					tdp.then(function(data) {
						$scope.trackDetails = data;
					});
				}
			});

			var trackListener = function (trackDetails) {
				$scope.trackDetails = trackDetails;
			};

			TrackService.attachTrackListener(trackListener);

			$scope.$on('$destroy', function () {
				TrackService.detachTrackListener(trackListener);
			});
		}
	};
});