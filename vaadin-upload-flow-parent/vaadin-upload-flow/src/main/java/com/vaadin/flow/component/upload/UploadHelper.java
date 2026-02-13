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

import com.vaadin.flow.server.streams.UploadHandler;

/**
 * Utility methods for Upload and UploadManager components.
 * <p>
 * Intended only for internal use and can be removed or changed in the future.
 */
public class UploadHelper {

    /**
     * Checks whether the given {@link UploadManager} has an
     * {@link UploadHandler UploadHandler} configured.
     *
     * @param uploadManager
     *            the upload manager to check, not {@code null}
     * @return {@code true} if the upload manager has an upload handler
     *         configured, {@code false} otherwise
     */
    public static boolean hasUploadHandler(UploadManager uploadManager) {
        return uploadManager.getConnector().getElement().hasAttribute("target");
    }
}
