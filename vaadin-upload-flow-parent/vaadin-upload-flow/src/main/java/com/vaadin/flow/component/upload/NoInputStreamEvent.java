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

/**
 * FailedEvent that indicates that an input stream could not be obtained.
 */
public class NoInputStreamEvent extends FailedEvent {

    /**
     * Create an instance of the event.
     *
     * @param source
     *            the source of the file
     * @param fileName
     *            the received file name
     * @param mimeType
     *            the MIME type of the received file
     * @param length
     *            the length of the received file
     */
    public NoInputStreamEvent(Upload source, String fileName, String mimeType,
            long length) {
        super(source, fileName, mimeType, length);
    }

}
