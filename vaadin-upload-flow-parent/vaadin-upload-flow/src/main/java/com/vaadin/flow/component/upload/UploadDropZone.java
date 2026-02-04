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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
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
@NpmPackage(value = "@vaadin/upload", version = "25.1.0-alpha6")
@JsModule("@vaadin/upload/src/vaadin-upload-drop-zone.js")
public class UploadDropZone extends Component
        implements HasComponents, HasUploadManager, HasSize {

    /**
     * Creates a new empty drop zone without a manager. The manager must be set
     * later using {@link #setUploadManager(UploadManager)}.
     */
    public UploadDropZone() {
    }

    /**
     * Creates a new drop zone linked to the given manager.
     *
     * @param manager
     *            the upload manager to link to, not {@code null}
     * @throws NullPointerException
     *             if manager is {@code null}
     */
    public UploadDropZone(UploadManager manager) {
        setUploadManager(Objects.requireNonNull(manager,
                "manager cannot be null, use the default constructor instead"));
    }

    /**
     * Sets whether this drop zone is enabled. When disabled, the drop zone will
     * not accept dropped files.
     * <p>
     * <strong>Note:</strong> Disabling this drop zone only affects the UI and
     * does not prevent a malicious client from initiating uploads. To securely
     * prevent uploads, use {@link UploadManager#setEnabled(boolean)}.
     *
     * @param enabled
     *            {@code true} to enable the drop zone, {@code false} to disable
     * @see UploadManager#setEnabled(boolean)
     */
    @SuppressWarnings("java:S1185") // Override is intentional to provide
                                    // specific Javadoc
    @Override
    public void setEnabled(boolean enabled) {
        HasComponents.super.setEnabled(enabled);
    }
}
