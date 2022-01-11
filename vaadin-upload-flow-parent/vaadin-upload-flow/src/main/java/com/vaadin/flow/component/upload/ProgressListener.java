/*
 * Copyright 2000-2022 Vaadin Ltd.
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

/**
 * ProgressListener receives events to track progress of upload.
 */
@FunctionalInterface
public interface ProgressListener extends Serializable {

    /**
     * Updates progress to listener.
     *
     * @param readBytes
     *            bytes transferred
     * @param contentLength
     *            total size of file currently being uploaded, -1 if unknown
     */
    void updateProgress(long readBytes, long contentLength);
}
