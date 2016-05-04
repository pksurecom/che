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

import org.eclipse.che.api.debugger.shared.dto.Breakpoint;
import org.eclipse.che.api.debugger.shared.dto.DebugSession;
import org.eclipse.che.api.debugger.shared.dto.Location;
import org.eclipse.che.api.debugger.shared.dto.StackFrameDump;
import org.eclipse.che.api.debugger.shared.dto.Value;
import org.eclipse.che.api.debugger.shared.dto.Variable;
import org.eclipse.che.api.debugger.shared.dto.action.ResumeAction;
import org.eclipse.che.api.debugger.shared.dto.action.StartAction;
import org.eclipse.che.api.debugger.shared.dto.action.StepIntoAction;
import org.eclipse.che.api.debugger.shared.dto.action.StepOutAction;
import org.eclipse.che.api.debugger.shared.dto.action.StepOverAction;
import org.eclipse.che.api.promises.client.Promise;

import java.util.List;
import java.util.Map;

/**
 * Client for the service to debug application.
 *
 * @author Vitaly Parfonov
 * @author Anatoliy Bazko
 */
public interface DebuggerServiceClient {

    /**
     * Establishes connection with debug server.
     *
     * @param debuggerType
     *      the debugger server type, for instance: gdb, jdb etc
     * @param connectionProperties
     *      the connection properties
     */
    Promise<DebugSession> connect(String debuggerType, Map<String, String> connectionProperties);

    /**
     * Disconnects from debugger server.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> disconnect(String id);

    /**
     * Gets debug session info.
     *
     * @param id
     *      debug session id
     */
    Promise<DebugSession> getSessionInfo(String id);

    /**
     * Starts debug session when connection is established.
     * Some debug server might not required this step.
     *
     * @param id
     *      debug session id
     * @param action
     *      the start action parameters
     */
    Promise<Void> start(String id, StartAction action);

    /**
     * Adds breakpoint.
     *
     * @param id
     *      debug session id
     * @param breakpoint
     *      the breakpoint to add
     */
    Promise<Void> addBreakpoint(String id, Breakpoint breakpoint);

    /**
     * Deletes breakpoint.
     *
     * @param id
     *      debug session id
     * @param location
     *      the location of the breakpoint to delete
     */
    Promise<Void> deleteBreakpoint(String id, Location location);

    /**
     * Deletes all breakpoints.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> deleteAllBreakpoints(String id);

    /**
     * Returns all breakpoints.
     *
     * @param id
     *      debug session id
     */
    Promise<List<Breakpoint>> getAllBreakpoints(String id);

    /**
     * Gets dump of fields and local variables of the current frame.
     *
     * @param id
     *      debug session id
     */
    Promise<StackFrameDump> getStackFrameDump(String id);

    /**
     * Resumes application.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> resume(String id, ResumeAction action);

    /**
     * Returns a value of the variable.
     *
     * @param id
     *      debug session id
     */
    Promise<Value> getValue(String id, Variable variable);

    /**
     * Sets the new value of the variable.
     *
     * @param id
     *      debug session id
     */
    Promise<Void> setValue(String id, Variable variable);

    /**
     * Does step into.
     *
     * @param id
     *      debug session id
     * @param action
     *      the step into action parameters
     */
    Promise<Void> stepInto(String id, StepIntoAction action);

    /**
     * Does step over.
     *
     * @param id
     *      debug session id
     * @param action
     *      the step over action parameters
     */
    Promise<Void> stepOver(String id, StepOverAction action);

    /**
     * Does step out.
     *
     * @param id
     *      debug session id
     * @param action
     *      the step out action parameters
     */
    Promise<Void> stepOut(String id, StepOutAction action);

    /**
     * Evaluate the expression.
     *
     * @param id
     *      debug session id
     * @param expression
     *      the expression to evaluate
     */
    Promise<String> evaluate(String id, String expression);
}
