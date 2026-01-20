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
    public final void setManager(UploadManager manager) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> setTarget(manager)));
        addAttachListener(event -> setTarget(manager));
    }

    private void setTarget(UploadManager manager) {
        getElement().executeJs("this.manager = $0.manager",
                manager.getConnector());
    }

    /**
     * Sets whether this button is enabled. When disabled, the button cannot be
     * used to select files.
     * <p>
     * <strong>Note:</strong> Disabling this button only affects the UI and does
     * not prevent a malicious client from initiating uploads. To securely
     * prevent uploads, use {@link UploadManager#setEnabled(boolean)}.
     *
     * @param enabled
     *            {@code true} to enable the button, {@code false} to disable
     * @see UploadManager#setEnabled(boolean)
     */
    @SuppressWarnings("java:S1185") // Override is intentional to provide
                                    // specific Javadoc
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /**
     * Sets the capture attribute for mobile file input. This controls whether
     * to use the device's camera or microphone to capture files directly.
     * <p>
     * Common values are:
     * <ul>
     * <li>{@code "user"} - Use the front-facing camera</li>
     * <li>{@code "environment"} - Use the back-facing camera</li>
     * </ul>
     *
     * @param capture
     *            the capture attribute value, or {@code null} to remove
     */
    public void setCapture(String capture) {
        getElement().setProperty("capture", capture);
    }

    /**
     * Gets the capture attribute for mobile file input.
     *
     * @return the capture attribute value, or {@code null} if not set
     */
    public String getCapture() {
        return getElement().getProperty("capture");
    }
}
