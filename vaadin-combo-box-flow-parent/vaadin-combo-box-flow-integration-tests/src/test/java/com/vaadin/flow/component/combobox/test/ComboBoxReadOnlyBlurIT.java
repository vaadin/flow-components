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
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

@TestPath("vaadin-combo-box/readonly-blur")
public class ComboBoxReadOnlyBlurIT extends AbstractComboBoxIT {

    @Test
    public void comboBoxReadOnlyBlur() {
        open();
        ComboBoxElement comboBoxElement = $(ComboBoxElement.class)
                .waitForFirst();

        // simulate blur on combo box
        comboBoxElement.dispatchEvent("focusout");
        getCommandExecutor().waitForVaadin();

        // Blur should not trigger custom value set event.
        Assert.assertThrows(NoSuchElementException.class,
                () -> findElement(By.id("custom-value-set")));
    }
}
