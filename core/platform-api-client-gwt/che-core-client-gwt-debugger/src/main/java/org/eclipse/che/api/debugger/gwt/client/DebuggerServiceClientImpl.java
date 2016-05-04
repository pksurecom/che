/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.debugger.gwt.client;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.debugger.shared.dto.Breakpoint;
import org.eclipse.che.api.debugger.shared.dto.DebugSession;
import org.eclipse.che.api.debugger.shared.dto.Location;
import org.eclipse.che.api.debugger.shared.dto.StackFrameDump;
import org.eclipse.che.api.debugger.shared.dto.Value;
import org.eclipse.che.api.debugger.shared.dto.Variable;
import org.eclipse.che.api.debugger.shared.dto.action.Action;
import org.eclipse.che.api.debugger.shared.dto.action.ResumeAction;
import org.eclipse.che.api.debugger.shared.dto.action.StartAction;
import org.eclipse.che.api.debugger.shared.dto.action.StepIntoAction;
import org.eclipse.che.api.debugger.shared.dto.action.StepOutAction;
import org.eclipse.che.api.debugger.shared.dto.action.StepOverAction;
import org.eclipse.che.api.machine.gwt.client.DevMachine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * The implementation of {@link DebuggerServiceClient}.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class DebuggerServiceClientImpl implements DebuggerServiceClient {
    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AppContext             appContext;

    @Inject
    protected DebuggerServiceClientImpl(AppContext appContext,
                                        LoaderFactory loaderFactory,
                                        AsyncRequestFactory asyncRequestFactory,
                                        DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;
    }

    @Override
    public Promise<DebugSession> connect(String debuggerType, Map<String, String> connectionProperties) {
        final String requestUrl = getBaseUrl() + "?type=" + debuggerType;
        return asyncRequestFactory.createPostRequest(requestUrl, null)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .data(JsonHelper.toJson(connectionProperties))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(DebugSession.class));
    }

    @Override
    public Promise<Void> disconnect(String id) {
        final String requestUrl = getBaseUrl() + "/" + id;
        return asyncRequestFactory.createDeleteRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<DebugSession> getSessionInfo(String id) {
        final String requestUrl = getBaseUrl() + "/" + id;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(DebugSession.class));
    }

    @Override
    public Promise<Void> start(String id, StartAction action) {
        return performAction(id, action);
    }

    @Override
    public Promise<Void> addBreakpoint(String id, Breakpoint breakpoint) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        return asyncRequestFactory.createPostRequest(requestUrl, breakpoint)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<List<Breakpoint>> getAllBreakpoints(String id) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(Breakpoint.class));
    }

    @Override
    public Promise<Void> deleteBreakpoint(String id, Location location) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        final String params = "?target=" + location.getTarget() + "&line=" + location.getLineNumber();
        return asyncRequestFactory.createDeleteRequest(requestUrl + params).send();
    }

    @Override
    public Promise<Void> deleteAllBreakpoints(String id) {
        final String requestUrl = getBaseUrl() + "/" + id + "/breakpoint";
        return asyncRequestFactory.createDeleteRequest(requestUrl).send();
    }

    @Override
    public Promise<StackFrameDump> getStackFrameDump(String id) {
        final String requestUrl = getBaseUrl() + "/" + id + "/dump";
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(StackFrameDump.class));
    }

    @Override
    public Promise<Void> resume(String id, ResumeAction action) {
        return performAction(id, action);
    }

    @Override
    public Promise<Value> getValue(String id, Variable variable) {
        final String requestUrl = getBaseUrl() + "/" + id + "/value";
        List<String> path = variable.getVariablePath().getPath();

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            params.append(i == 0 ? "?" : "&");
            params.append("path");
            params.append(i);
            params.append("=");
            params.append(path.get(i));
        }

        return asyncRequestFactory.createGetRequest(requestUrl + params)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(Value.class));
    }

    @Override
    public Promise<Void> setValue(String id, Variable variable) {
        final String requestUrl = getBaseUrl() + "/" + id + "/value";
        return asyncRequestFactory.createPutRequest(requestUrl, variable)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }

    @Override
    public Promise<Void> stepInto(String id, StepIntoAction action) {
        return performAction(id, action);
    }

    @Override
    public Promise<Void> stepOver(String id, StepOverAction action) {
        return performAction(id, action);
    }

    @Override
    public Promise<Void> stepOut(String id, StepOutAction action) {
        return performAction(id, action);
    }

    @Override
    public Promise<String> evaluate(String id, String expression) {
        String requestUrl = getBaseUrl() + "/" + id + "/evaluation";
        String params = "?expression=" + URL.encodeQueryString(expression);
        return asyncRequestFactory.createGetRequest(requestUrl + params)
                                  .loader(loaderFactory.newLoader())
                                  .send(new StringUnmarshaller());
    }

    private String getBaseUrl() {
        DevMachine devMachine = appContext.getDevMachine();
        return devMachine.getWsAgentBaseUrl() + "/debugger/" + devMachine.getWorkspace();
    }

    protected Promise<Void> performAction(String id, Action action) {
        final String requestUrl = getBaseUrl() + "/" + id;
        return asyncRequestFactory.createPostRequest(requestUrl, action)
                                  .loader(loaderFactory.newLoader())
                                  .send();
    }
}
