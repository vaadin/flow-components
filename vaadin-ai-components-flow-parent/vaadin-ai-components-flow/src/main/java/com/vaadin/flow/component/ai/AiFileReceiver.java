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
package com.vaadin.flow.component.ai;

import java.io.Serializable;

import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Interface for file upload components that are used in an AI conversation.
 *
 * @author Vaadin Ltd
 */
public interface AiFileReceiver extends Serializable {

    /**
     * Sets the upload handler for this file receiver.
     * <p>
     * Default implementation does nothing.
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
     * Default implementation does nothing.
     *
     * @param listener
     *            the listener to add, receives the removed file name
     */
    default void addFileRemovedListener(
            java.util.function.Consumer<String> listener) {
        // Default implementation - no-op
    }

    /**
     * Clears the list of uploaded files.
     * <p>
     * Default implementation does nothing.
     */
    default void clearFileList() {
        // Default implementation - no-op
    }
}
