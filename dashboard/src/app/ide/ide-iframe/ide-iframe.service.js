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

/*global $:false */

/**
 * Defines a service for displaying iframe for displaying the IDE.
 * @author Florent Benoit
 */
class IdeIFrameSvc {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout, $compile) {
    this.iframeAdded = false;
    this.$timeout = $timeout;
    this.$compile = $compile;

  }


  addIFrame() {
    if (!this.iframeAdded) {
      this.iframeAdded = true;
        // The new element to be added
        var $div = $('<ide-iframe id="ide-iframe-window" ng-show="showIDE" flex style="height: 100%"></ide-iframe>');

        // The parent of the new element
        var $target = $('body');

      let $scope = angular.element($target).scope();
      let insertHtml = this.$compile($div)($scope);
      $('body').find('.main-page').append(insertHtml);

    }
  }


}

export default IdeIFrameSvc;

