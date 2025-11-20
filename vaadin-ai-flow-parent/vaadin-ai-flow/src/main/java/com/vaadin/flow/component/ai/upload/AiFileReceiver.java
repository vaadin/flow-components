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
package com.vaadin.flow.component.ai.upload;

import com.vaadin.flow.server.streams.UploadHandler;

import java.io.Serializable;

/**
 * Interface for components that can receive file uploads for AI orchestrators.
 * <p>
 * Components implementing this interface can accept file uploads and notify
 * listeners when files are successfully uploaded.
 * </p>
 *
 * @author Vaadin Ltd
 */
public interface AiFileReceiver extends Serializable {

    /**
     * Adds a listener for successful upload events.
     *
     * @param listener
     *            the listener to add
     */
    void addSucceededListener(FileUploadListener listener);

    /**
     * Sets the upload handler for this file receiver.
     * <p>
     * The upload handler processes uploaded files and stores them in memory or
     * on disk. This method should be called to configure how files are handled
     * when uploaded.
     * </p>
     * <p>
     * Default implementation does nothing. Components implementing this
     * interface should override this method if they support upload handlers.
     * </p>
     *
     * @param uploadHandler
     *            the upload handler to use
     */
    default void setUploadHandler(UploadHandler uploadHandler) {
        // Default implementation - no-op
    }

    /**
     * Adds a listener for file removed events.
     * <p>
     * This listener is called when a user removes a file from the upload
     * component before submitting.
     * </p>
     * <p>
     * Default implementation does nothing. Components implementing this
     * interface should override this method if they support file removal
     * events. The implementation should extract the file name from the event
     * and pass it to the listener.
     * </p>
     *
     * @param listener
     *            the listener to add, receives the removed file name
     */
    default void addFileRemovedListener(
            java.util.function.Consumer<String> listener) {
        // Default implementation - no-op
    }

    /**
     * Clears the list of uploaded files from the UI.
     * <p>
     * This should be called after files have been processed to reset the
     * upload component state.
     * </p>
     * <p>
     * Default implementation does nothing. Components implementing this
     * interface should override this method if they support clearing the file
     * list.
     * </p>
     */
    default void clearFileList() {
        // Default implementation - no-op
    }
}
