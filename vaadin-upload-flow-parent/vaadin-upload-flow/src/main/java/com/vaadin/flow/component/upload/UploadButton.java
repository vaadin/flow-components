/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.Objects;

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
 * var button = new UploadButton("Select Files", manager);
 * add(button);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see UploadManager
 */
@Tag("vaadin-upload-button")
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-beta1")
@JsModule("@vaadin/upload/src/vaadin-upload-button.js")
public class UploadButton extends Button implements HasUploadManager {

    /**
     * Creates a new upload button without a manager. The manager must be set
     * later using {@link #setUploadManager(UploadManager)}.
     */
    public UploadButton() {
    }

    /**
     * Creates a new upload button linked to the given manager.
     *
     * @param manager
     *            the upload manager to link to, not {@code null}
     * @throws NullPointerException
     *             if manager is {@code null}
     */
    public UploadButton(UploadManager manager) {
        setUploadManager(Objects.requireNonNull(manager,
                "manager cannot be null, use the default constructor instead"));
    }

    /**
     * Creates a new upload button with the given text, linked to the given
     * manager.
     *
     * @param text
     *            the button text
     * @param manager
     *            the upload manager to link to, not {@code null}
     * @throws NullPointerException
     *             if manager is {@code null}
     */
    public UploadButton(String text, UploadManager manager) {
        this(manager);
        doSetText(text);
    }

    private void doSetText(String text) {
        super.setText(text);
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

}
