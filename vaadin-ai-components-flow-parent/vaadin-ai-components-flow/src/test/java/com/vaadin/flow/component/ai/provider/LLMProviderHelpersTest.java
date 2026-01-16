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
import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;

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
    public void toBase64DataUrl_withImageData_returnsCorrectFormat() {
        var data = getValidUtf8Data();
        var contentType = "image/png";
        var result = LLMProviderHelpers.toBase64DataUrl(data, contentType);
        Assert.assertTrue(result.startsWith("data:image/png;base64,"));
        var base64Part = result.substring("data:image/png;base64,".length());
        var decoded = Base64.getDecoder().decode(base64Part);
        Assert.assertArrayEquals(data, decoded);
    }

    @Test
    public void toBase64DataUrl_withDifferentContentType_includesContentType() {
        var data = getValidUtf8Data();
        var pngResult = LLMProviderHelpers.toBase64DataUrl(data, "image/png");
        var jpegResult = LLMProviderHelpers.toBase64DataUrl(data, "image/jpeg");
        var gifResult = LLMProviderHelpers.toBase64DataUrl(data, "image/gif");
        Assert.assertTrue(pngResult.startsWith("data:image/png;base64,"));
        Assert.assertTrue(jpegResult.startsWith("data:image/jpeg;base64,"));
        Assert.assertTrue(gifResult.startsWith("data:image/gif;base64,"));
    }

    @Test
    public void toBase64DataUrl_withEmptyData_returnsValidDataUrl() {
        var data = new byte[0];
        var result = LLMProviderHelpers.toBase64DataUrl(data, "image/png");
        Assert.assertEquals("data:image/png;base64,", result);
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
        var attachment = new TestAttachment(getValidUtf8Data(), "text/plain",
                "file.txt");
        LLMProviderHelpers.validateAttachment(attachment);
    }

    @Test
    public void validateAttachment_withNullAttachment_throwsNullPointerException() {
        var exception = Assert.assertThrows(NullPointerException.class,
                () -> LLMProviderHelpers.validateAttachment(null));
        Assert.assertEquals("Attachment must not be null",
                exception.getMessage());
    }

    @Test
    public void validateAttachment_withNullContentType_throwsNullPointerException() {
        var attachment = new TestAttachment(getValidUtf8Data(), null,
                "file.txt");
        var exception = Assert.assertThrows(NullPointerException.class,
                () -> LLMProviderHelpers.validateAttachment(attachment));
        Assert.assertEquals("Attachment content type must not be null",
                exception.getMessage());
    }

    @Test
    public void validateAttachment_withNullData_throwsNullPointerException() {
        var attachment = new TestAttachment(null, "text/plain", "file.txt");
        var exception = Assert.assertThrows(NullPointerException.class,
                () -> LLMProviderHelpers.validateAttachment(attachment));
        Assert.assertEquals("Attachment data must not be null",
                exception.getMessage());
    }

    @Test
    public void supportedContentType_fromMimeType_withImageTypes_returnsImage() {
        Assert.assertEquals(LLMProviderHelpers.AttachmentContentType.IMAGE,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("image/png"));
        Assert.assertEquals(LLMProviderHelpers.AttachmentContentType.IMAGE,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("image/jpeg"));
        Assert.assertEquals(LLMProviderHelpers.AttachmentContentType.IMAGE,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("image/gif"));
    }

    @Test
    public void supportedContentType_fromMimeType_withTextTypes_returnsText() {
        Assert.assertEquals(LLMProviderHelpers.AttachmentContentType.TEXT,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("text/plain"));
        Assert.assertEquals(LLMProviderHelpers.AttachmentContentType.TEXT,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("text/html"));
    }

    @Test
    public void supportedContentType_fromMimeType_withPdfTypes_returnsPdf() {
        Assert.assertEquals(LLMProviderHelpers.AttachmentContentType.PDF,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("application/pdf"));
        Assert.assertEquals(LLMProviderHelpers.AttachmentContentType.PDF,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("application/x-pdf"));
    }

    @Test
    public void supportedContentType_fromMimeType_withUnsupportedTypes_returnsUnsupported() {
        Assert.assertEquals(
                LLMProviderHelpers.AttachmentContentType.UNSUPPORTED,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("application/octet-stream"));
        Assert.assertEquals(
                LLMProviderHelpers.AttachmentContentType.UNSUPPORTED,
                LLMProviderHelpers.AttachmentContentType
                        .fromMimeType("video/mp4"));
    }

    @Test
    public void supportedContentType_fromMimeType_withNull_returnsUnsupported() {
        Assert.assertEquals(
                LLMProviderHelpers.AttachmentContentType.UNSUPPORTED,
                LLMProviderHelpers.AttachmentContentType.fromMimeType(null));
    }

    private static byte[] getInvalidUtf8Data() {
        return new byte[] { 'H', 'i', (byte) 0xFF, (byte) 0xFE };
    }

    private static byte[] getValidUtf8Data() {
        return "Hello, World! UTF-8: café résumé naïve"
                .getBytes(StandardCharsets.UTF_8);
    }

    private record TestAttachment(byte[] data, String contentType,
            String fileName) implements LLMProvider.Attachment {
    }
}
