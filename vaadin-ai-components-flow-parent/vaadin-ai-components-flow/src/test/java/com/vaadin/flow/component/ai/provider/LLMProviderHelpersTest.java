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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.ai.common.AIAttachment;

public class LLMProviderHelpersTest {

    @Test
    public void decodeAsUtf8_withValidUtf8_strictMode_returnsDecodedString() {
        var data = getValidUtf8Data();
        var input = new String(data, StandardCharsets.UTF_8);
        var result = LLMProviderHelpers.decodeAsUtf8(data, "test.txt", true);
        Assert.assertEquals(input, result);
    }

    @Test
    public void decodeAsUtf8_withValidUtf8_lenientMode_returnsDecodedString() {
        var data = getValidUtf8Data();
        var input = new String(data, StandardCharsets.UTF_8);
        var result = LLMProviderHelpers.decodeAsUtf8(data, "test.txt", false);
        Assert.assertEquals(input, result);
    }

    @Test
    public void decodeAsUtf8_withInvalidUtf8_strictMode_throwsException() {
        var data = getInvalidUtf8Data();
        var exception = Assert.assertThrows(IllegalArgumentException.class,
                () -> LLMProviderHelpers.decodeAsUtf8(data, "binary.pdf",
                        true));
        Assert.assertTrue(exception.getMessage().contains("binary.pdf"));
        Assert.assertTrue(exception.getMessage().contains("invalid UTF-8"));
    }

    @Test
    public void decodeAsUtf8_withInvalidUtf8_lenientMode_replacesInvalidSequences() {
        var data = getInvalidUtf8Data();
        var result = LLMProviderHelpers.decodeAsUtf8(data, "test.txt", false);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.startsWith("Hi"));
    }

    @Test
    public void decodeAsUtf8_withEmptyData_returnsEmptyString() {
        var data = new byte[0];
        var result = LLMProviderHelpers.decodeAsUtf8(data, "empty.txt", true);
        Assert.assertEquals("", result);
    }

    @Test
    public void formatTextAttachment_returnsCorrectFormat() {
        var result = LLMProviderHelpers.formatTextAttachment("doc.txt",
                "Hello World");
        Assert.assertEquals(
                "\n<attachment filename=\"doc.txt\">\nHello World\n</attachment>\n",
                result);
    }

    @Test
    public void formatTextAttachment_withSpecialCharactersInContent_preservesContent() {
        var content = "Line1\nLine2\n<xml>tag</xml>";
        var result = LLMProviderHelpers.formatTextAttachment("file.txt",
                content);
        Assert.assertTrue(result.contains(content));
        Assert.assertTrue(result.contains("filename=\"file.txt\""));
    }

    @Test
    public void formatTextAttachment_withEmptyContent_returnsValidFormat() {
        var result = LLMProviderHelpers.formatTextAttachment("empty.txt", "");
        Assert.assertEquals(
                "\n<attachment filename=\"empty.txt\">\n\n</attachment>\n",
                result);
    }

    @Test
    public void validateAttachment_withValidAttachment_doesNotThrow() {
        var attachment = new AIAttachment("file.txt", "text/plain",
                getValidUtf8Data());
        LLMProviderHelpers.validateAttachment(attachment);
    }

    @Test
    public void validateAttachment_withNullAttachment_throwsNullPointerException() {
        var exception = Assert.assertThrows(NullPointerException.class,
                () -> LLMProviderHelpers.validateAttachment(null));
        Assert.assertEquals("Attachment must not be null",
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
