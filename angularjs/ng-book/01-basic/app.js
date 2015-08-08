/**
 * Created by quanhua92 on 8/8/15.
 */
angular.module("myApp", [])
    .controller("MyController", function ($scope, $timeout) {
        $scope.person = {
            name: "Ari Lerner"
        }
    });