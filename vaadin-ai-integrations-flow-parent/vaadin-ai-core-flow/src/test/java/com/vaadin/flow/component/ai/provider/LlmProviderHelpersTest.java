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

import com.vaadin.flow.component.ai.common.AiAttachment;

class LlmProviderHelpersTest {

    @Test
    void withSessionContext_appendsDelimitedBlockAfterUserText() {
        var result = LlmProviderHelpers.withSessionContext("Hello",
                "Tenant: acme");

        Assertions.assertTrue(
                result.startsWith("Hello\n\n"
                        + LlmProviderHelpers.SESSION_CONTEXT_OPEN + "\n"),
                "Context block must follow the user's text; got: " + result);
        Assertions.assertTrue(
                result.endsWith("\nTenant: acme\n"
                        + LlmProviderHelpers.SESSION_CONTEXT_CLOSE),
                "Context must be the last thing in the block; got: " + result);
        // The note's wording is tuned freely and deliberately not pinned
        // here; what the block must always do is introduce the context
        // with one.
        var block = result.substring(
                result.indexOf(LlmProviderHelpers.SESSION_CONTEXT_OPEN)
                        + LlmProviderHelpers.SESSION_CONTEXT_OPEN.length(),
                result.lastIndexOf(LlmProviderHelpers.SESSION_CONTEXT_CLOSE));
        Assertions.assertFalse(block.replace("Tenant: acme", "").isBlank(),
                "The block must introduce the context with a note; got: "
                        + result);
    }

    @Test
    void withSessionContext_withoutContext_returnsUserTextUnchanged() {
        Assertions.assertEquals("Hello",
                LlmProviderHelpers.withSessionContext("Hello", null));
        Assertions.assertEquals("Hello",
                LlmProviderHelpers.withSessionContext("Hello", "   "));
    }

    @Test
    void decodeAsUtf8_withValidUtf8_strictMode_returnsDecodedString() {
        var data = getValidUtf8Data();
        var input = new String(data, StandardCharsets.UTF_8);
        var result = LlmProviderHelpers.decodeAsUtf8(data, "test.txt", true);
        Assertions.assertEquals(input, result);
    }

    @Test
    void decodeAsUtf8_withValidUtf8_lenientMode_returnsDecodedString() {
        var data = getValidUtf8Data();
        var input = new String(data, StandardCharsets.UTF_8);
        var result = LlmProviderHelpers.decodeAsUtf8(data, "test.txt", false);
        Assertions.assertEquals(input, result);
    }

    @Test
    void decodeAsUtf8_withInvalidUtf8_strictMode_throwsException() {
        var data = getInvalidUtf8Data();
        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> LlmProviderHelpers.decodeAsUtf8(data, "binary.pdf",
                        true));
        Assertions.assertTrue(exception.getMessage().contains("binary.pdf"));
        Assertions.assertTrue(exception.getMessage().contains("invalid UTF-8"));
    }

    @Test
    void decodeAsUtf8_withInvalidUtf8_lenientMode_replacesInvalidSequences() {
        var data = getInvalidUtf8Data();
        var result = LlmProviderHelpers.decodeAsUtf8(data, "test.txt", false);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.startsWith("Hi"));
    }

    @Test
    void decodeAsUtf8_withEmptyData_returnsEmptyString() {
        var data = new byte[0];
        var result = LlmProviderHelpers.decodeAsUtf8(data, "empty.txt", true);
        Assertions.assertEquals("", result);
    }

    @Test
    void formatTextAttachment_returnsCorrectFormat() {
        var result = LlmProviderHelpers.formatTextAttachment("doc.txt",
                "Hello World");
        Assertions.assertEquals(
                "\n<attachment filename=\"doc.txt\">\nHello World\n</attachment>\n",
                result);
    }

    @Test
    void formatTextAttachment_withSpecialCharactersInContent_preservesContent() {
        var content = "Line1\nLine2\n<xml>tag</xml>";
        var result = LlmProviderHelpers.formatTextAttachment("file.txt",
                content);
        Assertions.assertTrue(result.contains(content));
        Assertions.assertTrue(result.contains("filename=\"file.txt\""));
    }

    @Test
    void formatTextAttachment_withEmptyContent_returnsValidFormat() {
        var result = LlmProviderHelpers.formatTextAttachment("empty.txt", "");
        Assertions.assertEquals(
                "\n<attachment filename=\"empty.txt\">\n\n</attachment>\n",
                result);
    }

    @Test
    void validateAttachment_withValidAttachment_doesNotThrow() {
        var attachment = new AiAttachment("file.txt", "text/plain",
                getValidUtf8Data());
        LlmProviderHelpers.validateAttachment(attachment);
    }

    @Test
    void validateAttachment_withNullAttachment_throwsNullPointerException() {
        var exception = Assertions.assertThrows(NullPointerException.class,
                () -> LlmProviderHelpers.validateAttachment(null));
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
