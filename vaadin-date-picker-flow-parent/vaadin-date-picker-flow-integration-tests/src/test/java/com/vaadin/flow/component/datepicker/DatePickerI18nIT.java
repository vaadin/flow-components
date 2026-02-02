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
package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link DatePickerI18nPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-date-picker/i18n")
public class DatePickerI18nIT extends AbstractComponentIT {

    private DatePickerElement datePickerElement;

    @Before
    public void init() {
        open();
        datePickerElement = $(DatePickerElement.class).waitForFirst();
    }

    @Test
    public void setI18n_i18nIsApplied() {
        clickElementWithJs("set-i18n");
        datePickerElement.open();

        DatePickerElement.OverlayContentElement overlayContent = datePickerElement
                .getOverlayContent();

        Assert.assertEquals("Custom today",
                overlayContent.getTodayButton().getText());
        Assert.assertEquals("Custom cancel",
                overlayContent.getCancelButton().getText());
        Assert.assertTrue(overlayContent.getVisibleMonthCalendars().stream()
                .anyMatch(month -> month.getHeaderText()
                        .contains("Custom January")));
    }

    @Test
    public void setEmptyI18n_defaultI18nIsPreserved() {
        clickElementWithJs("set-i18n");
        clickElementWithJs("set-empty-i18n");
        datePickerElement.open();

        DatePickerElement.OverlayContentElement overlayContent = datePickerElement
                .getOverlayContent();

        Assert.assertEquals("Today", overlayContent.getTodayButton().getText());
        Assert.assertEquals("Cancel",
                overlayContent.getCancelButton().getText());
        Assert.assertTrue(overlayContent.getVisibleMonthCalendars().stream()
                .anyMatch(month -> month.getHeaderText().contains("January")));
    }
}
