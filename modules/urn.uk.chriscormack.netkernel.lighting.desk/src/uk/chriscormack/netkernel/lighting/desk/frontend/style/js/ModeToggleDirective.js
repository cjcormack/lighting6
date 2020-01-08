var module = angular.module('ModeToggleDirective', ['ChannelsService']);

module.directive('modeToggle', function() {
  return {
    restrict: 'A',
    templateUrl: '/lighting/style/partials/modeToggle.html',
    scope: {
      modeToggle: "=modetoggle",
      channel:    "=channel"
    },
    controller: function($scope, ChannelsService) {
      var currentModeLevel = ChannelsService.currentChannelLevel($scope.channel) || 0;

      $scope.modeToggle.onLevel = $scope.modeToggle.onLevel || 255;
      $scope.modeToggle.offLevel = $scope.modeToggle.offLevel || 0;

      $scope.currentActive = false;

      $scope.toggle = function() {
        if ($scope.currentActive) {
          ChannelsService.updateChannel($scope.channel, $scope.modeToggle.offLevel);
        } else {
          ChannelsService.updateChannel($scope.channel, $scope.modeToggle.onLevel);
        }
      }

      updateCurrentMode();

      var channelListener = function(newLevel){
        currentModeLevel = newLevel;
        updateCurrentMode();
      };

      ChannelsService.attachChannelListener($scope.channel, channelListener);

      $scope.$on('$destroy', function() {
        ChannelsService.detachChannelListener($scope.channel, channelListener);
      });

      function updateCurrentMode() {
        $scope.currentActive = (currentModeLevel > $scope.modeToggle.offLevel);
      }
    }
  };
});