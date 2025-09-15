/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.upload.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.upload.receivers.FileBuffer;

public class FileBufferTest {

    @Test
    public void shouldBeAbleToReadFileAfterReceiving() throws IOException {
        FileBuffer fileBuffer = new FileBuffer();
        final String data = "Upload data";
        final byte[] dataBytes = data.getBytes(Charset.defaultCharset());
        try (OutputStream os = fileBuffer.receiveUpload("uploadData", "text")) {
            os.write(dataBytes);
        }
        final String readData = IOUtils.toString(fileBuffer.getInputStream(),
                Charset.defaultCharset());
        Assert.assertEquals(data, readData);
    }
}
