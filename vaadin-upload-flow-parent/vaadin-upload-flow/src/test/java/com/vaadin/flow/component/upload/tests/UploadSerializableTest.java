/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.upload.tests;

import java.util.stream.Stream;

import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileBuffer;
import com.vaadin.flow.testutil.ClassesSerializableTest;

public class UploadSerializableTest extends ClassesSerializableTest {
    private static final UI FAKE_UI = new UI();

    @Override
    protected Stream<String> getExcludedPatterns() {

        return Stream.concat(super.getExcludedPatterns(), Stream.of(
                "com\\.vaadin\\.flow\\.component\\.upload\\.Upload",
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory",
                "com\\.vaadin\\.flow\\.component\\.upload\\.receivers\\.TempDirectory\\$LazyHolder"));
    }

    @Override
    protected void resetThreadLocals() {
        super.resetThreadLocals();
        UI.setCurrent(null);
    }

    @Override
    protected void setupThreadLocals() {
        super.setupThreadLocals();
        UI.setCurrent(FAKE_UI);
    }

    @Test
    public void serializeFileBuffer() throws Throwable {
        FileBuffer fileBuffer = new FileBuffer();
        fileBuffer.receiveUpload("foo.txt", "text/plain");

        serializeAndDeserialize(fileBuffer);
    }

    @Test
    public void serializeMultiFileBuffer() throws Throwable {
        MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
        multiFileBuffer.receiveUpload("foo.txt", "text/plain");

        serializeAndDeserialize(multiFileBuffer);
    }
}
