(function () {
    'use strict';

    angular
        .module('app.admin')
        .controller('AdminController', AdminController);

    AdminController.$inject = ['logger', 'dataservice'];
    /* @ngInject */
    function AdminController(logger, dataservice) {
        var vm = this;
        vm.title = 'Admin';
        vm.people = [];

        activate();

        function activate() {
            logger.info('Activated Admin View');
            dataservice.getPeople().then(function(response){
                vm.people = response;
            });
        }
    }
})();
