var module = angular.module('PatternSelectorDirective', ['ChannelsService']);

module.directive('patternSelector', function() {
  return {
    restrict: 'A',
    templateUrl: '/lighting/style/partials/patternSelector.html',
    scope: {
      groupChannel: "=groupchannel",
      patternChannel: "=patternchannel",
      patterns: '=patterns'
    },
    controller: function($scope, ChannelsService) {
      var currentGroupLevel = ChannelsService.currentChannelLevel($scope.groupChannel) || 0;
      var currentPatternLevel = ChannelsService.currentChannelLevel($scope.patternChannel) || 0;

      $scope.currentPattern = 'NONE';

      $scope.selectPattern = function(pattern) {
        ChannelsService.updateChannel($scope.groupChannel, pattern.groupLevel);
        ChannelsService.updateChannel($scope.patternChannel, pattern.patternLevel);
      }

      calculateCurrentPattern();

      var groupListener = function(newLevel){
        currentGroupLevel = newLevel;
        calculateCurrentPattern();
      };
      var patternListener = function(newLevel){
        currentPatternLevel = newLevel;
        calculateCurrentPattern();
      };
      ChannelsService.attachChannelListener($scope.groupChannel, groupListener);
      ChannelsService.attachChannelListener($scope.patternChannel, patternListener);

      $scope.$on('$destroy', function() {
        ChannelsService.detachChannelListener($scope.groupChannel, groupListener);
        ChannelsService.detachChannelListener($scope.patternChannel, patternListener);
      });

      function calculateCurrentPattern() {
        var currentSelection = null;
        $scope.patterns.forEach(function(pattern) {
          if (pattern.groupLevel <= currentGroupLevel && pattern.patternLevel <= currentPatternLevel) {
            if (currentSelection == null ||
                    (pattern.groupLevel >= currentSelection.groupLevel && pattern.patternLevel >= currentSelection.patternLevel)) {
              currentSelection = pattern;
            }
          }
        });
        if (currentSelection == null) {
          $scope.currentPattern = 'NONE';
        } else {
          $scope.currentPattern = currentSelection.name;
        }
      }
    }
  };
});