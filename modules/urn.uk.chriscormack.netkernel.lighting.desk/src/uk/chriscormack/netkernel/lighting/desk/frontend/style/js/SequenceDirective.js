var module = angular.module('SequenceDirective', ['SequenceService']);

module.directive('sequenceDirective', function() {
  return {
    restrict: 'A',
    templateUrl: '/lighting/style/partials/sequence.html',
    scope: {
      sequenceId: "=sequenceid"
    },
    controller: function($scope, SequenceService) {
      $scope.sequence = null;

      $scope.activate = function() {
        SequenceService.activate($scope.sequenceId);
      };

      SequenceService.getSequence($scope.sequenceId).then(function(sequence) {
        $scope.sequence = sequence;
      });
    }
  };
});