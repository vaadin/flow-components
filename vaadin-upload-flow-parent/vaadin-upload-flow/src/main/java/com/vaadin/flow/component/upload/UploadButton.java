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
 * @since 25.1
 */
@Tag("vaadin-upload-button")
@NpmPackage(value = "@vaadin/upload", version = "25.4.0-alpha2")
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
     * Gets the device input that is used for capturing a new file.
     *
     * @return the device input used for capturing a new file, or {@code null}
     *         if not set
     * @see #setCapture(UploadCapture)
     * @since 25.4
     */
    public UploadCapture getCapture() {
        return UploadCapture.fromValue(getElement().getProperty("capture"));
    }

    /**
     * Sets the device input to use for capturing a new file, for example
     * {@link UploadCapture#ENVIRONMENT} to take a photo with the back camera of
     * a phone. On devices that support it, clicking the button then opens the
     * camera or microphone directly instead of the file browser. The type of
     * input is determined by the accepted MIME types of the manager, for
     * example {@code "image/*"} for the camera, see
     * {@link UploadManager#setAcceptedMimeTypes(String...)}.
     * <p>
     * This is only a hint to the browser. Devices without such an input, for
     * example desktop browsers, ignore it and open the file browser. It does
     * not restrict which files can be uploaded.
     * <p>
     * The default is {@code null}, which opens the file browser.
     *
     * @param capture
     *            the device input to use for capturing a new file, or
     *            {@code null} to choose existing files
     * @since 25.4
     */
    public void setCapture(UploadCapture capture) {
        if (capture == null) {
            getElement().removeProperty("capture");
        } else {
            getElement().setProperty("capture", capture.getValue());
        }
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
