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
package com.vaadin.flow.component.ai.common;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an attachment that can be sent to an LLM and displayed in
 * messages.
 * <p>
 * This record holds the raw file data for sending to the LLM provider.
 *
 * @param name
 *            the display name of the attachment (e.g., file name), not
 *            {@code null}
 * @param mimeType
 *            the MIME type of the attachment (e.g., "image/png",
 *            "application/pdf"), not {@code null}
 * @param data
 *            the raw file data, not {@code null}
 * @author Vaadin Ltd
 */
public record AIAttachment(String name, String mimeType,
        byte[] data) implements Serializable {

    /**
     * Creates a new attachment.
     *
     * @param name
     *            the display name of the attachment
     * @param mimeType
     *            the MIME type of the attachment
     * @param data
     *            the raw file data
     * @throws NullPointerException
     *             if any parameter is null
     */
    public AIAttachment {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(mimeType, "MIME type cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");
    }
}
