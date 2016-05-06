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

import {CheProject} from './che-project';

/**
 * This class is handling the call to wsagent API.
 * @author Ann Shumilova
 */
export class CheWorkspaceAgent {

  /**
   * Default constructor that is using resource
   */
  constructor($resource, $q, cheWebsocket, workspaceAgentData) {
    this.$resource = $resource;
    this.workspaceAgentData = workspaceAgentData;

    this.project = new CheProject($resource, $q, cheWebsocket, this.workspaceAgentData.path);
   /* this.projectTypeService = new CheProjectType();
    this.gitService = new CheGit();*/
    //TODO
    this.cheWorkspace.addListener(this.project);
  }

  getWebsocketUrl() {
    //TODO
    return this.workspaceAgentData;
  }

  getProject() {
    return this.project;
  }

  getGitService() {

  }

  getProjectTypeService() {

  }
}
