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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-grid/select-invisible-grid")
public class SelectAndMakeVisibleIT extends AbstractComponentIT {

    @Test
    public void selectRowAndMakeGridVisible() throws InterruptedException {
        open();

        $("button").id("select").click();
        checkLogsForErrors();

        Long selectedLength = (Long) getCommandExecutor().executeScript(
                "return arguments[0].selectedItems.length;",
                findElement(By.tagName("vaadin-grid")));
        Assert.assertEquals("Unexpected number of selected items", 1l,
                selectedLength.longValue());
    }
}
