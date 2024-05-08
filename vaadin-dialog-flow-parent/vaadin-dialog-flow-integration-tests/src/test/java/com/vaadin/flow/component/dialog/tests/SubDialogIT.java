/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-dialog/sub-dialog")
public class SubDialogIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openSubDialog_dialogContentNotChanged() {
        // Open dialog
        clickElementWithJs("open-dialog");

        // Scroll the scroller inside the dialog
        var scroller = ((TestBenchElement) findElement(By.id("scroller")));
        scroller.scroll(100);
        Assert.assertEquals("100", scroller.getPropertyString("scrollTop"));

        // Open the sub dialog
        clickElementWithJs("open-sub-dialog");

        // Expect the scroller scroll position to not have changed
        Assert.assertEquals("100", scroller.getPropertyString("scrollTop"));
    }

}
