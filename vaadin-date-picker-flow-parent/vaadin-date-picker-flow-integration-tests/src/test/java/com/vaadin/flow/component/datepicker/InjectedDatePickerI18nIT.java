/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-date-picker/injected-datepicker")
public class InjectedDatePickerI18nIT extends AbstractComponentIT {

    @Test
    public void checkInitialI18n() {
        open();

        $("injected-datepicker-i18n").first().$("vaadin-date-picker").first()
                .$("input").first().click();

        TestBenchElement cancelButton = $("injected-datepicker-i18n").first()
                .$("vaadin-date-picker-overlay-content").first()
                .$("vaadin-button").withAttribute("slot", "cancel-button")
                .first();

        Assert.assertEquals("peruuta", cancelButton.getText());
    }
}
