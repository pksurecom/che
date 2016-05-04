/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Test the workspace name uniqueness
 * @author Oleksii Kurinnyi
 */

describe('unique-workspace-name-validator', function() {
  var $scope, form, $compiler;

  /**
   * Workspace API
   */
  var factoryWorkspace;

  /**
   * API builder.
   */
  var apiBuilder;

  /**
   * Backend for handling http operations
   */
  var httpBackend;

  /**
   * Che backend
   */
  var cheBackend;


  beforeEach(angular.mock.module('userDashboard'));


  beforeEach(inject(function($compile, $rootScope, cheWorkspace, cheAPIBuilder, cheHttpBackend, $document) {
    $scope = $rootScope;
    $compiler = $compile;
    factoryWorkspace = cheWorkspace;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
    this.$document = $document;

  }));

  describe('Validate Workspace Name', function() {

    it('workspaceAlready exists', function() {

      // setup tests objects
      var idWorkspace1 = 'idOfMyWorkspace1';
      var nameWorkspace1 = 'testWorkspace1';
      var workspace1 = apiBuilder.getWorkspaceBuilder().withName(nameWorkspace1).withId(idWorkspace1).build();

      factoryWorkspace.fetchWorkspaces();

      // add into backend
      cheBackend.addWorkspaces([workspace1]);
      cheBackend.setup();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = { workspaceName: null};

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.workspaceName" name="name" unique-workspace-name="model.workspaceName" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue(nameWorkspace1);

      // check form (expect invalid)
      expect(form.name.$invalid).toBe(true);
      expect(form.name.$valid).toBe(false);
    });

    it('workspace not yet defined', function() {

      // setup tests objects
      var idWorkspace1 = 'idOfMyWorkspace1';
      var nameWorkspace1 = 'testWorkspace1';
      var workspace1 = apiBuilder.getWorkspaceBuilder().withName(nameWorkspace1).withId(idWorkspace1).build();

      factoryWorkspace.fetchWorkspaces();

      // add into backend
      cheBackend.addWorkspaces([workspace1]);

      // setup backend
      cheBackend.setup();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = { workspaceName: null };

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.workspaceName" name="name" unique-workspace-name="model.workspaceName" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue('dummyWorkspace');

      // check form valid
      expect(form.name.$invalid).toBe(false);
      expect(form.name.$valid).toBe(true);

    });
  });
});
