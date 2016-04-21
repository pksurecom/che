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
package org.eclipse.che.plugin.docker.client.json;

import java.util.Arrays;
import java.util.Map;

/**
 * Defines information about container from list containers.
 *
 * @author Alexander Andrienko
 */
public class ContainerListEntry {
    private String              id;
    private String[]            names;
    private String              image;
    private String              imageID;
    private String              command;
    private long                created;
    private String              status;
    private Port[]              ports;
    private Map<String, String> labels;
    private int                 sizeRw;
    private int                 sizeRootFs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Port[] getPorts() {
        return ports;
    }

    public void setPorts(Port[] ports) {
        this.ports = ports;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public int getSizeRw() {
        return sizeRw;
    }

    public void setSizeRw(int sizeRw) {
        this.sizeRw = sizeRw;
    }

    public int getSizeRootFs() {
        return sizeRootFs;
    }

    public void setSizeRootFs(int sizeRootFs) {
        this.sizeRootFs = sizeRootFs;
    }

    @Override
    public String toString() {
        return "ContainerListEntry{" +
               "id='" + id + '\'' +
               ", names=" + Arrays.toString(names) +
               ", image='" + image + '\'' +
               ", imageID='" + imageID + '\'' +
               ", command='" + command + '\'' +
               ", created=" + created +
               ", status='" + status + '\'' +
               ", ports=" + Arrays.toString(ports) +
               ", labels=" + labels +
               ", sizeRw=" + sizeRw +
               ", sizeRootFs=" + sizeRootFs +
               '}';
    }
}
