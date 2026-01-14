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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * A button component for triggering file uploads. When clicked, it opens a file
 * picker dialog. This component is designed to work with {@link UploadManager}.
 * <p>
 * Example usage:
 *
 * <pre>
 * var manager = new UploadManager(uploadHandler);
 * var button = new UploadButton(manager);
 * button.add(new Span("Select Files"));
 * add(button);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see UploadManager
 */
@Tag("vaadin-upload-button")
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha1")
@JsModule("@vaadin/upload/src/vaadin-upload-button.js")
public class UploadButton extends Button {

    /**
     * Creates a new upload button without a manager. The manager must be set
     * later using {@link #setManager(UploadManager)}.
     */
    public UploadButton() {
    }

    /**
     * Creates a new upload button linked to the given manager.
     *
     * @param manager
     *            the upload manager to link to
     */
    public UploadButton(UploadManager manager) {
        setManager(manager);
    }

    /**
     * Sets the upload manager that this button is linked to. When files are
     * selected, they will be added to the manager.
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
        getElement().executeJs("this.manager = $0.manager", manager.getConnector());
    }
}
