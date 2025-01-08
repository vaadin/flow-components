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

import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;

public class MultiFileBufferTest {

    @Test
    public void shouldBeAbleToReadFilesAfterReceiving() throws IOException {
        MultiFileBuffer fileBuffer = new MultiFileBuffer();
        TestData[] testData = { new TestData("upload1", "Upload data 1"),
                new TestData("upload2", "Upload data 2"),
                new TestData("upload3", "Upload data 3"), };
        for (TestData data : testData) {
            final byte[] dataBytes = data.data
                    .getBytes(Charset.defaultCharset());
            try (OutputStream os = fileBuffer.receiveUpload(data.filename,
                    "text")) {
                os.write(dataBytes);
            }
        }
        for (TestData data : testData) {
            final String readData = IOUtils.toString(
                    fileBuffer.getInputStream(data.filename),
                    Charset.defaultCharset());
            Assert.assertEquals(data.data, readData);
        }
    }

    class TestData {
        private final String filename;
        private final String data;

        public TestData(String filename, String data) {
            this.filename = filename;
            this.data = data;
        }
    }
}
