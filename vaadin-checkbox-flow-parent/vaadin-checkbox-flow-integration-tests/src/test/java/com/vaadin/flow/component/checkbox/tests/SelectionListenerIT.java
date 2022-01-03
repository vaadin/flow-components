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
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-checkbox/selection-listener")
public class SelectionListenerIT extends AbstractComponentIT {

    @Test
    public void selectItem_selectionEventIsFired() {
        open();

        $("vaadin-checkbox-group").first().$(CheckboxElement.class).first()
                .click();

        WebElement selection = findElement(By.id("current-selection"));
        Assert.assertEquals("foo", selection.getText());
    }
}
