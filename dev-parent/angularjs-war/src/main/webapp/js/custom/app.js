'use strict';

/* App Module */
var phonecatApp = angular.module('phonecatApp', [
  'ngRoute',
  'phonecatAnimations',
  'phonecatControllers',
  'phonecatFilters'
]);

phonecatApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider.
      when('/phones', {
        templateUrl: 'partials/phone-list.html',
        controller: 'PhoneListCtrl'
      }).
      when('/phones/:phoneId', {
        templateUrl: 'partials/phone-detail.html',
        controller: 'PhoneDetailCtrl'
      }).
      when('/login', {
        templateUrl: 'partials/login.html',
        controller: 'LoginCtrl',
        pageTitle: "Login"
      }).
      when('/focusExample', {
        templateUrl: 'partials/focus-example.html',
        controller: 'FocusExampleCtrl',
        pageTitle: "Focus Example"
      }).
      when('/userDetails', {
        templateUrl: 'partials/user-details.html',
        controller: 'UserDetailsCtrl',
        pageTitle: "User Details"
      }).
      when('/error', {
        templateUrl: 'partials/error-page.html',
        controller: 'ErrorPageCtrl',
        pageTitle: "Error Page"
      }).
      otherwise({
        redirectTo: '/phones'
      });
}]);

phonecatApp.service('titleService', ['$rootScope', function($rootScope) {
    this.defaultTitle = function() { return "Google Phone Gallery"};
    this.delimitedTitle = function() {return this.defaultTitle() + ": "};
    this.currentTitle = this.defaultTitle();
    this.setTitle = function(pageTitle) {
        this.currentTitle = this.delimitedTitle() + pageTitle;
        $rootScope.$broadcast('titleChanged');
    };
    this.setDefaultTitle = function() {
        this.currentTitle = this.defaultTitle();
        $rootScope.$broadcast('titleChanged');
    };
}]);

phonecatApp.service('authService', ['$rootScope', '$window', function ($rootScope, $window) {
    this.setAuthData = function (authData) {
        if($window.localStorage) {
            $window.localStorage.setItem('auth-id', authData.authId);
            $window.localStorage.setItem('auth-token', authData.authToken);
            $rootScope.$broadcast('authChanged');
        }
    };
    this.getAuthData = function () {
        if($window.localStorage && $window.localStorage.getItem('auth-id') && $window.localStorage.getItem('auth-token')) {
            var authData = {
                authId: $window.localStorage.getItem('auth-id'),
                authToken: $window.localStorage.getItem('auth-token')
            };
            return authData;
        } else {
            return undefined;
        }
    };
    this.isAuthenticated = function () {
        return !angular.isUndefined(this.getAuthData());
    };
}]);

phonecatApp.factory('authHttpRequestInterceptor', ['$rootScope', '$injector', 'authService', function ($rootScope, $injector, authService) {
    var authHttpRequestInterceptor = {
        request: function ($request) {
            if (authService.isAuthenticated()) {
                $request.headers['auth-id'] = authService.getAuthData().authId;
                $request.headers['auth-token'] = authService.getAuthData().authToken;
            }
            return $request;
        }
    }
    return authHttpRequestInterceptor;
}]);

phonecatApp.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('authHttpRequestInterceptor');
}]);

phonecatApp.run(['$rootScope', '$location', '$anchorScroll', '$routeParams', function($rootScope, $location, $anchorScroll, $routeParams) {
  //when the route is changed scroll to the proper element
  $rootScope.$on('$routeChangeSuccess', function(newRoute, oldRoute) {
    $location.hash($routeParams.scrollTo);
    $location.replace();
  });
}]);

