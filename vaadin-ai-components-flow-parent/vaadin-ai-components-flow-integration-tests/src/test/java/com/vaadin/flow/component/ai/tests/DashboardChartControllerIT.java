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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.charts.testbench.ChartElement;
import com.vaadin.flow.component.messages.testbench.MessageListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for ChartAIController rendering within a Dashboard.
 */
@TestPath("vaadin-ai/dashboard-chart-controller")
public class DashboardChartControllerIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    /**
     * Verifies that axis categories are applied when a new Chart widget is
     * added to a Dashboard and populated via an async LLM provider. This
     * reproduces the real AI dashboard flow where the orchestrator's
     * onRequestCompleted() runs via ui.access() from a background thread,
     * causing the widget addition and chart rendering to span separate Push
     * updates.
     * <p>
     * Fails with {@code chart.drawChart()} (merge mode) — axis shows numeric
     * indices instead of category names. Passes with
     * {@code chart.drawChart(true)} (reset mode).
     */
    @Test
    public void renderBarChart_categoriesDisplayed() {
        clickElementWithJs("render-bar-chart");

        // Wait for the async LLM response to complete
        var messageList = $(MessageListElement.class).single();
        waitUntil(d -> messageList.getMessageElements().size() >= 2, 10);

        var chart = $(ChartElement.class).single();
        waitUntil(d -> !chart.getVisiblePoints().isEmpty(), 10);

        @SuppressWarnings("unchecked")
        var categories = (List<String>) executeScript(
                "return arguments[0].configuration.xAxis[0].categories", chart);
        Assert.assertEquals(List.of("January", "February", "March"),
                categories);
    }
}
