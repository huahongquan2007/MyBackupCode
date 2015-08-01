angular.module('ContactsApp', ['ngRoute', 'ngResource'])
    .config(function($routeProvider, $locationProvider){
        $routeProvider
            .when('/contacts', {
                controller: 'ListController',
                templateUrl: 'views/list.html'
            });
        $locationProvider.html5Mode(true); //remove /#1/ in /#1/contacts
    });