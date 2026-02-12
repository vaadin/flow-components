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

/**
 * Specifies the upload format to use when sending files to the server.
 */
public enum UploadFormat {
    /**
     * Send files as raw binary data with the file's MIME type as Content-Type.
     * This is the default. Typically less hassle for example with large files
     * in application and proxy servers.
     */
    RAW,
    /**
     * Sends individual files as multi-part requests. Note, that the client-side
     * component still sends only one file per request. Can be useful for
     * certain application security proxies that may drop large request bodies
     * unless they are multipart requests.
     */
    MULTIPART
}
