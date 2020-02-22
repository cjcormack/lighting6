var module = angular.module('ScriptsDirective', ['ScriptsService']);

module.directive('scriptsDirective', function (ScriptsService, $window) {
	return {
		restrict: 'A',
		templateUrl: '/lighting/style/partials/scriptsDirective.html',
		scope: {},
		link: function ($scope, el, attrs) {
			$scope.current = null;

			var handleUnsavedChanges = function() {
				if (!$scope.current || !($scope.current.hasChanged || $scope.current.isNew)) {
					return true;
				}

				$scope.current.discardChangesVisible = true;

				return false;
			};

			$scope.selectScript = function (scriptId) {
				if ($scope.current && $scope.current.details.id === scriptId) {
					return;
				}

				if (!handleUnsavedChanges()) {
					return;
				}

				var details = $scope.scripts[scriptId];

				$scope.current = {
					hasChanged: false,
					details: details,
					originalDetails: angular.copy(details),
					isNew: false
				};
			};
			$scope.newScript = function() {
				if (!handleUnsavedChanges()) {
					return;
				}

				var newScript = {
					name: "New Script",
					script: ""
				};
				$scope.scripts["new"] = newScript;
				$scope.current = {
					hasChanged: true,
					details: newScript,
					originalDetails: angular.copy(newScript),
					isNew: true
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
				$scope.current.details = angular.copy($scope.current.originalDetails);
				$scope.scripts[$scope.current.details.id] = $scope.current.details;
			};
			$scope.saveChanges = function() {
				if (!$scope.current || (!$scope.current.hasChanged && !$scope.current.isNew)) {
					return;
				}

				if ($scope.current.isNew) {
					$scope.loading = "Creating";
					ScriptsService.createScript($scope.current.details).then(function(response) {
						var scriptId = response.id;
						updateScriptList().then(function() {
							$scope.loading = false;
							$scope.current = {
								hasChanged: false,
								details: $scope.scripts[scriptId],
								originalDetails: angular.copy($scope.scripts[scriptId])
							};
						}, function(error) {
							$scope.loading = false;
						})
					}, function(error) {
						$scope.loading = false;
					});
				} else if ($scope.current.hasChanged) {
					var scriptId = $scope.current.details.id;

					$scope.loading = "Saving";
					ScriptsService.updateScript(scriptId, $scope.current.details).then(function() {
						updateScriptList().then(function() {
							$scope.loading = false;
							$scope.current = {
								hasChanged: false,
								details: $scope.scripts[scriptId],
								originalDetails: angular.copy($scope.scripts[scriptId])
							};
						}, function(error) {
							$scope.loading = false;
						});
					}, function(error) {
						$scope.loading = false;
					});
				}

			};
			$scope.deleteScript = function() {
				if (!$scope.current) {
					return;
				}

				if ($scope.current.isNew && !$scope.current.hasChanged) {
					delete $scope.scripts["new"];
					$scope.current = null;
				} else {
					$scope.current.deleteApprovalVisible = true;
				}

			};
			$scope.doDeleteScript = function() {
				if (!$scope.current) {
					return;
				}

				if ($scope.current.isNew) {
					delete $scope.scripts["new"];
					$scope.current = null;
				} else {
					var scriptId = $scope.current.details.id;

					$scope.loading = "Deleting";
					ScriptsService.deleteScript(scriptId).then(function(response) {
						updateScriptList().then(function() {
							$scope.loading = false;
						});
					}, function(error) {
						$scope.loading = false;
					});
				}
			};

			$scope.testCompile = function() {
				if (!$scope.current) {
					return;
				}

				$scope.loading = "Compiling";

				ScriptsService.compileScript($scope.current.details.script).then(function(result) {
					$scope.loading = false;
					updatePositions();

					$scope.current.compileResult = result;
					$scope.current.compileResultModalVisible = true;
				}, function(error) {
					$scope.loading = false;
				});
			};
			$scope.runScript = function() {
				if (!$scope.current) {
					return;
				}

				$scope.loading = "Running";

				ScriptsService.runScript($scope.current.details.script).then(function(result) {
					$scope.loading = false;

					updatePositions();

					$scope.current.runResult = result;
					$scope.current.runResultModalVisible = true;
				}, function(error) {
					$scope.loading = false;
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
			};

			updatePositions();

			angular.element($window).bind('resize', function(){
				$scope.$apply(function() {
					updatePositions();
				});
			});

			var updateScriptList = function() {
				$scope.loading = "Loading";
				return ScriptsService.getScriptList(true).then(
					function (scripts) {
						$scope.loading = false;
						var scriptMap = {};
						scripts.forEach(function(script) {
							scriptMap[script.id] = script;
						});
						$scope.scripts = scriptMap;

						var keys = Object.keys(scriptMap);
						if (keys.length >= 1) {
							$scope.selectScript(keys[0]);
						}
					}, function (error) {
						$scope.loading = false;
						console.error('Could not load scripts', error);
					}
				);
			};
			updateScriptList();
		}
	};
});
