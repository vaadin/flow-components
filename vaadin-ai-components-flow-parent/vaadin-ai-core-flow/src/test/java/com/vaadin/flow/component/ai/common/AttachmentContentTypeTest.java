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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AttachmentContentTypeTest {

    @Test
    void supportedContentType_fromMimeType_withImageTypes_returnsImage() {
        Assertions.assertEquals(AttachmentContentType.IMAGE,
                AttachmentContentType.fromMimeType("image/png"));
        Assertions.assertEquals(AttachmentContentType.IMAGE,
                AttachmentContentType.fromMimeType("image/jpeg"));
        Assertions.assertEquals(AttachmentContentType.IMAGE,
                AttachmentContentType.fromMimeType("image/gif"));
    }

    @Test
    void supportedContentType_fromMimeType_withTextTypes_returnsText() {
        Assertions.assertEquals(AttachmentContentType.TEXT,
                AttachmentContentType.fromMimeType("text/plain"));
        Assertions.assertEquals(AttachmentContentType.TEXT,
                AttachmentContentType.fromMimeType("text/html"));
    }

    @Test
    void supportedContentType_fromMimeType_withPdfTypes_returnsPdf() {
        Assertions.assertEquals(AttachmentContentType.PDF,
                AttachmentContentType.fromMimeType("application/pdf"));
        Assertions.assertEquals(AttachmentContentType.PDF,
                AttachmentContentType.fromMimeType("application/x-pdf"));
    }

    @Test
    void supportedContentType_fromMimeType_withAudioTypes_returnsAudio() {
        Assertions.assertEquals(AttachmentContentType.AUDIO,
                AttachmentContentType.fromMimeType("audio/mpeg"));
        Assertions.assertEquals(AttachmentContentType.AUDIO,
                AttachmentContentType.fromMimeType("audio/wav"));
        Assertions.assertEquals(AttachmentContentType.AUDIO,
                AttachmentContentType.fromMimeType("audio/ogg"));
    }

    @Test
    void supportedContentType_fromMimeType_withVideoTypes_returnsVideo() {
        Assertions.assertEquals(AttachmentContentType.VIDEO,
                AttachmentContentType.fromMimeType("video/mp4"));
        Assertions.assertEquals(AttachmentContentType.VIDEO,
                AttachmentContentType.fromMimeType("video/webm"));
        Assertions.assertEquals(AttachmentContentType.VIDEO,
                AttachmentContentType.fromMimeType("video/ogg"));
    }

    @Test
    void supportedContentType_fromMimeType_withUnsupportedTypes_returnsUnsupported() {
        Assertions.assertEquals(AttachmentContentType.UNSUPPORTED,
                AttachmentContentType.fromMimeType("application/octet-stream"));
        Assertions.assertEquals(AttachmentContentType.UNSUPPORTED,
                AttachmentContentType.fromMimeType("application/json"));
    }

    @Test
    void supportedContentType_fromMimeType_withNull_returnsUnsupported() {
        Assertions.assertEquals(AttachmentContentType.UNSUPPORTED,
                AttachmentContentType.fromMimeType(null));
    }
}
