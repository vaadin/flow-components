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
package com.vaadin.flow.component.combobox.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Verifies that with {@code @Push} enabled, a server-side {@code setValue}
 * surrounded by two push frames does not produce a spurious client-initiated
 * value change with an empty value.
 */
@TestPath("vaadin-multi-select-combo-box/push-value-change")
public class MultiSelectComboBoxPushValueChangeIT extends AbstractComponentIT {
    private TestBenchElement runScenario;
    private TestBenchElement log;

    @Before
    public void init() {
        open();
        runScenario = $("button").waitForFirst();
        log = $("div").id("value-change-log");
    }

    @Test
    public void setValueWithPushFrames_noSpuriousEmptyClientValueChange() {
        runScenario.click();

        // Wait for the value change triggered by the server-side setValue
        waitUntil(driver -> !log.getText().isEmpty());

        List<String> entries = getLogEntries();

        // No client-initiated value change with an empty value must occur
        for (String entry : entries) {
            boolean isFromClient = entry.contains("isFromClient=true");
            boolean isEmptyValue = entry.contains("value=[]");
            Assert.assertFalse(
                    "Spurious client-initiated value change with empty value: "
                            + entry,
                    isFromClient && isEmptyValue);
        }

        // The final server value must remain ["1"]
        String last = entries.get(entries.size() - 1);
        Assert.assertTrue(
                "Expected final value to remain [1], but log was: " + entries,
                last.contains("value=[1]"));
    }

    private List<String> getLogEntries() {
        return log.$("div").all().stream().map(TestBenchElement::getText)
                .toList();
    }
}
