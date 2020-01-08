var module = angular.module('ModeSelectorDirective', ['ChannelsService']);

module.directive('modeSelector', function() {
  return {
    restrict: 'A',
    templateUrl: '/lighting/style/partials/modeSelector.html',
    scope: {
      modeSelector: "=modeselector",
      channel:      "=channel"
    },
    controller: function($scope, ChannelsService) {

      var currentModeLevel = ChannelsService.currentChannelLevel($scope.channel) || 0;
      $scope.currentActive = null;

      $scope.selectMode = function(mode) {
        ChannelsService.updateChannel($scope.channel, mode.channelLevel);
      };

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
        var currentSelection = null;
        $scope.modeSelector.modes.forEach(function(mode) {
          if (mode.channelLevel <= currentModeLevel) {
            if (currentSelection == null || mode.channelLevel >= currentSelection.channelLevel) {
              currentSelection = mode;
            }
          }
        });
        $scope.currentActive = currentSelection;
      }
    }
  };
});