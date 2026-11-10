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
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.messages.testbench.MessageInputElement;
import com.vaadin.flow.component.upload.testbench.UploadButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for disabling submitting on the input of the AIOrchestrator
 * while a file is being uploaded.
 */
@TestPath("vaadin-ai/orchestrator-submit-disabled")
public class AIOrchestratorSubmitDisabledIT extends AbstractComponentIT {

    private MessageInputElement messageInput;
    private UploadButtonElement uploadButton;

    @Before
    public void init() {
        open();
        messageInput = $(MessageInputElement.class).single();
        uploadButton = $(UploadButtonElement.class).single();
    }

    @Test
    public void receivingFile_submitDisabledUntilFileReceived()
            throws IOException {
        var request = openUploadRequest();
        try (var body = request.getOutputStream()) {
            body.write("content".getBytes(StandardCharsets.UTF_8));
            body.flush();
            waitUntil(driver -> isSubmitDisabled(), 5);
        }

        Assert.assertEquals(200, request.getResponseCode());
        waitUntil(driver -> !isSubmitDisabled(), 5);
    }

    private boolean isSubmitDisabled() {
        return Boolean.TRUE
                .equals(messageInput.getPropertyBoolean("submitDisabled"));
    }

    /**
     * Opens an upload request to the upload manager, as the browser does, so
     * that the server receives the file until the request body is closed.
     */
    private HttpURLConnection openUploadRequest() throws IOException {
        var target = (String) executeScript(
                "return new URL(arguments[0].manager.target, document.baseURI).href;",
                uploadButton);
        var session = getDriver().manage().getCookieNamed("JSESSIONID")
                .getValue();
        var request = (HttpURLConnection) URI.create(target).toURL()
                .openConnection(Proxy.NO_PROXY);
        request.setRequestMethod("POST");
        request.setDoOutput(true);
        request.setChunkedStreamingMode(0);
        request.setRequestProperty("Cookie", "JSESSIONID=" + session);
        request.setRequestProperty("Content-Type", "text/plain");
        request.setRequestProperty("X-Filename", "file.txt");
        return request;
    }
}
