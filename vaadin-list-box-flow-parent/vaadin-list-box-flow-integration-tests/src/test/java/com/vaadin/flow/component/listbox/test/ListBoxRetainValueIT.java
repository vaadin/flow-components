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

package com.vaadin.flow.component.listbox.test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-list-box/list-box-retain-value")
public class ListBoxRetainValueIT extends AbstractComponentIT {

    @Test
    public void listBoxRetainValueWhenRemovedAndAdded() {
        open();
        WebElement value = findElement(By.id("list-box-value"));
        Assert.assertEquals(value.getText(), "2");
        findElement(By.id("add-button")).click();
        Assert.assertEquals(value.getText(), "2");
    }
}
