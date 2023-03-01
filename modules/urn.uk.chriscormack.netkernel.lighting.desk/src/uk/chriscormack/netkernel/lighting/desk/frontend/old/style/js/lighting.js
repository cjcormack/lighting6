var lightingApp = angular.module('lightingApp', ['ChannelSliderDirective', 'PatternSelectorDirective', 'ChannelGroupDirective', 'ColourSelectorDirective', 'ModeSelectorDirective', 'ModeToggleDirective', 'LightingWebSocketService', 'ChannelsService', 'FixtureService', 'FixtureDirective', 'SequenceService', 'SequenceDirective', 'SequenceStepDirective', 'TrackService', 'PlaybackDetailsDirective', 'ScriptsDirective', 'ScriptsService', 'AceEditor', 'semantic-ui']);

lightingApp.controller('WebSocketManagerController', function($scope, LightingWebSocketService, ChannelsService, FixtureService, SequenceService) {
  $scope.currentView = 'scripts';

  $scope.currentWebSocketStatus = 'striped';
  $scope.canReconnect = false;
  $scope.connected = false;

  $scope.channels = [];
  $scope.channelsInPages = [];
  $scope.fixtures = [];
  $scope.sequences = [];

  $scope.reconnect = function(){
    LightingWebSocketService.reconnect();
    $scope.canReconnect = false;
  };

  $scope.blackout = function() {
    LightingWebSocketService.sendRequest('blackout');
  };
  $scope.unblackout = function() {
    LightingWebSocketService.sendRequest('unblackout');
  };

  $scope.sequenceBeat = function() {
    SequenceService.sequenceBeat();
  };

  LightingWebSocketService.attachConnectionListener(function(status) {
    if (status == 'open') {
      $scope.currentWebSocketStatus = 'successful';
      $scope.canReconnect = false;
      $scope.connected = true;
      loadInitialState();
    } else if (status == 'close' || status == 'error') {
      $scope.currentWebSocketStatus = 'failed';
      $scope.canReconnect = true;
      $scope.connected = false;
      $scope.channels = [];
      $scope.fixtures = [];
      $scope.channelsInPages = [];
    } else {
      $scope.currentWebSocketStatus = 'striped';
      $scope.canReconnect = false;
      $scope.connected = false;
      $scope.channels = [];
      $scope.fixtures = [];
      $scope.channelsInPages = [];
    }

  });

  function loadInitialState() {
    ChannelsService.getChannels(true).then(
      function(channels) {
        $scope.channels = channels;
        $scope.channelsInPages = channelsIntoPages(channels, 8);

        FixtureService.getFixtureList(true).then(
          function(fixtures) {
            $scope.fixtures = fixtures;
          }, function(error) {
            console.error('Could not load fixtures', error);
          }
        );

        SequenceService.getSequenceList(true).then(
          function(sequences) {
            $scope.sequences = sequences;
          }, function(error) {
            console.error('Could not load sequences', error);
          }
        );
      }, function(error) {
        console.error('Could not load channels', error);
      }
    );
  }

  function channelsIntoPages(channels, channelsPerPage) {
    var fullPageCount = Math.floor(channels.length / channelsPerPage);
    var finalPageChannels = channels.length % channelsPerPage;

    var pages = [];

    for (var i = 0; i < fullPageCount; i++) {
      var page = [];

      for (var j = 0; j < channelsPerPage; j++) {
        var channel = channels[i * channelsPerPage + j];
        page.push(channel);
      }

      pages.push(page);
    }

    if (finalPageChannels > 0) {
      var finalPage = [];
      for (var i = 0; i < finalPageChannels; i++) {
        var channel = channels[fullPageCount * channelsPerPage + i];
        finalPage.push(channel);
      }
      pages.push(finalPage);
    }

    return pages;
  }
});
