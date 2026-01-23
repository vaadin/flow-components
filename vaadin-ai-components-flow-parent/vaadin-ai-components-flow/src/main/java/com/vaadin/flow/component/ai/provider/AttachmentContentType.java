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
package com.vaadin.flow.component.ai.provider;

/**
 * Supported content type categories for attachments.
 * <p>
 * Intended only for internal use and can be removed in the future.
 */
enum AttachmentContentType {
    /** Image content types (image/*). */
    IMAGE,
    /** Text content types (text/*). */
    TEXT,
    /** PDF content types (application/pdf, application/x-pdf). */
    PDF,
    /** Audio content types (audio/*). */
    AUDIO,
    /** Video content types (video/*). */
    VIDEO,
    /** Unsupported or unknown content types. */
    UNSUPPORTED;

    /**
     * Detects the content type category from a MIME type string.
     *
     * @param contentType
     *            the MIME content type
     * @return the corresponding category, or {@code UNSUPPORTED} if not
     *         recognized
     */
    public static AttachmentContentType fromMimeType(String contentType) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return IMAGE;
            }
            if (contentType.startsWith("text/")) {
                return TEXT;
            }
            if (contentType.startsWith("application/pdf")
                    || contentType.startsWith("application/x-pdf")) {
                return PDF;
            }
            if (contentType.startsWith("audio/")) {
                return AUDIO;
            }
            if (contentType.startsWith("video/")) {
                return VIDEO;
            }
        }
        return UNSUPPORTED;
    }
}
