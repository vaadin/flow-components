/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.ComponentEvent;

/**
 * Sent when a file selected for upload is removed.
 *
 * @author Vaadin Ltd.
 */
public class FileRemovedEvent extends ComponentEvent<Upload> {

    private final String fileName;

    /**
     * Creates a new event using the given source and the removed file name.
     *
     * @param source
     *            the source component
     * @param fileName
     *            the removed file name
     */
    public FileRemovedEvent(Upload source, String fileName) {
        super(source, true);
        this.fileName = fileName;
    }

    /**
     * Get the removed file name.
     *
     * @return the removed file name
     */
    public String getFileName() {
        return fileName;
    }
}
