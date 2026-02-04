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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

import com.vaadin.flow.component.ai.component.AiAttachment;

/**
 * Utility methods for LLM provider implementations.
 * <p>
 * Intended only for internal use and can be removed in the future.
 */
final class LLMProviderHelpers {

    /**
     * Decodes byte array as UTF-8 text.
     *
     * @param data
     *            the byte array to decode
     * @param fileName
     *            the file name for error messages
     * @param strict
     *            if {@code true}, throws exception on invalid UTF-8; if
     *            {@code false}, replaces invalid sequences
     * @return the decoded string
     * @throws IllegalArgumentException
     *             if {@code strict} is {@code true} and data contains invalid
     *             UTF-8
     */
    public static String decodeAsUtf8(byte[] data, String fileName,
            boolean strict) {
        if (!strict) {
            return new String(data, StandardCharsets.UTF_8);
        }
        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException(
                    "File '" + fileName + "' contains invalid UTF-8 data.", e);
        }
    }

    public static String getBase64Data(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Formats text content as an attachment block.
     *
     * @param fileName
     *            the file name to include in the attachment tag
     * @param content
     *            the text content
     * @return formatted attachment string
     */
    public static String formatTextAttachment(String fileName, String content) {
        return "\n<attachment filename=\"" + fileName + "\">\n" + content
                + "\n</attachment>\n";
    }

    /**
     * Validates that an attachment and its required fields are not
     * {@code null}.
     *
     * @param attachment
     *            the attachment to validate
     * @throws NullPointerException
     *             if attachment, content type, or data is {@code null}
     */
    public static void validateAttachment(AiAttachment attachment) {
        Objects.requireNonNull(attachment, "Attachment must not be null");
        Objects.requireNonNull(attachment.mimeType(),
                "Attachment content type must not be null");
        Objects.requireNonNull(attachment.data(),
                "Attachment data must not be null");
    }
}
