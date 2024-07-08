/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import com.vaadin.flow.component.upload.receivers.FileBuffer;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
