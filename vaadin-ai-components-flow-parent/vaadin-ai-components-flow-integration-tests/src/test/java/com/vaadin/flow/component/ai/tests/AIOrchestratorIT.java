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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.testbench.MessageElement;
import com.vaadin.flow.component.messages.testbench.MessageInputElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.component.upload.testbench.UploadDropZoneElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for AIOrchestrator.
 */
@TestPath("vaadin-ai/orchestrator")
public class AIOrchestratorIT extends AbstractComponentIT {

    private MessageListElement messageList;
    private MessageInputElement messageInput;
    private UploadDropZoneElement uploadDropZone;

    @Before
    public void init() {
        open();
        messageList = $(MessageListElement.class).single();
        messageInput = $(MessageInputElement.class).single();
        uploadDropZone = $(UploadDropZoneElement.class).single();
    }

    @Test
    public void promptButton_sendsMessage_responseIsDisplayed() {
        clickElementWithJs("prompt-button");
        waitUntil(driver -> getMessageCount() >= 2, 5);
        Assert.assertTrue("Should have at least 2 messages (user + assistant)",
                getMessageCount() >= 2);
    }

    @Test
    public void messageInput_submitMessage_responseIsDisplayed() {
        messageInput.submit("Hello");
        waitUntil(driver -> getMessageCount() >= 2, 5);
        Assert.assertEquals(2, getMessageCount());
    }

    @Test
    public void uploadFile_submitMessage_attachmentRenderedInMessage()
            throws Exception {
        uploadFile("test-file.txt");
        messageInput.submit("Check this file");
        waitUntil(driver -> getMessageCount() >= 2, 5);

        var userMessage = getFirstUserMessage();
        Assert.assertTrue("User message should have attachments",
                userMessage.hasAttachments());
        Assert.assertEquals(1, userMessage.getAttachmentElements().size());
        Assert.assertNotNull(userMessage.getAttachmentByName("test-file.txt"));
    }

    @Test
    public void uploadFile_submitMessage_clickAttachment_infoDisplayed()
            throws Exception {
        uploadFile("report.txt");
        messageInput.submit("Check this");
        waitUntil(driver -> getMessageCount() >= 2, 5);

        var userMessage = getFirstUserMessage();
        var attachment = userMessage.getAttachmentByName("report.txt");
        Assert.assertNotNull("Attachment should exist", attachment);

        attachment.click();

        var clickedInfo = $("span").id("clicked-attachment-info");
        waitUntil(driver -> !clickedInfo.getText().isEmpty(), 5);
        Assert.assertTrue("Clicked attachment info should contain filename",
                clickedInfo.getText().contains("report.txt"));
    }

    private int getMessageCount() {
        return messageList.getMessageElements().size();
    }

    private MessageElement getFirstUserMessage() {
        return messageList.getMessageElements().getFirst();
    }

    private void uploadFile(String fileName) throws IOException {
        File tempFile = createTempFile(fileName);
        uploadDropZone.getUploadManager().upload(tempFile);
    }

    private File createTempFile(String fileName) throws IOException {
        File tempDir = Files.createTempDirectory("upload-test").toFile();
        tempDir.deleteOnExit();
        File tempFile = new File(tempDir, fileName);
        try (var writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("Test file content");
        }
        tempFile.deleteOnExit();
        return tempFile;
    }
}
