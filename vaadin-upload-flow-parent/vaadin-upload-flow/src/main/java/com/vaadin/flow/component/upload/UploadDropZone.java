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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * A drop zone component for file uploads. When files are dropped on this
 * component, they are added to the linked {@link UploadManager}.
 * <p>
 * The component has minimal styling by default. When files are dragged over it,
 * the {@code dragover} attribute is set on the element, which can be used for
 * styling.
 * <p>
 * Example usage with UploadManager:
 *
 * <pre>
 * var manager = new UploadManager(uploadHandler);
 * var dropZone = new UploadDropZone(manager);
 * dropZone.add(new Span("Drop files here"));
 * add(dropZone);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see UploadManager
 */
@Tag("vaadin-upload-drop-zone")
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha1")
@JsModule("@vaadin/upload/src/vaadin-upload-drop-zone.js")
public class UploadDropZone extends Component implements HasComponents {

    /**
     * Creates a new empty drop zone without a manager. The manager must be set
     * later using {@link #setManager(UploadManager)}.
     */
    public UploadDropZone() {
    }

    /**
     * Creates a new drop zone linked to the given manager.
     *
     * @param manager
     *            the upload manager to link to
     */
    public UploadDropZone(UploadManager manager) {
        setManager(manager);
    }

    /**
     * Sets the upload manager that this drop zone is linked to. When files are
     * dropped, they will be added to the manager.
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
