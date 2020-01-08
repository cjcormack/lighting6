var module = angular.module('SequenceStepDirective', ['SequenceService', 'LightingWebSocketService']);

module.directive('sequenceStep', function() {
  return {
    restrict: 'A',
    templateUrl: '/lighting/style/partials/sequenceStep.html',
    scope: {
      sequenceName: "=sequencename",
      fixtureName: "=fixturename",
      stepNo: "=stepno",
      step: "=step"
    },
    controller: function($scope, SequenceService) {

    }
  };
});