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

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.common.AIAttachment;

class LLMProviderHelpersTest {

    @Test
    void decodeAsUtf8_withValidUtf8_strictMode_returnsDecodedString() {
        var data = getValidUtf8Data();
        var input = new String(data, StandardCharsets.UTF_8);
        var result = LLMProviderHelpers.decodeAsUtf8(data, "test.txt", true);
        Assertions.assertEquals(input, result);
    }

    @Test
    void decodeAsUtf8_withValidUtf8_lenientMode_returnsDecodedString() {
        var data = getValidUtf8Data();
        var input = new String(data, StandardCharsets.UTF_8);
        var result = LLMProviderHelpers.decodeAsUtf8(data, "test.txt", false);
        Assertions.assertEquals(input, result);
    }

    @Test
    void decodeAsUtf8_withInvalidUtf8_strictMode_throwsException() {
        var data = getInvalidUtf8Data();
        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> LLMProviderHelpers.decodeAsUtf8(data, "binary.pdf",
                        true));
        Assertions.assertTrue(exception.getMessage().contains("binary.pdf"));
        Assertions.assertTrue(exception.getMessage().contains("invalid UTF-8"));
    }

    @Test
    void decodeAsUtf8_withInvalidUtf8_lenientMode_replacesInvalidSequences() {
        var data = getInvalidUtf8Data();
        var result = LLMProviderHelpers.decodeAsUtf8(data, "test.txt", false);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.startsWith("Hi"));
    }

    @Test
    void decodeAsUtf8_withEmptyData_returnsEmptyString() {
        var data = new byte[0];
        var result = LLMProviderHelpers.decodeAsUtf8(data, "empty.txt", true);
        Assertions.assertEquals("", result);
    }

    @Test
    void formatTextAttachment_returnsCorrectFormat() {
        var result = LLMProviderHelpers.formatTextAttachment("doc.txt",
                "Hello World");
        Assertions.assertEquals(
                "\n<attachment filename=\"doc.txt\">\nHello World\n</attachment>\n",
                result);
    }

    @Test
    void formatTextAttachment_withSpecialCharactersInContent_preservesContent() {
        var content = "Line1\nLine2\n<xml>tag</xml>";
        var result = LLMProviderHelpers.formatTextAttachment("file.txt",
                content);
        Assertions.assertTrue(result.contains(content));
        Assertions.assertTrue(result.contains("filename=\"file.txt\""));
    }

    @Test
    void formatTextAttachment_withEmptyContent_returnsValidFormat() {
        var result = LLMProviderHelpers.formatTextAttachment("empty.txt", "");
        Assertions.assertEquals(
                "\n<attachment filename=\"empty.txt\">\n\n</attachment>\n",
                result);
    }

    @Test
    void validateAttachment_withValidAttachment_doesNotThrow() {
        var attachment = new AIAttachment("file.txt", "text/plain",
                getValidUtf8Data());
        LLMProviderHelpers.validateAttachment(attachment);
    }

    @Test
    void validateAttachment_withNullAttachment_throwsNullPointerException() {
        var exception = Assertions.assertThrows(NullPointerException.class,
                () -> LLMProviderHelpers.validateAttachment(null));
        Assertions.assertEquals("Attachment must not be null",
                exception.getMessage());
    }

    private static byte[] getInvalidUtf8Data() {
        return new byte[] { 'H', 'i', (byte) 0xFF, (byte) 0xFE };
    }

    private static byte[] getValidUtf8Data() {
        return "Hello, World! UTF-8: café résumé naïve"
                .getBytes(StandardCharsets.UTF_8);
    }
}
