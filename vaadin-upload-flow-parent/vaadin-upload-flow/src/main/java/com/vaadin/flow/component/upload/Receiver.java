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

import java.io.OutputStream;
import java.io.Serializable;

/**
 * Interface that must be implemented by the upload receivers to provide the
 * Upload component an output stream to write the uploaded data.
 *
 * @author Vaadin Ltd.
 */
@FunctionalInterface
public interface Receiver extends Serializable {

    /**
     * Invoked when a new upload arrives.
     *
     * @param fileName
     *            the desired filename of the upload, usually as specified by
     *            the client
     * @param mimeType
     *            the MIME type of the uploaded file
     * @return stream to which the uploaded file should be written
     */
    OutputStream receiveUpload(String fileName, String mimeType);
}
