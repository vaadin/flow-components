/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
