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

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.component.textfield.testbench.TextAreaElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration test verifying that {@link com.vaadin.flow.component.ai.form.FormAIController}
 * populates a {@link com.vaadin.flow.component.formlayout.FormLayout} via the
 * canned {@code fill_form} tool call from a background thread.
 */
@TestPath("vaadin-ai/form-controller")
public class FormControllerIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void triggerFill_populatesAllFields() {
        var merchant = $(TextFieldElement.class).id("merchant");
        var amount = $(NumberFieldElement.class).id("amount");
        var currency = $(ComboBoxElement.class).id("currency");
        var date = $(DatePickerElement.class).id("date");
        var notes = $(TextAreaElement.class).id("notes");

        clickElementWithJs("trigger-fill");

        waitUntil(d -> "Trattoria Toscana".equals(merchant.getValue()), 10);
        Assert.assertEquals("58.4", amount.getValue());
        Assert.assertEquals("EUR", currency.getSelectedText());
        Assert.assertEquals(LocalDate.of(2026, 5, 4), date.getDate());
        Assert.assertEquals("dinner with the team", notes.getValue());
    }
}
