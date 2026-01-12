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
 * var fileListBox = new UploadFileListBox(manager);
 * add(fileListBox);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see UploadManager
 */
@Tag("vaadin-upload-file-list-box")
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha1")
@JsModule("@vaadin/upload/src/vaadin-upload-file-list-box.js")
@JsModule("./vaadin-upload-manager-connector.js")
public class UploadFileListBox extends Component {

    /**
     * Creates a new empty file list box without a manager. The manager must be
     * set later using {@link #setManager(UploadManager)}.
     */
    public UploadFileListBox() {
    }

    /**
     * Creates a new file list box linked to the given manager.
     *
     * @param manager
     *            the upload manager to link to
     */
    public UploadFileListBox(UploadManager manager) {
        setManager(manager);
    }

    /**
     * Sets the upload manager that this file list box is linked to. The file
     * list box will display files managed by this manager.
     *
     * @param manager
     *            the upload manager, or {@code null} to unlink
     */
    public void setManager(UploadManager manager) {
        if (manager != null) {
            String managerId = manager.getId();
            getElement().getNode().runWhenAttached(
                    ui -> ui.beforeClientResponse(this, context -> setTarget(managerId)));
            addAttachListener(event -> setTarget(managerId));
        }
    }

    private void setTarget(String managerId) {
        getElement().executeJs(
                "this.target = window.Vaadin.Upload.UploadManager.getUploadManager($0);",
                managerId);
    }
}
