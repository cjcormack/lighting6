var module = angular.module('ChannelSliderDirective', ['ChannelsService', 'TrackService']);

module.directive('channelSlider', function () {
	return {
		restrict: 'E',
		templateUrl: '/lighting/style/partials/channelSlider.html',
		scope: {
			channelid: '=channelid',
			maxlevel: '=?maxlevel',
			minlevel: '=?minlevel',
			enabled: '=?enabled',
			reverse: '=?reverse'
		},
		controller: function ($scope, ChannelsService, TrackService) {
			$scope.currentLevel = ChannelsService.currentChannelLevel($scope.channelid) || 0;
			$scope.minlevel = $scope.minlevel || 0;
			$scope.maxlevel = $scope.maxlevel || 255;

			if ($scope.enabled === undefined) {
				$scope.enabled = true;
			}

			if ($scope.reverse) {
				$scope.currentLevel = $scope.maxlevel - $scope.currentLevel;
			}

			var lastSetValue = 0;

			var channelListener = function (newLevel) {

				if ($scope.reverse) {
					newLevel = $scope.maxlevel - newLevel;
				} else {
				}
				$scope.currentLevel = newLevel;
				lastSetValue = newLevel;
			};

			ChannelsService.attachChannelListener($scope.channelid, channelListener);

			$scope.$watch("currentLevel", function (n, o) {
				if (n === lastSetValue) {
					lastSetValue = -1;
					return;
				}

				if (n != o) {
					lastSetValue = -1;
					if ($scope.reverse) {
						ChannelsService.updateChannel($scope.channelid, $scope.maxlevel - $scope.currentLevel);
					} else {
						ChannelsService.updateChannel($scope.channelid, $scope.currentLevel);
					}
				}
			});

			$scope.$on('$destroy', function () {
				ChannelsService.detachChannelListener($scope.channelid, channelListener);
			});
		}
	};
});
