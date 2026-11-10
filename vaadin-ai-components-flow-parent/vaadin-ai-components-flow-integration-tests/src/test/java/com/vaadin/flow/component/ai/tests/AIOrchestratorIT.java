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
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
    public void submitMessage_refreshPage_historyRestored() {
        messageInput.submit("Hello");
        waitUntil(driver -> getMessageCount() >= 2, 5);
        Assert.assertEquals(2, getMessageCount());

        // Refresh the page - history should be auto-restored from session
        open();
        messageList = $(MessageListElement.class).single();

        waitUntil(driver -> getMessageCount() >= 2, 5);
        var messages = messageList.getMessageElements();
        Assert.assertEquals(2, messages.size());
        Assert.assertTrue(messages.get(0).getText().contains("Hello"));
        Assert.assertTrue(messages.get(1).getText().contains("Echo: Hello"));
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

    @Test
    public void uploadStarted_submitDisabledUntilUploadsFinished()
            throws Exception {
        uploadDropZone.getCommandExecutor().executeScript(
                "arguments[0].manager.dispatchEvent(new CustomEvent('upload-start'))",
                uploadDropZone);
        waitUntil(driver -> isSubmitDisabled(), 5);

        uploadFile("test-file.txt");
        waitUntil(driver -> !isSubmitDisabled(), 5);
    }

    @Test
    public void uploadCanceled_submitEnabled() {
        startUploadWithoutSending();
        waitUntil(driver -> isSubmitDisabled(), 5);

        cancelUpload();
        waitUntil(driver -> !isSubmitDisabled(), 5);

        messageInput.submit("Hello");
        waitUntil(driver -> getMessageCount() >= 2, 5);
    }

    @Test
    public void uploadCanceledWhileServerReceiving_submitEnabled()
            throws IOException {
        startUploadWithoutSending();
        waitUntil(driver -> isSubmitDisabled(), 5);

        // Sends an upload request that the server is still receiving when the
        // browser cancels its upload, and that finishes outside of a
        // client-server round-trip
        var request = openUploadRequest();
        try (var body = request.getOutputStream()) {
            body.write("content".getBytes(StandardCharsets.UTF_8));
            body.flush();
            waitUntil(driver -> isServerUploading(), 5);

            cancelUpload();
            waitUntil(driver -> !isSubmitDisabled(), 5);
        }
        Assert.assertEquals(200, request.getResponseCode());
        Assert.assertFalse(isSubmitDisabled());

        messageInput.submit("Hello");
        waitUntil(driver -> getMessageCount() >= 2, 5);
    }

    @Test
    public void uploadManagerDisabledWhileUploading_submitEnabledAfterUpload() {
        startUploadWithoutSending();
        waitUntil(driver -> isSubmitDisabled(), 5);

        clickElementWithJs("toggle-upload-enabled-button");
        executeScript("window.sendUpload();");
        waitUntil(driver -> (Boolean) executeScript(
                "return !arguments[0].manager.files.some(file => file.uploading);",
                uploadDropZone), 5);
        clickElementWithJs("toggle-upload-enabled-button");

        waitUntil(driver -> !isSubmitDisabled(), 5);
        messageInput.submit("Hello");
        waitUntil(driver -> getMessageCount() >= 2, 5);
    }

    private boolean isSubmitDisabled() {
        return Boolean.TRUE
                .equals(messageInput.getPropertyBoolean("submitDisabled"));
    }

    /**
     * Starts an upload in the browser without sending it, which keeps the
     * upload in progress until it is canceled, or sent with
     * {@code window.sendUpload()}.
     */
    private void startUploadWithoutSending() {
        executeScript(
                """
                        const send = XMLHttpRequest.prototype.send;
                        XMLHttpRequest.prototype.send = function (body) {
                          if (body instanceof File || body instanceof FormData) {
                            this.upload.onloadstart();
                            window.sendUpload = () => send.call(this, body);
                            return;
                          }
                          return send.call(this, body);
                        };
                        arguments[0].manager.addFiles([new File(['content'], 'test-file.txt')]);
                        """,
                uploadDropZone);
    }

    private void cancelUpload() {
        executeScript(
                "const manager = arguments[0].manager; manager.abortUpload(manager.files[0]);",
                uploadDropZone);
    }

    private boolean isServerUploading() {
        clickElementWithJs("check-uploading-button");
        return "true".equals($("span").id("uploading-info").getText());
    }

    private HttpURLConnection openUploadRequest() throws IOException {
        var target = (String) executeScript(
                "return new URL(arguments[0].manager.target, document.baseURI).href;",
                uploadDropZone);
        var session = getDriver().manage().getCookieNamed("JSESSIONID")
                .getValue();
        var request = (HttpURLConnection) URI.create(target).toURL()
                .openConnection(Proxy.NO_PROXY);
        request.setRequestMethod("POST");
        request.setDoOutput(true);
        request.setChunkedStreamingMode(0);
        request.setRequestProperty("Cookie", "JSESSIONID=" + session);
        request.setRequestProperty("Content-Type", "text/plain");
        request.setRequestProperty("X-Filename", "server-file.txt");
        return request;
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
