/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-picker/date-picker-in-a-grid-header")
public class DatePickerInAGridHeaderIT extends AbstractComponentIT {

    /**
     * Test for https://github.com/vaadin/vaadin-date-picker-flow/issues/100
     */
    @Test
    public void openPage_datePickerIsRendereredWithoutErrors() {
        open();
        waitForElementPresent(By.id("date-picker"));
        checkLogsForErrors();
    }

}
