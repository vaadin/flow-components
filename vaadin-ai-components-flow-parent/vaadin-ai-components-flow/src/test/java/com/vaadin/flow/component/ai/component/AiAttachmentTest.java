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
package com.vaadin.flow.component.ai.component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;

public class AiAttachmentTest {

    @Test
    public void nullParameter_throws() {
        Assert.assertThrows(NullPointerException.class,
                () -> new AiAttachment(null, getMimeType(), getData()));
        Assert.assertThrows(NullPointerException.class,
                () -> new AiAttachment(getFileName(), null, getData()));
        Assert.assertThrows(NullPointerException.class,
                () -> new AiAttachment(getFileName(), getMimeType(), null));
    }

    @Test
    public void toDataUrl() {
        var attachment = new AiAttachment(getFileName(), getMimeType(),
                getData());
        var dataUrl = attachment.toDataUrl();
        Assert.assertTrue(dataUrl.startsWith("data:" + getMimeType()));
        Assert.assertTrue(dataUrl.endsWith(
                ";base64," + Base64.getEncoder().encodeToString(getData())));
    }

    private static byte[] getData() {
        return "test".getBytes(StandardCharsets.UTF_8);
    }

    private static String getMimeType() {
        return "text/plain";
    }

    private static String getFileName() {
        return "test.txt";
    }
}
