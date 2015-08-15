angular.module("myApp", [])
    .directive('myDirective', function(){
       return {
           restrict: 'EAC',
           replace: true,
           template: '<a href="http://google.com">Click me to go to google</a>'
       }
    });