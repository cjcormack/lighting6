var module = angular.module('ScriptsDirective', ['ScriptsService']);

module.directive('scriptsDirective', function (ScriptsService, $window) {
	return {
		restrict: 'A',
		templateUrl: '/lighting/style/partials/scriptsDirective.html',
		scope: {},
		link: function ($scope, el, attrs) {
			$scope.current = null;

			$scope.selectScript = function (scriptId) {
				if ($scope.current && $scope.current.details.id === scriptId) {
					return;
				}
				$scope.current = {
					hasChanged: false,
					details: $scope.scripts[scriptId],
					originalDetails: angular.copy($scope.scripts[scriptId])
				};
			};

			var handleChange = function() {
				if (!$scope.current) {
					return;
				}

				$scope.current.hasChanged = !angular.equals($scope.current.originalDetails, $scope.current.details);

				updatePositions();
			};

			$scope.scriptChange = function() {
				handleChange();
			};

			$scope.$watchCollection("current.details", function() {
				handleChange();
			});

			$scope.discardChanges = function() {
				if (!$scope.current || !$scope.current.hasChanged) {
					return;
				}
				console.log("discardChanges");
				$scope.current.details = angular.copy($scope.current.originalDetails);
				$scope.scripts[$scope.current.details.id] = $scope.current.details;
			};
			$scope.saveChanges = function() {
				if (!$scope.current || !$scope.current.hasChanged) {
					return;
				}
				console.log("saveChanges");

				var scriptId = $scope.current.details.id;

				ScriptsService.updateScript(scriptId, $scope.current.details).then(function() {
					updateScriptList().then(function() {
						$scope.current = {
							hasChanged: false,
							details: $scope.scripts[scriptId],
							originalDetails: angular.copy($scope.scripts[scriptId])
						};
					})
				});
			};

			$scope.testCompile = function() {
				if (!$scope.current) {
					return;
				}

				ScriptsService.compileScript($scope.current.details.script).then(function(result) {
					updatePositions();

					$scope.current.compileResult = result;
					$scope.current.compileResultModalVisible = true;

					console.log(result);
				});
			};
			$scope.runScript = function() {
				if (!$scope.current) {
					return;
				}

				ScriptsService.runScript($scope.current.details.script).then(function(result) {
					updatePositions();

					$scope.current.runResult = result;
					$scope.current.runResultModalVisible = true;
					console.log(result);
				});
			};

			var updatePositions = function() {
				var newHeight = 0;

				var scriptEditor = el.find("#script-editor");
				if (scriptEditor && scriptEditor.length > 0) {
					var top = $(scriptEditor[0]).offset().top;
					var innerHeight = $window.innerHeight;

					newHeight = innerHeight - top - 100;
				}

				if (newHeight < 100) {
					newHeight = 100;
				}

				var elPosition = $(el[0]).offset();

				$scope.height = newHeight + "px";
				$scope.modalHolder = {
					left: -elPosition.left + "px",
					top: -elPosition.top + "px"
				};

				console.log();
			};

			updatePositions();

			angular.element($window).bind('resize', function(){
				$scope.$apply(function() {
					updatePositions();
				});
			});

			var updateScriptList = function() {
				return ScriptsService.getScriptList(true).then(
					function (scripts) {
						var scriptMap = {};
						scripts.forEach(function(script) {
							scriptMap[script.id] = script;
						});
						$scope.scripts = scriptMap;
					}, function (error) {
						console.error('Could not load scripts', error);
					}
				);
			};
			updateScriptList();
		}
	};
});
