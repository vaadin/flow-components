/*
 * Copyright 2000-2024 Vaadin Ltd.
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

    @Test
    public void serializeMultiFileBuffer_restoreFileMap() {
        MultiFileBuffer multiFileBuffer = new MultiFileBuffer();
        try {
            multiFileBuffer = serializeAndDeserialize(multiFileBuffer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // Verifies that internal file map is restored, would throw otherwise
        multiFileBuffer.receiveUpload("bar.txt", "text/plain");
    }
}
