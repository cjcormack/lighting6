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

				var details = $scope.scriptsMap[scriptId];

				$scope.current = {
					hasChanged: false,
					isValid: true,
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
					id: "new",
					name: "New Script",
					script: ""
				};
				$scope.scripts.push(newScript);
				buildScriptsMap();

				$scope.current = {
					hasChanged: true,
					details: newScript,
					originalDetails: angular.copy(newScript),
					isNew: true,
					isValid: true
				};
			};

			var handleChange = function() {
				if (!$scope.current) {
					return;
				}

				$scope.current.hasChanged = !angular.equals($scope.current.originalDetails, $scope.current.details);

				var isValid = true;

				var currentName = $scope.current.details.name;
				var currentScriptId = $scope.current.details.id;
				if ($scope.current.isNew) {
					currentScriptId = "new";
				}

				if (!currentName) {
					isValid = false;
				}

				if (!$scope.current.details.script) {
					isValid = false;
				}

				var keys = Object.keys($scope.scriptsMap);
				keys.forEach(function(key) {
					if (key != currentScriptId) {
						script = $scope.scriptsMap[key];
						if (currentName == script.name) {
							isValid = false;
						}
					}
				});

				$scope.current.isValid = isValid;

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
				$scope.current.details.name = $scope.current.originalDetails.name;
				$scope.current.details.script = $scope.current.originalDetails.script;
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
								details: $scope.scriptsMap[scriptId],
								originalDetails: angular.copy($scope.scriptsMap[scriptId])
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
								details: $scope.scriptsMap[scriptId],
								originalDetails: angular.copy($scope.scriptsMap[scriptId])
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
					$scope.scripts.pop();
					buildScriptsMap();
					$scope.current = null;
					selectFirstScript();
				} else {
					$scope.current.deleteApprovalVisible = true;
				}

			};
			$scope.doDeleteScript = function() {
				if (!$scope.current) {
					return;
				}

				if ($scope.current.isNew) {
					$scope.scripts.pop();
					buildScriptsMap();
					$scope.current = null;
					selectFirstScript();
				} else {
					var scriptId = $scope.current.details.id;

					$scope.loading = "Deleting";
					ScriptsService.deleteScript(scriptId).then(function(response) {
						$scope.current = null;
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

					console.log(result);

					updatePositions();

					$scope.current.runResult = result;
					$scope.current.runResultModalVisible = true;
				}, function(error) {
					console.log(error);
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

			var selectFirstScript = function() {
				var keys = Object.keys($scope.scriptsMap);
				if (keys.length >= 1) {
					$scope.selectScript(keys[0]);
				}
			};

			var buildScriptsMap = function() {
				var scriptMap = {};
				$scope.scripts.forEach(function(script) {
					scriptMap[script.id] = script;
				});
				$scope.scriptsMap = scriptMap;
			};

			var updateScriptList = function() {
				$scope.loading = "Loading";
				return ScriptsService.getScriptList(true).then(
					function (scripts) {
						$scope.loading = false;

						$scope.scripts = scripts;
						buildScriptsMap();

						selectFirstScript();
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
