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
package com.vaadin.flow.component.ai.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.testbench.MessageInputElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-ai/serialization")
public class SerializationIT extends AbstractComponentIT {

    private static final Path SERIALIZED_FILE = Paths.get("TESTFOLDER",
            "testFile.ser");

    @Before
    public void init() throws IOException {
        Files.createDirectories(SERIALIZED_FILE.getParent());
        Files.deleteIfExists(SERIALIZED_FILE);
        open();
    }

    @After
    public void cleanup() throws IOException {
        Files.deleteIfExists(SERIALIZED_FILE);
    }

    @Test
    public void submitListenerSurvivesSerialization() {
        // Send first message
        submitMessage("Hello");
        waitForMessageCount(2);
        waitForResponseCount(1);

        // Serialize and assert size
        clickSerialize();
        var size = readByteSize();
        Assert.assertTrue(size > 0);
        var previousSize = size;

        // Send second message, serialize again. Size must grow
        submitMessage("Second message");
        waitForMessageCount(4);
        waitForResponseCount(2);
        clickSerialize();
        size = readByteSize();
        Assert.assertTrue(size > previousSize);
        previousSize = size;

        // Reload page. Old messages should still be visible.
        clickDeserialize();
        waitForMessageCount(4);
        Assert.assertEquals(2, getResponseCount());

        // Send a message after deserialization. The responseCompleteListener
        // must have fired again
        submitMessage("After deserialization");
        waitForMessageCount(6);
        waitForResponseCount(3);

        // Serialize and assert size
        clickSerialize();
        Assert.assertTrue(readByteSize() > previousSize);
    }

    private void submitMessage(String text) {
        $(MessageInputElement.class).single().submit(text);
    }

    private int getMessageCount() {
        return $(MessageListElement.class).single().getMessageElements().size();
    }

    private void waitForMessageCount(int expected) {
        waitUntil(d -> getMessageCount() == expected, 2);
    }

    private void clickSerialize() {
        var previousText = getSizeText();
        clickElementWithJs("serialize-button");
        waitUntil(d -> {
            var text = getSizeText();
            return !text.isEmpty() && !text.equals(previousText);
        }, 2);
    }

    private String getSizeText() {
        return $("span").id("size-span").getText();
    }

    private void clickDeserialize() {
        clickElementWithJs("deserialize-button");
    }

    private int readByteSize() {
        var sizeText = getSizeText();
        Assert.assertFalse(sizeText.isEmpty());
        return Integer.parseInt(sizeText);
    }

    private int getResponseCount() {
        return Integer.parseInt($("span").id("response-count-span").getText());
    }

    private void waitForResponseCount(int expected) {
        waitUntil(d -> getResponseCount() == expected, 2);
    }
}
