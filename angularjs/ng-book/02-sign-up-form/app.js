angular.module('myApp', [])
    .directive('ensureUnique', ['$http', function($http) {
        return {
            required: 'ngModel',
            link: function(scope, ele, attrs, c) {
                scope.$watch(attrs.ngModel, function(n) {
                    if (!n) return;
                    $http({
                        method: 'POST',
                        url: '/api/check/' + attrs.ensureUnique,
                         data: {
                             field: attrs.ensureUnique,
                             value: scope.ngModel
                         }
                    }).success(function(data) {
                        c.$setValidity('unique', data.isUnique);
                    }).error(function(data) {
                        c.$setValidity('unique', false);
                    })
                })
            }
        }
    }])
    .controller('signupController', function($scope) {
        $scope.signup_form = {};
        $scope.signup_form.submitted = false;
        $scope.signupForm = function() {
            if( $scope.signup_form.$valid) {
                // submit as normal
            } else {
                $scope.signup_form.submitted = true;
            }
        }
    });