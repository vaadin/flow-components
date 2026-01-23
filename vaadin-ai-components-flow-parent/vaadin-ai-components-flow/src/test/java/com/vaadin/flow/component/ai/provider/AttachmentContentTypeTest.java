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

import org.junit.Assert;
import org.junit.Test;

public class AttachmentContentTypeTest {

    @Test
    public void supportedContentType_fromMimeType_withImageTypes_returnsImage() {
        Assert.assertEquals(AttachmentContentType.IMAGE,
                AttachmentContentType.fromMimeType("image/png"));
        Assert.assertEquals(AttachmentContentType.IMAGE,
                AttachmentContentType.fromMimeType("image/jpeg"));
        Assert.assertEquals(AttachmentContentType.IMAGE,
                AttachmentContentType.fromMimeType("image/gif"));
    }

    @Test
    public void supportedContentType_fromMimeType_withTextTypes_returnsText() {
        Assert.assertEquals(AttachmentContentType.TEXT,
                AttachmentContentType.fromMimeType("text/plain"));
        Assert.assertEquals(AttachmentContentType.TEXT,
                AttachmentContentType.fromMimeType("text/html"));
    }

    @Test
    public void supportedContentType_fromMimeType_withPdfTypes_returnsPdf() {
        Assert.assertEquals(AttachmentContentType.PDF,
                AttachmentContentType.fromMimeType("application/pdf"));
        Assert.assertEquals(AttachmentContentType.PDF,
                AttachmentContentType.fromMimeType("application/x-pdf"));
    }

    @Test
    public void supportedContentType_fromMimeType_withAudioTypes_returnsAudio() {
        Assert.assertEquals(AttachmentContentType.AUDIO,
                AttachmentContentType.fromMimeType("audio/mpeg"));
        Assert.assertEquals(AttachmentContentType.AUDIO,
                AttachmentContentType.fromMimeType("audio/wav"));
        Assert.assertEquals(AttachmentContentType.AUDIO,
                AttachmentContentType.fromMimeType("audio/ogg"));
    }

    @Test
    public void supportedContentType_fromMimeType_withVideoTypes_returnsVideo() {
        Assert.assertEquals(AttachmentContentType.VIDEO,
                AttachmentContentType.fromMimeType("video/mp4"));
        Assert.assertEquals(AttachmentContentType.VIDEO,
                AttachmentContentType.fromMimeType("video/webm"));
        Assert.assertEquals(AttachmentContentType.VIDEO,
                AttachmentContentType.fromMimeType("video/ogg"));
    }

    @Test
    public void supportedContentType_fromMimeType_withUnsupportedTypes_returnsUnsupported() {
        Assert.assertEquals(AttachmentContentType.UNSUPPORTED,
                AttachmentContentType.fromMimeType("application/octet-stream"));
        Assert.assertEquals(AttachmentContentType.UNSUPPORTED,
                AttachmentContentType.fromMimeType("application/json"));
    }

    @Test
    public void supportedContentType_fromMimeType_withNull_returnsUnsupported() {
        Assert.assertEquals(AttachmentContentType.UNSUPPORTED,
                AttachmentContentType.fromMimeType(null));
    }
}
