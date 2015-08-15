angular.module("myApp", [])
    .directive('myDirective', function(){
       return {
           restrict: 'EAC',
           replace: true,
           scope: {
               myUrl: '=someAttr',
               myLinkText: '@'
           },
           template: '\
               <div>\
               <label>My Url Field:</label>\
               <input type="text" ng-model="myUrl"/><a href="{{myUrl}}">{{myLinkText}}</a>'
       }
    });