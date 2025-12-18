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

import java.io.Serializable;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.UploadHandler;

/**
 * A non-visual orchestrator that manages file uploads using external UI
 * components. It programmatically creates a JavaScript {@code UploadOrchestrator}
 * instance and connects it to the provided drop zone, add button, and file list
 * components.
 * <p>
 * Unlike the {@link Upload} component which provides a complete upload UI, this
 * orchestrator is invisible and delegates all UI rendering to external
 * components that you provide. This allows complete freedom in designing the
 * upload experience.
 * <p>
 * The orchestrator automatically initializes when the components are attached
 * to the UI and cleans up when they are detached.
 * <p>
 * Note: The JavaScript module for the orchestrator is loaded via
 * {@link UploadDropZone}. Ensure at least an {@link UploadDropZone} is used
 * to guarantee the module is bundled.
 * <p>
 * Example usage:
 *
 * <pre>
 * // Create external UI components
 * var dropZone = new UploadDropZone();
 * var addButton = new NativeButton("Select Files");
 * var fileList = new UploadFileList();
 *
 * // Create orchestrator and connect components
 * var orchestrator = new UploadOrchestrator();
 * orchestrator.setDropZone(dropZone);
 * orchestrator.setAddButton(addButton);
 * orchestrator.setFileList(fileList);
 * orchestrator.setUploadHandler(UploadHandler.inMemory((metadata, data) -&gt; {
 *     // Process uploaded file
 * }));
 *
 * // Add UI components to the layout
 * add(dropZone, addButton, fileList);
 * </pre>
 *
 * @author Vaadin Ltd.
 * @see Upload
 * @see UploadDropZone
 * @see UploadFileList
 */
public class UploadOrchestrator implements Serializable {

    private Component addButton;
    private UploadDropZone dropZone;
    private HasUploadFileList fileList;
    private UploadHandler uploadHandler;
    private String targetName = "upload";

    /**
     * Creates a new upload orchestrator.
     */
    public UploadOrchestrator() {
    }

    /**
     * Creates a new upload orchestrator with the given upload handler.
     *
     * @param handler
     *            the upload handler to use
     */
    public UploadOrchestrator(UploadHandler handler) {
        this();
        setUploadHandler(handler);
    }

    /**
     * Sets a custom component to be used as the add button. When clicked, this
     * component will open the file selection dialog.
     *
     * @param addButton
     *            the component to use as the add button, or {@code null} to
     *            remove
     */
    public void setAddButton(Component addButton) {
        this.addButton = addButton;
        initializeIfReady();
    }

    /**
     * Gets the custom component used as the add button.
     *
     * @return the add button component, or {@code null} if not set
     */
    public Component getAddButton() {
        return addButton;
    }

    /**
     * Sets the drop zone component. Files can be dragged and dropped onto this
     * component to upload them.
     *
     * @param dropZone
     *            the drop zone component, or {@code null} to remove
     */
    public void setDropZone(UploadDropZone dropZone) {
        this.dropZone = dropZone;
        initializeIfReady();
    }

    /**
     * Gets the drop zone component.
     *
     * @return the drop zone component, or {@code null} if not set
     */
    public UploadDropZone getDropZone() {
        return dropZone;
    }

    /**
     * Sets a custom component to display the list of files being uploaded.
     *
     * @param <T>
     *            the type of the file list component
     * @param fileList
     *            the file list component, or {@code null} to remove
     */
    public <T extends Component & HasUploadFileList> void setFileList(
            T fileList) {
        this.fileList = fileList;
        initializeIfReady();
    }

    /**
     * Gets the file list component.
     *
     * @return the file list component, or {@code null} if not set
     */
    public HasUploadFileList getFileList() {
        return fileList;
    }

    /**
     * Sets the upload handler that processes uploaded files.
     *
     * @param handler
     *            the upload handler, not {@code null}
     */
    public void setUploadHandler(UploadHandler handler) {
        setUploadHandler(handler, "upload");
    }

    /**
     * Sets the upload handler that processes uploaded files.
     *
     * @param handler
     *            the upload handler, not {@code null}
     * @param targetName
     *            the endpoint name (single path segment), used as the last path
     *            segment of the upload URL, not {@code null} or blank
     */
    public void setUploadHandler(UploadHandler handler, String targetName) {
        Objects.requireNonNull(handler, "UploadHandler cannot be null");
        Objects.requireNonNull(targetName, "The target name cannot be null");
        if (targetName.isBlank()) {
            throw new IllegalArgumentException(
                    "The target name cannot be blank");
        }
        this.uploadHandler = handler;
        this.targetName = targetName;
        initializeIfReady();
    }

    private void initializeIfReady() {
        // We need at least one component and a handler to initialize
        Element anchorElement = getAnchorElement();
        if (anchorElement == null || uploadHandler == null) {
            return;
        }

        // Register the stream resource with the anchor element
        StreamResourceRegistry.ElementStreamResource elementStreamResource = new StreamResourceRegistry.ElementStreamResource(
                uploadHandler, anchorElement) {
            @Override
            public String getName() {
                return targetName;
            }
        };

        // Set the target attribute and create the JS orchestrator
        anchorElement.getNode().runWhenAttached(ui -> ui.beforeClientResponse(
                anchorElement.getComponent().orElse(null), context -> {
                    anchorElement.setAttribute("data-upload-target",
                            elementStreamResource);

                    // Create the JS orchestrator using element.executeJs
                    // This automatically handles lifecycle (cleanup on detach)
                    anchorElement.executeJs(
                            "const target = this.getAttribute('data-upload-target');" +
                            "const orchestrator = new window.Vaadin.UploadOrchestrator({ target: target });" +
                            "if ($0) orchestrator.dropZone = $0;" +
                            "if ($1) orchestrator.addButton = $1;" +
                            "if ($2) orchestrator.fileList = $2;",
                            dropZone != null ? dropZone.getElement() : null,
                            addButton != null ? addButton.getElement() : null,
                            fileList != null ? ((Component) fileList).getElement() : null);
                }));
    }

    private Element getAnchorElement() {
        if (dropZone != null) {
            return dropZone.getElement();
        }
        if (addButton != null) {
            return addButton.getElement();
        }
        if (fileList != null) {
            return ((Component) fileList).getElement();
        }
        return null;
    }
}
