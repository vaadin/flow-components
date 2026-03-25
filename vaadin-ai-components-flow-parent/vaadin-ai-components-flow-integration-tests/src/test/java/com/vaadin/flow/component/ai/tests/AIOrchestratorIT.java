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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.charts.testbench.ChartElement;
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
        waitUntil(d -> !$(MessageListElement.class).all().isEmpty(), 10);
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
    public void chartController_renderBarChart_categoriesDisplayed() {
        int initialCount = getMessageCount();
        clickElementWithJs("render-bar-chart");
        waitUntil(driver -> getMessageCount() >= initialCount + 2, 10);

        var chart = $(ChartElement.class).single();
        waitUntil(driver -> !chart.getVisiblePoints().isEmpty(), 5);

        var categories = getChartCategories(chart);
        Assert.assertEquals(List.of("January", "February", "March"),
                categories);
        Assert.assertEquals("bar", getChartType(chart));
    }

    @Test
    public void chartController_changeBarToScatter_staleCategoriesCleared() {
        // Render bar chart first — establishes categories
        int initialCount = getMessageCount();
        clickElementWithJs("render-bar-chart");
        waitUntil(driver -> getMessageCount() >= initialCount + 2, 10);

        var chart = $(ChartElement.class).single();
        waitUntil(driver -> !chart.getVisiblePoints().isEmpty(), 5);
        Assert.assertNotNull("Bar chart should have categories",
                getChartCategories(chart));

        // Switch to scatter — uses numeric x/y, no categories.
        // With drawChart(true), old categories are cleared.
        // With drawChart(false), old categories would persist as
        // stale state because the merge keeps properties not
        // present in the new config.
        int countBeforeScatter = getMessageCount();
        clickElementWithJs("render-scatter-chart");
        waitUntil(driver -> getMessageCount() >= countBeforeScatter + 2, 10);
        waitUntil(driver -> "scatter".equals(getChartType(chart)), 5);

        var categories = getChartCategories(chart);
        Assert.assertTrue(
                "Scatter chart should not have stale categories from bar chart, but had: "
                        + categories,
                categories == null || categories.isEmpty());
    }

    @Test
    public void chartController_changeBarToLine_categoriesPreserved() {
        int initialCount = getMessageCount();
        clickElementWithJs("render-bar-chart");
        waitUntil(driver -> getMessageCount() >= initialCount + 2, 10);

        var chart = $(ChartElement.class).single();
        waitUntil(driver -> !chart.getVisiblePoints().isEmpty(), 5);
        Assert.assertEquals("bar", getChartType(chart));

        // Change to line — keeps existing data, only updates config
        int countBeforeLine = getMessageCount();
        clickElementWithJs("render-line-chart");
        waitUntil(driver -> getMessageCount() >= countBeforeLine + 2, 10);
        waitUntil(driver -> "line".equals(getChartType(chart)), 5);

        var categories = getChartCategories(chart);
        Assert.assertEquals(List.of("January", "February", "March"),
                categories);
    }

    @SuppressWarnings("unchecked")
    private List<String> getChartCategories(ChartElement chart) {
        return (List<String>) executeScript(
                "return arguments[0].configuration.xAxis[0].categories", chart);
    }

    private String getChartType(ChartElement chart) {
        return (String) executeScript(
                "return arguments[0].configuration.options.chart.type", chart);
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
