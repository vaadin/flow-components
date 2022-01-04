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

import com.vaadin.flow.component.ComponentEvent;

/**
 * Sent when the file selected for upload doesn't meet the constraints specified
 * on {@link Upload}
 *
 * @author Vaadin Ltd.
 */
public class FileRejectedEvent extends ComponentEvent<Upload> {

    private String errorMessage;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source
     *            the source component
     * @param errorMessage
     *            the error message
     */
    public FileRejectedEvent(Upload source, String errorMessage) {
        super(source, true);
        this.errorMessage = errorMessage;
    }

    /**
     * Get the error message
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
