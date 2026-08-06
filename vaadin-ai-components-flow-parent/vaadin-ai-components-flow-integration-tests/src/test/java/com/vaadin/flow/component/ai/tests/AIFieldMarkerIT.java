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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Smoke tests proving that the AI field marker a {@code FormAIController}
 * applies to the fields it filled reaches the browser and works there: the
 * {@code vaadin-ai-field-marker} module is loaded, the marker renders the texts
 * the server sent, the "AI is working" state reaches the field, and reverting
 * from the marker's popover restores the field's value on the server.
 * <p>
 * The scenario matrix around marking — which fields get marked, when a mark is
 * cleared, what a revert restores — is covered by {@code FormAIControllerTest}
 * against the marker element's server-side state.
 */
@TestPath("vaadin-ai/ai-field-marker")
public class AIFieldMarkerIT extends AbstractComponentIT {

    private static final String MARKER = "vaadin-ai-field-marker";

    private TestBenchElement name;
    private TestBenchElement company;
    private TestBenchElement unchanged;

    @Before
    public void init() {
        open();
        name = $("vaadin-text-field").id("name");
        company = $("vaadin-text-field").id("company");
        unchanged = $("vaadin-text-field").id("unchanged");
    }

    @Test
    public void runTurn_markerRendersTextsSentByServer() {
        runTurn();

        var marker = waitForMarker(name);
        var badge = marker.$("button").withClassName("badge").first();
        Assert.assertEquals(AIFieldMarkerPage.BADGE_LABEL,
                badge.getDomAttribute("aria-label"));
        Assert.assertEquals(AIFieldMarkerPage.BADGE_TOOLTIP,
                marker.$("vaadin-tooltip").first().getPropertyString("text"));
        Assert.assertEquals(AIFieldMarkerPage.MESSAGE,
                textContentOf(marker.$("p").withClassName("message").first()));
        Assert.assertEquals(AIFieldMarkerPage.REVERT,
                textContentOf(revertButtonOf(marker)));
    }

    @Test
    public void runTurn_unchangedFieldIsNotMarked() {
        runTurn();

        waitForMarker(name);
        Assert.assertTrue(
                "A field the turn left unchanged must not carry a marker",
                unchanged.$(MARKER).all().isEmpty());
    }

    @Test
    public void startTurn_fieldShowsWorkingState() {
        $("button").id("start-turn").click();

        waitUntil(driver -> name.hasAttribute("ai-working"));
        Assert.assertTrue(
                "The field must be read-only on the client while the AI works",
                name.getPropertyBoolean("readonly"));

        $("button").id("finish-turn").click();

        waitUntil(driver -> !name.hasAttribute("ai-working"));
        // The marker keeps the field locked for the shimmer wind-down, so the
        // client-side read-only state is restored slightly after the state
        // ends.
        waitUntil(driver -> !name.getPropertyBoolean("readonly"));
    }

    @Test
    public void revertFromPopover_restoresValueOnServer() {
        runTurn();

        var marker = waitForMarker(company);
        marker.$("button").withClassName("badge").first().click();
        var revert = revertButtonOf(marker);
        waitUntil(driver -> revert.isDisplayed());
        revert.click();

        waitUntil(driver -> "Acme Inc."
                .equals(company.getPropertyString("value")));
        waitUntil(driver -> company.$(MARKER).all().isEmpty());
    }

    /**
     * Runs one AI turn end to end and waits for the values it writes to land.
     * The marker holds a value back for the duration of its shimmer, so a
     * written value shows up shortly after the turn ends.
     */
    private void runTurn() {
        $("button").id("start-turn").click();
        $("button").id("finish-turn").click();
        waitUntil(driver -> AIFieldMarkerPage.NAME_VALUE
                .equals(name.getPropertyString("value")));
        waitUntil(driver -> AIFieldMarkerPage.COMPANY_VALUE
                .equals(company.getPropertyString("value")));
    }

    private TestBenchElement waitForMarker(TestBenchElement field) {
        waitUntil(driver -> !field.$(MARKER).all().isEmpty());
        return field.$(MARKER).first();
    }

    private TestBenchElement revertButtonOf(TestBenchElement marker) {
        return marker.$("div").withClassName("actions").first().$("button")
                .first();
    }

    /**
     * @return the element's text as the DOM holds it, regardless of whether it
     *         is currently rendered — the popover contents are only displayed
     *         while the popover is open
     */
    private String textContentOf(TestBenchElement element) {
        return (String) executeScript("return arguments[0].textContent",
                element);
    }
}
