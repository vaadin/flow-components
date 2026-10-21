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

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-ai/ai-field-lock")
public class AIFieldLockIT extends AbstractComponentIT {

    private TextFieldElement tripType;
    private TextFieldElement costCenter;

    @Before
    public void init() {
        open();
        tripType = $(TextFieldElement.class).id("trip-type");
        costCenter = $(TextFieldElement.class).id("cost-center");
    }

    @Test
    public void fieldMadeWritableMidTurn_isLockedDuringTurn() {
        $("button").id("start-turn").click();
        waitUntil(driver -> tripType.hasAttribute("ai-working"));
        $("button").id("fill").click();
        waitUntil(driver -> costCenter.hasAttribute("ai-working"));

        Assert.assertTrue(
                "Cost center is in the working state, so it must be locked "
                        + "on the client",
                costCenter.getPropertyBoolean("readonly"));
    }

    @Test
    public void fieldMadeWritableMidTurn_isEditableAfterTurn() {
        $("button").id("start-turn").click();
        waitUntil(driver -> tripType.hasAttribute("ai-working"));
        $("button").id("fill").click();
        waitUntil(driver -> costCenter.hasAttribute("ai-working"));
        $("button").id("finish-turn").click();

        // The working guard is lifted after a short wind-down; once the trip
        // type field is editable again, the guard has been lifted
        waitUntil(driver -> "Business".equals(tripType.getValue()));
        waitUntil(driver -> !tripType.getPropertyBoolean("readonly"));
        $("button").id("check").click();
        waitUntil(driver -> "false"
                .equals($("span").id("server-read-only").getText()));

        Assert.assertFalse(
                "Cost center is writable on the server, so it must be "
                        + "editable on the client after the turn",
                costCenter.getPropertyBoolean("readonly"));
    }
}
