angular.module('ontrack.extension.git', [
    'ot.service.core',
    'ot.service.configuration',
    'ot.service.form',
    'ot.service.structure',
    'ot.service.plot'
])
    .directive('otExtensionGitCommitSummary', function () {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'extension/git/directive.commit.summary.tpl.html',
            scope: {
                uiCommit: '=',
                title: '@'
            }
        };
    })
    .directive('otExtensionGitCommitBuilds', function () {
        return {
            restrict: 'E',
            templateUrl: 'extension/git/directive.commit.builds.tpl.html',
            scope: {
                commitInfo: '='
            }
        };
    })
    .directive('otExtensionGitCommitPromotions', function () {
        return {
            restrict: 'E',
            templateUrl: 'extension/git/directive.commit.promotions.tpl.html',
            scope: {
                commitInfo: '='
            }
        };
    })

    // Sync

    .config(function ($stateProvider) {
        $stateProvider.state('git-sync', {
            url: '/extension/git/sync/{branch}',
            templateUrl: 'extension/git/git.sync.tpl.html',
            controller: 'GitSyncCtrl'
        });
    })
    .controller('GitSyncCtrl', function ($stateParams, $state, $scope, $http, $interpolate, ot, otStructureService, otNotificationService) {

        var branchId = $stateParams.branch;
        var view = ot.view();
        view.commands = [
            ot.viewCloseCommand('/branch/' + branchId)
        ];

        // Loading of the sync information
        function load() {
            otStructureService.getBranch(branchId).then(function (branch) {
                $scope.branch = branch;
                view.title = $interpolate("Build synchronisation for branch {{project.name}}/{{name}}")(branch);
                view.breadcrumbs = ot.branchBreadcrumbs(branch);
            });
        }

        // Initialisation
        load();

        // Launching the sync
        $scope.launchSync = function () {
            ot.pageCall($http.post('extension/git/sync/' + branchId, {})).then(function () {
                // Message
                otNotificationService.info("The build synchronisation has been launched in the background.");
                // Goes back to the branch
                $state.go('branch', {branchId: branchId});
            });
        };

    })

    // Issues

    .config(function ($stateProvider) {
        $stateProvider.state('git-issue', {
            url: '/extension/git/{branch}/issue/{issue}',
            templateUrl: 'extension/git/git.issue.tpl.html',
            controller: 'GitIssueCtrl'
        });
    })
    .controller('GitIssueCtrl', function ($stateParams, $scope, $http, $interpolate, ot) {

        var view = ot.view();

        ot.call(
            $http.get(
                $interpolate('extension/git/{{branch}}/issue/{{issue}}')($stateParams)
            )).then(function (ontrackGitIssueInfo) {
                $scope.ontrackGitIssueInfo = ontrackGitIssueInfo;
            });
    })

    // Configurations

    .config(function ($stateProvider) {
        // Artifactory configurations
        $stateProvider.state('git-configurations', {
            url: '/extension/git/configurations',
            templateUrl: 'extension/git/git.configurations.tpl.html',
            controller: 'GitConfigurationsCtrl'
        });
    })
    .controller('GitConfigurationsCtrl', function ($scope, $http, ot, otFormService, otAlertService, otConfigurationService) {
        var view = ot.view();
        view.title = 'Git configurations';
        view.description = 'Management of the Git configurations.';

        // Loading the Artifactory configurations
        function load() {
            ot.call($http.get('extension/git/configurations')).then(function (configurations) {
                $scope.configurations = configurations;
                view.commands = [
                    {
                        id: 'git-configuration-create',
                        name: "Create a configuration",
                        cls: 'ot-command-new',
                        action: $scope.createConfiguration
                    },
                    ot.viewApiCommand(configurations._self),
                    ot.viewCloseCommand('/home')
                ];
            });
        }

        load();

        // Creating a configuration
        $scope.createConfiguration = function () {
            otFormService.display({
                uri: $scope.configurations._create,
                title: "Git configuration",
                buttons: [ otConfigurationService.testButton($scope.configurations._test) ],
                submit: function (data) {
                    return ot.call($http.post($scope.configurations._create, data));
                }
            }).then(load);
        };

        // Deleting a configuration
        $scope.deleteConfiguration = function (configuration) {
            otAlertService.confirm({
                title: 'Git configuration',
                message: "Do you really want to delete this Git configuration? Some projects may still refer to it."
            }).then(
                function success() {
                    ot.call($http.delete(configuration._delete)).then(load);
                }
            );
        };

        // Updating a configuration
        $scope.updateConfiguration = function (configuration) {
            otFormService.display({
                uri: configuration._update,
                title: "Git configuration",
                buttons: [ otConfigurationService.testButton($scope.configurations._test) ],
                submit: function (data) {
                    return ot.call($http.put(configuration._update, data));
                }
            }).then(load);
        };
    })

    // Commits

    .config(function ($stateProvider) {
        $stateProvider.state('git-commit', {
            url: '/extension/git/{branch}/commit/{commit}',
            templateUrl: 'extension/git/git.commit.tpl.html',
            controller: 'GitCommitCtrl'
        });
    })
    .controller('GitCommitCtrl', function ($stateParams, $scope, $http, $interpolate, ot) {

        var view = ot.view();
        view.title = $interpolate("Commit {{commit}}")($stateParams);

        ot.call(
            $http.get(
                $interpolate('extension/git/{{branch}}/commit/{{commit}}')($stateParams)
            )).then(function (ontrackGitCommitInfo) {
                $scope.ontrackGitCommitInfo = ontrackGitCommitInfo;
            });
    })

    // Change log

    .config(function ($stateProvider) {
        $stateProvider.state('git-changelog', {
            url: '/extension/git/changelog?from&to',
            templateUrl: 'extension/git/git.changelog.tpl.html',
            controller: 'GitChangeLogCtrl'
        });
    })
    .controller('GitChangeLogCtrl', function ($q, $log, $interpolate, $anchorScroll, $location, $stateParams, $scope, $http,
                                              ot, otStructureService, otScmChangeLogService, otScmChangelogFilechangefilterService) {

        // The build request
        $scope.buildDiffRequest = {
            from: $stateParams.from,
            to: $stateParams.to
        };

        // The view
        var view = ot.view();
        view.title = "Git change log";

        /**
         * The REST end point to contact is contained by the current path, with the leading
         * slash being removed.
         */
        var path = $location.path().substring(1);

        /**
         * Loads the change log
         */

        ot.pageCall($http.get(path, {params: $scope.buildDiffRequest})).then(function (changeLog) {
            $scope.changeLog = changeLog;

            view.breadcrumbs = ot.projectBreadcrumbs(changeLog.project);

            $scope.commitsCommand = "Commits";
            $scope.issuesCommand = "Issues";
            $scope.filesCommand = "File changes";

            // Loading the commits if needed
            $scope.changeLogCommits = function () {
                if (!$scope.commits) {
                    $scope.commitsLoading = true;
                    $scope.commitsCommand = "Loading the commits...";
                    ot.pageCall($http.get($scope.changeLog._commits)).then(function (commits) {
                        $scope.commits = commits;
                        $scope.commitsLoading = false;
                        $scope.commitsCommand = "Commits";
                        $location.hash('commits');
                        $anchorScroll();
                    });
                } else {
                    $location.hash('commits');
                    $anchorScroll();
                }
            };

            // Loading the issues if needed
            $scope.changeLogIssues = function () {
                if (!$scope.issues) {
                    $scope.issuesLoading = true;
                    $scope.issuesCommand = "Loading the issues...";
                    ot.pageCall($http.get($scope.changeLog._issues)).then(function (issues) {
                        $scope.issues = issues;
                        $scope.issuesLoading = false;
                        $scope.issuesCommand = "Issues";
                        $location.hash('issues');
                        $anchorScroll();
                    });
                } else {
                    $location.hash('issues');
                    $anchorScroll();
                }
            };

            // Loading the file changes if needed
            $scope.changeLogFiles = function () {
                if (!$scope.files) {
                    $scope.filesLoading = true;
                    $scope.filesCommand = "Loading the file changes...";
                    ot.pageCall($http.get($scope.changeLog._files)).then(function (files) {
                        $scope.files = files;
                        $scope.filesLoading = false;
                        $scope.filesCommand = "File changes";
                        $location.hash('files');
                        $anchorScroll();
                    });
                } else {
                    $location.hash('files');
                    $anchorScroll();
                }
            };

            // File filter configuration
            $scope.changeLogFileFilterConfig = otScmChangelogFilechangefilterService.initFilterConfig();

            // Configuring the change log export
            $scope.changeLogExport = function () {
                otScmChangeLogService.displayChangeLogExport({
                    changeLog: $scope.changeLog,
                    exportFormatsLink: changeLog._exportFormats,
                    exportIssuesLink: changeLog._exportIssues
                });
            };

            // Shows the diff for a file
            $scope.showFileDiff = function (changelog, changeLogFile) {
                if (!$scope.diffLoading) {
                    $scope.diffLoading = true;
                    changeLogFile.diffLoading = true;
                    otScmChangelogFilechangefilterService
                        .diffFileFilter(changeLog, changeLogFile.oldPath ? changeLogFile.oldPath : changeLogFile.newPath)
                        .finally(function () {
                            changeLogFile.diffLoading = false;
                            $scope.diffLoading = false;
                        });
                }
            };

        });

    })
    .directive('gitPlot', function (otPlot) {
        return {
            restrict: 'A',
            scope: {
                gitPlot: '='
            },
            link: function (scope, element) {
                scope.$watch('gitPlot',
                    function () {
                        if (scope.gitPlot) {
                            otPlot.draw(element[0], scope.gitPlot);
                        }
                    }
                );
            }
        };
    })

    // Git project sync

    .config(function ($stateProvider) {
        // Git project sync
        $stateProvider.state('git-project-sync', {
            url: '/extension/git/project-sync/{projectId}',
            templateUrl: 'extension/git/git.project-sync.tpl.html',
            controller: 'GitProjectSyncCtrl'
        });
    })
    .controller('GitProjectSyncCtrl', function ($http, $scope, $stateParams, ot, otStructureService, otNotificationService) {
        // Gets the project ID from the parameters
        var projectId = $stateParams.projectId;
        // View definition
        var view = ot.view();
        view.title = 'Git project synchronisation';
        // Loading the project
        otStructureService.getProject(projectId).then(function (project) {
            $scope.project = project;
            // Sub page of the project
            view.breadcrumbs = ot.projectBreadcrumbs(project);
            // Commands
            view.commands = [
                ot.viewCloseCommand('/project/' + project.id)
            ];
            // Loads the Git synchronisation information
            return ot.pageCall($http.get(project._gitSync));
        }).then(function (gitSyncInfo) {
            $scope.gitSyncInfo = gitSyncInfo;
        });
        // Project synchronisation
        $scope.projectSync = function (reset) {
            $scope.synchronising = true;
            ot.pageCall($http.post($scope.project._gitSync, {reset: reset})).then(function (ack) {
                if (!ack.success) {
                    otNotificationService.error("The Git synchronisation could be launched.");
                } else {
                    otNotificationService.success("The Git synchronisation has been launched in the background.");
                }
                // Loads the Git synchronisation information
                return ot.pageCall($http.get($scope.project._gitSync));
            }).then(function (gitSyncInfo){
                $scope.gitSyncInfo = gitSyncInfo;
            }).finally(function () {
                $scope.synchronising = false;
            });
        };
    })
;