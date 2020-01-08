var module = angular.module('ColourSelectorDirective', ['ChannelsService']);

module.directive('colourSelector', function() {
  return {
    restrict: 'A',
    templateUrl: '/lighting/style/partials/colourSelector.html',
    scope: {
      redChannel:   "=redchannel",
      greenChannel: "=greenchannel",
      blueChannel:  "=bluechannel"
    },
    controller: function($scope, ChannelsService) {
      $scope.currentColour = "#ffffff";

      var currentRedLevel   = ChannelsService.currentChannelLevel($scope.redChannel)   || 0;
      var currentGreenLevel = ChannelsService.currentChannelLevel($scope.greenChannel) || 0;
      var currentBlueLevel  = ChannelsService.currentChannelLevel($scope.blueChannel)  || 0;

      updateCurrentMode();

      var redListener = function(newLevel){
        currentRedLevel = newLevel;
        updateCurrentMode();
      };
      var greenListener = function(newLevel){
        currentGreenLevel = newLevel;
        updateCurrentMode();
      };
      var blueListener = function(newLevel){
        currentBlueLevel = newLevel;
        updateCurrentMode();
      };

      ChannelsService.attachChannelListener($scope.redChannel, redListener);
      ChannelsService.attachChannelListener($scope.greenChannel, greenListener);
      ChannelsService.attachChannelListener($scope.blueChannel, blueListener);

      $scope.$on('$destroy', function() {
        ChannelsService.detachChannelListener($scope.redChannel, redListener);
        ChannelsService.detachChannelListener($scope.greenChannel, greenListener);
        ChannelsService.detachChannelListener($scope.blueChannel, blueListener);
      });

      function updateCurrentMode() {
        $scope.currentColour = rgbToHex(currentRedLevel, currentGreenLevel, currentBlueLevel);
      }

      function componentToHex(c) {
        var hex = c.toString(16);
        return hex.length == 1 ? "0" + hex : hex;
      }

      function rgbToHex(r, g, b) {
        return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
      }
    }
  };
});