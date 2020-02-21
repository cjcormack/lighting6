var module = angular.module('AceEditor', []);

module.directive('aceEditor', function () {
	return {
		restrict: 'EA',
		require: '?ngModel',
		link: function (scope, elm, attrs, ngModel) {
			var editor = ace.edit(elm[0]);
			var session = editor.getSession();

			var setValue = null;

			// editor.setTheme("ace/theme/monokai");
			editor.session.setMode("ace/mode/kotlin");

			elm.on('$destroy', function () {
				editor.session.$stopWorker();
				editor.destroy();
			});

			scope.$watch(function() {
				return [elm[0].offsetWidth, elm[0].offsetHeight];
			}, function() {
				editor.resize();
				editor.renderer.updateFull();
			}, true);

			session.on("change", function(e){
				scope.$evalAsync(function() {
					ngModel.$setViewValue(session.getValue());
				});
			});

			ngModel.$render = function(){
				session.setValue(ngModel.$viewValue)
			};
		}
	};
});
