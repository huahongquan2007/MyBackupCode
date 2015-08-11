angular.module('myApp', ['ngMessages'])
    .directive('ngFocus', [function() {
        var FOCUS_CLASS = "ng-focused"
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function(scope, element, attrs, ctrl) {
                ctrl.$focused = false;
                element.bind('focus', function(evt) {
                    element.addClass(FOCUS_CLASS);
                    scope.$apply(function() {
                        ctrl.$focused = true;
                    });
                }).bind('blur', function(evt) {
                   element.removeClass(FOCUS_CLASS);
                    scope.$apply(function() {
                        ctrl.$focused = false;
                    })
                });
            }
        }
    }])
    .directive('ensureUnique', ['$http', function($http) {
        return {
            required: 'ngModel',
            link: function(scope, ele, attrs, ctrl) {
                var url = attrs.ensureUnique;

                // TODO: don't know why it is undefined here
                ctrl.$parsers.push(function(val) {
                    if (!val || val.length === 0) {
                        return;
                    }

                    ngModel.$setValidity('checkingAvailability', true);
                    ngModel.$setValidity('usernameAvailability', false);

                    $http({
                        method: 'GET',
                        url: url,
                        params: {
                            username: val
                        }
                    }).success(function() {
                        ngModel.$setValidity('checkingAvailability', false);
                        ngModel.$setValidity('usernameAvailability', true);
                    })['catch'](function() {
                        ngModel.$setValidity('checkingAvailability', false);
                        ngModel.$setValidity('usernameAvailability', false);
                    });
                    return val;
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