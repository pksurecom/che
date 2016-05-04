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
package org.eclipse.che.plugin.docker.client;

import com.google.common.io.CharStreams;

import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.commons.test.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.docker.client.connection.CloseConnectionInputStream;
import org.eclipse.che.plugin.docker.client.connection.DockerConnection;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.connection.DockerResponse;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;
import org.eclipse.che.plugin.docker.client.json.ContainerCommitted;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerExitStatus;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerProcesses;
import org.eclipse.che.plugin.docker.client.json.Event;
import org.eclipse.che.plugin.docker.client.json.ExecCreated;
import org.eclipse.che.plugin.docker.client.json.ExecInfo;
import org.eclipse.che.plugin.docker.client.json.Image;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.SystemInfo;
import org.eclipse.che.plugin.docker.client.json.Version;
import org.eclipse.che.plugin.docker.client.params.AttachContainerParams;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CommitParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.GetEventsParams;
import org.eclipse.che.plugin.docker.client.params.GetExecInfoParams;
import org.eclipse.che.plugin.docker.client.params.GetResourceParams;
import org.eclipse.che.plugin.docker.client.params.InspectContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.client.params.KillContainerParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.PushParams;
import org.eclipse.che.plugin.docker.client.params.PutResourceParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.client.params.StopContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.TopParams;
import org.eclipse.che.plugin.docker.client.params.WaitContainerParams;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Anton Korneta
 * @author Mykola Morhun
 */
@Listeners(MockitoTestNGListener.class)
public class DockerConnectorTest {

    private static final String EXCEPTION_ERROR_MESSAGE    = "Error response from docker API, status: 500, message: Error";
    private static final int    RESPONSE_ERROR_CODE        = 500;
    private static final int    RESPONSE_SUCCESS_CODE      = 200;
    private static final int    RESPONSE_NO_CONTENT_CODE   = 204;
    private static final int    RESPONSE_CREATED_CODE      = 201;

    private static final String REQUEST_METHOD_GET    = "GET";
    private static final String REQUEST_METHOD_POST   = "POST";
    private static final String REQUEST_METHOD_PUT    = "PUT";
    private static final String REQUEST_METHOD_DELETE = "DELETE";

    private static final String   IMAGE                 = "image";
    private static final String   REPOSITORY            = "repository";
    private static final String   CONTAINER             = "container";
    private static final String   REGISTRY              = "registry";
    private static final String   TAG                   = "tag";
    private static final String   DIGEST                = "hash:1234567890";
    private static final String   EXEC_ID               = "exec_id";
    private static final String   PATH_TO_FILE          = "/home/user/path/file.ext";
    private static final String   STREAM_DATA           = "stream data";
    private static final String   DOCKER_RESPONSE       = "stream";
    private static final String   ERROR_MESSAGE         = "some error occurs";
    private static final String[] CMD_WITH_ARGS         = { "command", "arg1", "arg2" };
    private static final String[] CMD_ARGS              = { "arg1", "arg2" };
    private static final byte[]   STREAM_DATA_BYTES     = STREAM_DATA.getBytes();
    private static final byte[]   DOCKER_RESPONSE_BYTES = DOCKER_RESPONSE.getBytes();

    private static final int CONTAINER_EXIT_CODE = 0;

    private DockerConnector dockerConnector;

    private DockerConnection             dockerConnection;
    @Mock
    private DockerConnectorConfiguration dockerConnectorConfiguration;
    @Mock
    private DockerConnectionFactory      dockerConnectionFactory;
    @Mock
    private DockerResponse               dockerResponse;
    @Mock
    private ProgressMonitor              progressMonitor;
    @Mock
    private InitialAuthConfig            initialAuthConfig;
    @Mock
    private InputStream                  inputStream;
    @Mock
    private MessageProcessor<LogMessage> logMessageProcessor;
    @Mock
    private MessageProcessor<Event>      eventMessageProcessor;
    @Mock
    private File                         dockerfile;

    @Captor
    private ArgumentCaptor<Object>       captor;

    @BeforeMethod
    public void setup() throws IOException, URISyntaxException {
        dockerConnection = mock(DockerConnection.class, new SelfReturningAnswer());
        when(dockerConnectionFactory.openConnection(any(URI.class))).thenReturn(dockerConnection);
        when(dockerConnection.request()).thenReturn(dockerResponse);
        when(dockerConnectorConfiguration.getAuthConfigs()).thenReturn(initialAuthConfig);
        when(dockerResponse.getStatus()).thenReturn(RESPONSE_SUCCESS_CODE);
        when(dockerResponse.getInputStream()).thenReturn(inputStream);

        dockerConnector = spy(new DockerConnector(dockerConnectorConfiguration, dockerConnectionFactory));

        doReturn(new DockerException(EXCEPTION_ERROR_MESSAGE, RESPONSE_ERROR_CODE))
                .when(dockerConnector).getDockerException(any(DockerResponse.class));

    }

    @Test
    public void shouldBeAbleToGetSystemInfo() throws IOException, JsonParseException {
        SystemInfo systemInfo = mock(SystemInfo.class);

        doReturn(systemInfo).when(dockerConnector).parseResponseStreamAndClose(inputStream, SystemInfo.class);

        SystemInfo returnedSystemInfo =
                dockerConnector.getSystemInfo();

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/info");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedSystemInfo, systemInfo);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileGettingSystemInfoIfResponseCodeIsNotSuccess() throws IOException {
        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.getSystemInfo();

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToGetVersion() throws IOException, JsonParseException {
        Version version = mock(Version.class);

        doReturn(version).when(dockerConnector).parseResponseStreamAndClose(inputStream, Version.class);

        Version returnedVersion =
                dockerConnector.getVersion();

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/version");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedVersion, version);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileGettingVersionIfResponseCodeIsNotSuccess() throws IOException {
        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.getVersion();

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToGetListImages() throws IOException, JsonParseException {
        List<Image> images = new ArrayList<>();

        doReturn(images).when(dockerConnector).parseResponseStreamAsListAndClose(inputStream);

        List<Image> returnedImages =
                dockerConnector.listImages();

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/images/json");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedImages, images);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileListingImagesIfResponseCodeIsNotSuccess() throws IOException {
        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.listImages();

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldCallInspectImageWithParametersObject() throws IOException {
        InspectImageParams inspectImageParams = InspectImageParams.from(IMAGE);

        ImageInfo imageInfo = mock(ImageInfo.class);

        doReturn(imageInfo).when(dockerConnector).inspectImage(any(InspectImageParams.class));

        ImageInfo returnedImageInfo =
                dockerConnector.inspectImage(IMAGE);

        verify(dockerConnector).inspectImage((InspectImageParams) captor.capture());

        assertEquals(captor.getValue(), inspectImageParams);
        assertEquals(returnedImageInfo, imageInfo);
    }

    @Test
    public void shouldBeAbleToInspectImage() throws IOException, JsonParseException {
        InspectImageParams inspectImageParams = InspectImageParams.from(IMAGE);

        ImageInfo imageInfo = mock(ImageInfo.class);

        doReturn(imageInfo).when(dockerConnector).parseResponseStreamAndClose(inputStream, ImageInfo.class);

        ImageInfo returnedImageInfo =
                dockerConnector.inspectImage(inspectImageParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/images/" + inspectImageParams.getImage() + "/json");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedImageInfo, imageInfo);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileGettingImageInfoIfResponseCodeIsNotSuccess() throws IOException {
        InspectImageParams inspectImageParams = InspectImageParams.from(IMAGE);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.inspectImage(inspectImageParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToStopContainer() throws IOException {
        StopContainerParams stopContainerParams = StopContainerParams.from(CONTAINER);

        dockerConnector.stopContainer(stopContainerParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/containers/" + stopContainerParams.getContainer() + "/stop");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileStoppingContainerIfResponseCodeIsNotSuccess() throws IOException {
        StopContainerParams stopContainerParams = StopContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.stopContainer(stopContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldCallKillContainerWithParametersObject() throws IOException {
        KillContainerParams killContainerParams = KillContainerParams.from(CONTAINER);

        doNothing().when(dockerConnector).killContainer(killContainerParams);

        dockerConnector.killContainer(CONTAINER);

        verify(dockerConnector).killContainer((KillContainerParams) captor.capture());

        assertEquals(captor.getValue(), killContainerParams);
    }

    @Test
    public void shouldBeAbleToKillContainer() throws IOException {
        KillContainerParams killContainerParams = KillContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_NO_CONTENT_CODE);

        dockerConnector.killContainer(killContainerParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/containers/" + killContainerParams.getContainer() + "/kill");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileKillingContainerIfResponseCodeIsNotSuccess() throws IOException {
        KillContainerParams killContainerParams = KillContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.killContainer(killContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToRemoveContainer() throws IOException {
        RemoveContainerParams removeContainerParams = RemoveContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_NO_CONTENT_CODE);

        dockerConnector.removeContainer(removeContainerParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_DELETE);
        verify(dockerConnection).path("/containers/" + removeContainerParams.getContainer());
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileRemovingContainerIfResponseCodeIsNotSuccess() throws IOException {
        RemoveContainerParams removeContainerParams = RemoveContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.removeContainer(removeContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldCallWaitContainerWithParametersObject() throws IOException {
        WaitContainerParams waitContainerParams = WaitContainerParams.from(CONTAINER);

        doReturn(CONTAINER_EXIT_CODE).when(dockerConnector).waitContainer(waitContainerParams);

        int returnedExitCode =
                dockerConnector.waitContainer(CONTAINER);

        verify(dockerConnector).waitContainer((WaitContainerParams) captor.capture());

        assertEquals(captor.getValue(), waitContainerParams);
        assertEquals(returnedExitCode, CONTAINER_EXIT_CODE);
    }

    @Test
    public void shouldBeAbleToWaitContainer() throws IOException, JsonParseException {
        WaitContainerParams waitContainerParams = WaitContainerParams.from(CONTAINER);

        ContainerExitStatus containerExitStatus = new ContainerExitStatus();
        containerExitStatus.setStatusCode(CONTAINER_EXIT_CODE);

        doReturn(containerExitStatus).when(dockerConnector).parseResponseStreamAndClose(inputStream, ContainerExitStatus.class);

        int returnedExitCode =
                dockerConnector.waitContainer(waitContainerParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/containers/" + waitContainerParams.getContainer() + "/wait");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedExitCode, containerExitStatus.getStatusCode());
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileWaitingContainerIfResponseCodeIsNotSuccess() throws IOException {
        WaitContainerParams waitContainerParams = WaitContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.waitContainer(waitContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldCallInspectContainerWithParametersObject() throws IOException {
        InspectContainerParams inspectContainerParams = InspectContainerParams.from(CONTAINER);

        ContainerInfo containerInfo = mock(ContainerInfo.class);

        doReturn(containerInfo).when(dockerConnector).inspectContainer(inspectContainerParams);

        ContainerInfo returnedContainerInfo =
                dockerConnector.inspectContainer(CONTAINER);

        verify(dockerConnector).inspectContainer((InspectContainerParams) captor.capture());

        assertEquals(captor.getValue(), inspectContainerParams);
        assertEquals(returnedContainerInfo, containerInfo);
    }

    @Test
    public void shouldBeAbleToInspectContainer() throws IOException, JsonParseException {
        InspectContainerParams inspectContainerParams = InspectContainerParams.from(CONTAINER);

        ContainerInfo containerInfo = mock(ContainerInfo.class);

        doReturn(containerInfo).when(dockerConnector).parseResponseStreamAndClose(inputStream, ContainerInfo.class);

        ContainerInfo returnedContainerInfo =
                dockerConnector.inspectContainer(inspectContainerParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/containers/" + inspectContainerParams.getContainer() + "/json");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedContainerInfo, containerInfo);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileInspectingContainerIfResponseCodeIsNotSuccess() throws IOException {
        InspectContainerParams inspectContainerParams = InspectContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.inspectContainer(inspectContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToAttachContainer() throws IOException {
        AttachContainerParams attachContainerParams = AttachContainerParams.from(CONTAINER);

        when(dockerResponse.getInputStream()).thenReturn(new ByteArrayInputStream(DOCKER_RESPONSE_BYTES));

        dockerConnector.attachContainer(attachContainerParams, logMessageProcessor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/containers/" + attachContainerParams.getContainer() + "/attach");
        verify(dockerConnection).query("stdout", 1);
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileAttachingContainerIfResponseCodeIsNotSuccess() throws IOException {
        AttachContainerParams attachContainerParams = AttachContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.attachContainer(attachContainerParams, logMessageProcessor);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToCreateExec() throws IOException, JsonParseException {
        CreateExecParams createExecParams = CreateExecParams.from(CONTAINER, CMD_WITH_ARGS);
        Exec exec = new Exec(CMD_WITH_ARGS, EXEC_ID);

        ExecCreated execCreated = mock(ExecCreated.class);

        doReturn(execCreated).when(dockerConnector).parseResponseStreamAndClose(inputStream, ExecCreated.class);
        when(execCreated.getId()).thenReturn(EXEC_ID);

        Exec returnedExec =
                dockerConnector.createExec(createExecParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/containers/" + createExecParams.getContainer() + "/exec");
        verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
        verify(dockerConnection).header(eq("Content-Length"), anyInt());
        verify(dockerConnection).entity(any(byte[].class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedExec, exec);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileCreatingExecIfResponseCodeIsNotSuccess() throws IOException {
        CreateExecParams inspectContainerParams = CreateExecParams.from(CONTAINER, CMD_WITH_ARGS);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.createExec(inspectContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToStartExec() throws IOException {
        StartExecParams startExecParams = StartExecParams.from(EXEC_ID);

        doReturn(new ByteArrayInputStream(DOCKER_RESPONSE_BYTES)).when(dockerResponse).getInputStream();

        dockerConnector.startExec(startExecParams, logMessageProcessor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/exec/" + startExecParams.getExecId() + "/start");
        verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
        verify(dockerConnection).header(eq("Content-Length"), anyInt());
        verify(dockerConnection).entity(any(byte[].class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileStartingExecIfResponseCodeIsNotSuccess() throws IOException {
        StartExecParams startExecParams = StartExecParams.from(EXEC_ID);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.startExec(startExecParams, logMessageProcessor);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldCallGetExecInfoWithParametersObject() throws IOException {
        GetExecInfoParams getExecInfoParams = GetExecInfoParams.from(EXEC_ID);

        ExecInfo execInfo = mock(ExecInfo.class);

        doReturn(execInfo).when(dockerConnector).getExecInfo(any(GetExecInfoParams.class));

        dockerConnector.getExecInfo(EXEC_ID);

        verify(dockerConnector).getExecInfo((GetExecInfoParams) captor.capture());

        assertEquals(captor.getValue(), getExecInfoParams);
    }

    @Test
    public void shouldBeAbleToGetExecInfo() throws IOException, JsonParseException {
        GetExecInfoParams getExecInfoParams = GetExecInfoParams.from(EXEC_ID);

        ExecInfo execInfo = mock(ExecInfo.class);

        doReturn(execInfo).when(dockerConnector).parseResponseStreamAndClose(inputStream, ExecInfo.class);

        ExecInfo returnedExecInfo =
                dockerConnector.getExecInfo(getExecInfoParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/exec/" + getExecInfoParams.getExecId() + "/json");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedExecInfo, execInfo);
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowIOExceptionWhileGettingExecInfoIfResponseCodeIsNotSuccess() throws IOException {
        GetExecInfoParams getExecInfoParams = GetExecInfoParams.from(EXEC_ID);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.getExecInfo(getExecInfoParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void topShouldBeAbleToGetProcesses() throws IOException, JsonParseException {
        TopParams topParams = TopParams.from(CONTAINER);

        ContainerProcesses containerProcesses = mock(ContainerProcesses.class);

        doReturn(containerProcesses).when(dockerConnector).parseResponseStreamAndClose(inputStream, ContainerProcesses.class);

        ContainerProcesses returnedContainerProcesses =
                dockerConnector.top(topParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/containers/" + topParams.getContainer() + "/top");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedContainerProcesses, containerProcesses);
    }

    @Test
    public void topWithPsArgsShouldBeAbleToGetProcesses() throws IOException, JsonParseException {
        TopParams topParams = TopParams.from(CONTAINER)
                                       .withPsArgs(CMD_ARGS);

        ContainerProcesses containerProcesses = mock(ContainerProcesses.class);

        doReturn(containerProcesses).when(dockerConnector).parseResponseStreamAndClose(inputStream, ContainerProcesses.class);

        ContainerProcesses returnedContainerProcesses =
                dockerConnector.top(topParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/containers/" + topParams.getContainer() + "/top");
        verify(dockerConnection).query(eq("ps_args"), anyString());
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedContainerProcesses, containerProcesses);
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void topShouldThrowIOExceptionWhileGettingProcessesIfResponseCodeIsNotSuccess() throws IOException {
        TopParams topParams = TopParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.top(topParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToGetResourcesFromContainer() throws IOException {
        GetResourceParams getResourceParams = GetResourceParams.from(CONTAINER, PATH_TO_FILE);

        when(dockerResponse.getInputStream())
                .thenReturn(new CloseConnectionInputStream(new ByteArrayInputStream(STREAM_DATA_BYTES), dockerConnection));

        String response = CharStreams.toString(new InputStreamReader(dockerConnector.getResource(getResourceParams)));

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/containers/" + getResourceParams.getContainer() + "/archive");
        verify(dockerConnection).query(eq("path"), eq(PATH_TO_FILE));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(response, STREAM_DATA);
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldProduceErrorWhenGetsResourcesFromContainerIfResponseCodeIsNotSuccess() throws IOException {
        GetResourceParams getResourceParams = GetResourceParams.from(CONTAINER, PATH_TO_FILE);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);
        when(dockerResponse.getInputStream())
                .thenReturn(new CloseConnectionInputStream(new ByteArrayInputStream(ERROR_MESSAGE.getBytes()), dockerConnection));

        dockerConnector.getResource(getResourceParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToPutResourcesIntoContainer() throws IOException {
        InputStream source = new CloseConnectionInputStream(new ByteArrayInputStream(STREAM_DATA_BYTES), dockerConnection);
        PutResourceParams putResourceParams = PutResourceParams.from(CONTAINER, PATH_TO_FILE)
                                                               .withSourceStream(source);

        dockerConnector.putResource(putResourceParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_PUT);
        verify(dockerConnection).path("/containers/" + putResourceParams.getContainer() + "/archive");
        verify(dockerConnection).query(eq("path"), eq(PATH_TO_FILE));
        verify(dockerConnection).header("Content-Type", ExtMediaType.APPLICATION_X_TAR);
        verify(dockerConnection).header(eq("Content-Length"), anyInt());
        verify(dockerConnection).entity(any(InputStream.class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldProduceErrorWhenPutsResourcesIntoContainerIfResponseCodeIsNotSuccess() throws IOException {
        InputStream source = new CloseConnectionInputStream(new ByteArrayInputStream(ERROR_MESSAGE.getBytes()), dockerConnection);
        PutResourceParams putResourceParams = PutResourceParams.from(CONTAINER, PATH_TO_FILE)
                                                               .withSourceStream(source);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);
        when(dockerResponse.getInputStream())
                .thenReturn(new ByteArrayInputStream(ERROR_MESSAGE.getBytes()));

        dockerConnector.putResource(putResourceParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToGetEvents() throws IOException {
        GetEventsParams getEventsParams = GetEventsParams.from();

        doReturn(new ByteArrayInputStream(STREAM_DATA_BYTES)).when(dockerResponse).getInputStream();

        dockerConnector.getEvents(getEventsParams, eventMessageProcessor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_GET);
        verify(dockerConnection).path("/events");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileGettingEventsIfResponseCodeIsNotSuccess() throws IOException {
        GetEventsParams getExecInfoParams = GetEventsParams.from();

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.getEvents(getExecInfoParams, eventMessageProcessor);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToBuildImage() throws IOException, InterruptedException {
        AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
        AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
        Map<String, AuthConfig> auth = new HashMap<>();
        auth.put("auth", authConfig);
        authConfigs.setConfigs(auth);
        String imageId = "37a7da3b7edc";

        BuildImageParams getEventsParams = BuildImageParams.from(dockerfile)
                                                           .withAuthConfigs(authConfigs);

        doReturn(new ByteArrayInputStream(("{\"stream\":\"Successfully built " + imageId + "\"}").getBytes()))
                .when(dockerResponse).getInputStream();

        String returnedImageId =
                dockerConnector.buildImage(getEventsParams, progressMonitor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/build");
        verify(dockerConnection).query("rm", 1);
        verify(dockerConnection).query("forcerm", 1);
        verify(dockerConnection).header("Content-Type", "application/x-compressed-tar");
        verify(dockerConnection).header(eq("Content-Length"), anyInt());
        verify(dockerConnection).header(eq("X-Registry-Config"), any(byte[].class));
        verify(dockerConnection).entity(any(InputStream.class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedImageId, imageId);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileBuildingImageIfResponseCodeIsNotSuccess() throws IOException, InterruptedException {
        AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
        AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
        Map<String, AuthConfig> auth = new HashMap<>();
        auth.put("auth", authConfig);
        authConfigs.setConfigs(auth);

        BuildImageParams getExecInfoParams = BuildImageParams.from(dockerfile)
                                                             .withAuthConfigs(authConfigs);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.buildImage(getExecInfoParams, progressMonitor);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test(expectedExceptions = java.io.IOException.class, expectedExceptionsMessageRegExp = "Docker image build failed")
    public void shouldThrowIOExceptionWhenBuildImageButNoSuccessMessageInResponse() throws IOException, InterruptedException {
        AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
        AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
        Map<String, AuthConfig> auth = new HashMap<>();
        auth.put("auth", authConfig);
        authConfigs.setConfigs(auth);

        BuildImageParams getEventsParams = BuildImageParams.from(dockerfile)
                                                           .withAuthConfigs(authConfigs);

        doReturn(new ByteArrayInputStream("c96d378b4911: Already exists".getBytes())).when(dockerResponse).getInputStream();

        dockerConnector.buildImage(getEventsParams, progressMonitor);

        verify(dockerResponse).getInputStream();
    }

    @Test
    public void shouldCallRemoveImageWithParametersObject() throws IOException {
        RemoveImageParams removeImageParams = RemoveImageParams.from(IMAGE);

        doNothing().when(dockerConnector).removeImage(removeImageParams);

        dockerConnector.removeImage(IMAGE);

        verify(dockerConnector).removeImage((RemoveImageParams) captor.capture());

        assertEquals(captor.getValue(), removeImageParams);
    }

    @Test
    public void shouldBeAbleToRemoveImage() throws IOException {
        RemoveImageParams removeImageParams = RemoveImageParams.from(IMAGE);

        dockerConnector.removeImage(removeImageParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_DELETE);
        verify(dockerConnection).path("/images/" + removeImageParams.getImage());
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileRemovingImageIfResponseCodeIsNotSuccess() throws IOException {
        RemoveImageParams removeImageParams = RemoveImageParams.from(IMAGE);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.removeImage(removeImageParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToTagImage() throws IOException {
        TagParams tagParams = TagParams.from(IMAGE, REPOSITORY);

        dockerConnector.tag(tagParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/images/" + tagParams.getImage() + "/tag");
        verify(dockerConnection).query("repo", tagParams.getRepository());
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileTaggingImageIfResponseCodeIsNotSuccess() throws IOException {
        TagParams tagParams = TagParams.from(IMAGE, REPOSITORY);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.tag(tagParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToPushImageToRepository() throws IOException, InterruptedException {
        PushParams pushParams = PushParams.from(REPOSITORY);

        when(dockerResponse.getInputStream()).thenReturn(
                new ByteArrayInputStream(("{\"status\":\"latest: digest: " + DIGEST + " size: 1234\"}").getBytes()));

        dockerConnector.push(pushParams, progressMonitor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/images/" + pushParams.getRepository() + "/push");
        verify(dockerConnection).header(eq("X-Registry-Auth"), any(AuthConfig.class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();
    }

    @Test
    public void shouldBeAbleToPushImageToRegistryRepository() throws IOException, InterruptedException {
        PushParams pushParams = PushParams.from(REPOSITORY)
                                          .withRegistry(REGISTRY);

        when(dockerResponse.getInputStream()).thenReturn(
                new ByteArrayInputStream(("{\"status\":\"latest: digest: " + DIGEST + " size: 1234\"}").getBytes()));

        dockerConnector.push(pushParams, progressMonitor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/images/" + pushParams.getRegistry() + '/' + pushParams.getRepository() + "/push");
        verify(dockerConnection).header(eq("X-Registry-Auth"), any(AuthConfig.class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhilePushingImageIfResponseCodeIsNotSuccess() throws IOException, InterruptedException {
        PushParams pushParams = PushParams.from(REPOSITORY);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.push(pushParams, progressMonitor);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test(expectedExceptions = DockerException.class,
          expectedExceptionsMessageRegExp = "Docker image was successfully pushed, but its digest wasn't obtained")
    public void shouldThrowDockerExceptionWhenPushImageButDigestWasNotParsed() throws IOException, InterruptedException {
        PushParams pushParams = PushParams.from(REPOSITORY);

        dockerConnector.push(pushParams, progressMonitor);
    }

    @Test
    public void shouldBeAbleToParseDigestFromDockerPushOutput() throws IOException, InterruptedException {
        String dockerPushOutput = "{\"progress\":\"[=====>              ] 25%\"}\n" +
                                  "{\"status\":\"Image already exists\"}\n" +
                                  "{\"progress\":\"[===============>    ] 75%\"}\n" +
                                  "{\"status\":\"" + TAG + ": digest: " + DIGEST + " size: 12345\"}";

        when(initialAuthConfig.getAuthConfigHeader()).thenReturn("auth-header");
        when(dockerResponse.getInputStream()).thenReturn(new ByteArrayInputStream(dockerPushOutput.getBytes()));

        assertEquals(DIGEST, dockerConnector.push(PushParams.from(REPOSITORY)
                                                            .withRegistry(REGISTRY)
                                                            .withTag(TAG),
                                                  progressMonitor));
    }

    @Test
    public void shouldBeAbleToCommitImage() throws IOException, JsonParseException {
        CommitParams commitParams = CommitParams.from(CONTAINER, REPOSITORY);

        ContainerCommitted containerCommitted = mock(ContainerCommitted.class);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_CREATED_CODE);
        when(containerCommitted.getId()).thenReturn(IMAGE);
        doReturn(containerCommitted).when(dockerConnector).parseResponseStreamAndClose(inputStream, ContainerCommitted.class);

        String returnedImage =
                dockerConnector.commit(commitParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/commit");
        verify(dockerConnection).query("container", commitParams.getContainer());
        verify(dockerConnection).query("repo", commitParams.getRepository());
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedImage, IMAGE);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileCommittingImageIfResponseCodeIsNotSuccess() throws IOException {
        CommitParams commitParams = CommitParams.from(CONTAINER, REPOSITORY);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.commit(commitParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToPullImageFromRepository() throws IOException, InterruptedException {
        PullParams pullParams = PullParams.from(IMAGE);

        when(dockerResponse.getInputStream()).thenReturn(new ByteArrayInputStream(STREAM_DATA_BYTES));

        dockerConnector.pull(pullParams, progressMonitor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/images/create");
        verify(dockerConnection).query("fromImage", pullParams.getImage());
        verify(dockerConnection).header(eq("X-Registry-Auth"), any(AuthConfig.class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();
    }

    @Test
    public void shouldBeAbleToPullImageFromRegistryRepository() throws IOException, InterruptedException {
        PullParams pullParams = PullParams.from(IMAGE)
                                          .withRegistry(REGISTRY);

        when(dockerResponse.getInputStream()).thenReturn(new ByteArrayInputStream(STREAM_DATA_BYTES));

        dockerConnector.pull(pullParams, progressMonitor);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/images/create");
        verify(dockerConnection).query("fromImage", pullParams.getRegistry() + '/' + pullParams.getImage());
        verify(dockerConnection).header(eq("X-Registry-Auth"), any(AuthConfig.class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhilePullingImageIfResponseCodeIsNotSuccess() throws IOException, InterruptedException {
        PullParams pullParams = PullParams.from(IMAGE);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.pull(pullParams, progressMonitor);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToCreateContainer() throws IOException, JsonParseException {
        CreateContainerParams createContainerParams = CreateContainerParams.from(new ContainerConfig());

        ContainerCreated containerCreated = mock(ContainerCreated.class);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_CREATED_CODE);
        doReturn(containerCreated).when(dockerConnector).parseResponseStreamAndClose(inputStream, ContainerCreated.class);

        ContainerCreated returnedContainerCreated =
                dockerConnector.createContainer(createContainerParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/containers/create");
        verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
        verify(dockerConnection).header(eq("Content-Length"), anyInt());
        verify(dockerConnection).entity(any(byte[].class));
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
        verify(dockerResponse).getInputStream();

        assertEquals(returnedContainerCreated, containerCreated);
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileCreatingContainerIfResponseCodeIsNotSuccess() throws IOException {
        CreateContainerParams createContainerParams = CreateContainerParams.from(new ContainerConfig());

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.createContainer(createContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldBeAbleToStartContainer() throws IOException {
        StartContainerParams startContainerParams = StartContainerParams.from(CONTAINER);

        dockerConnector.startContainer(startContainerParams);

        verify(dockerConnectionFactory).openConnection(any(URI.class));
        verify(dockerConnection).method(REQUEST_METHOD_POST);
        verify(dockerConnection).path("/containers/" + startContainerParams.getContainer() + "/start");
        verify(dockerConnection).request();
        verify(dockerResponse).getStatus();
    }

    @Test(expectedExceptions = DockerException.class, expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE)
    public void shouldThrowDockerExceptionWhileStartingContainerIfResponseCodeIsNotSuccess() throws IOException {
        StartContainerParams startContainerParams = StartContainerParams.from(CONTAINER);

        when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

        dockerConnector.startContainer(startContainerParams);

        verify(dockerResponse).getStatus();
        verify(dockerConnector).getDockerException(dockerResponse);
    }

    @Test
    public void shouldUseFirstLetterLowercaseWhenParseResponseStreamAndClose() throws IOException, JsonParseException {
        String response = "{\n" +
                          "  \"BuildTime\": \"2016-03-10T15:54:52.312835708+00:00\",\n" +
                          "  \"KernelVersion\": \"3.16.0-53-generic\",\n" +
                          "  \"Arch\": \"amd64\",\n" +
                          "  \"Os\": \"linux\",\n" +
                          "  \"GoVersion\": \"go1.5.3\",\n" +
                          "  \"GitCommit\": \"20f81dd\",\n" +
                          "  \"ApiVersion\": \"1.22\",\n" +
                          "  \"Version\": \"1.10.3\"\n" +
                          "}\n";
        Version version = new Version();
        version.setVersion("1.10.3");
        version.setApiVersion("1.22");
        version.setGoVersion("go1.5.3");
        version.setGitCommit("20f81dd");
        version.setOs("linux");
        version.setArch("amd64");

        Version parsedVersion = dockerConnector.parseResponseStreamAndClose(new ByteArrayInputStream(response.getBytes()), Version.class);

        assertEquals(parsedVersion, version);
    }

}
