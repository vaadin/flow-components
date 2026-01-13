/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.upload;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * A component that displays the list of files being uploaded. When linked to an
 * {@link UploadManager}, it automatically displays upload progress, status, and
 * controls for each file.
 * <p>
 * The component automatically syncs files from the manager and forwards
 * retry/abort/start events back to the manager.
 * <p>
 * Example usage with UploadManager:
 *
 * <pre>
 * var manager = new UploadManager(uploadHandler);
 * var fileList = new UploadFileList(manager);
 * add(fileList);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see UploadManager
 */
@Tag("vaadin-upload-file-list")
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha1")
@JsModule("@vaadin/upload/src/vaadin-upload-file-list.js")
public class UploadFileList extends Component {

    /**
     * Creates a new empty file list without a manager. The manager must be
     * set later using {@link #setManager(UploadManager)}.
     */
    public UploadFileList() {
    }

    /**
     * Creates a new file list linked to the given manager.
     *
     * @param manager
     *            the upload manager to link to
     */
    public UploadFileList(UploadManager manager) {
        setManager(manager);
    }

    /**
     * Sets the upload manager that this file list is linked to. The file
     * list will display files managed by this manager.
     *
     * @param manager
     *            the upload manager, or {@code null} to unlink
     */
    public void setManager(UploadManager manager) {
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> setTarget(manager)));
        addAttachListener(event -> setTarget(manager));
    }

    private void setTarget(UploadManager manager) {
        getElement().executeJs("this.target = $0.manager", ComponentUtil.getData(manager.getOwner(), "upload-manager-connector-"+ manager.getId()));
    }
}
