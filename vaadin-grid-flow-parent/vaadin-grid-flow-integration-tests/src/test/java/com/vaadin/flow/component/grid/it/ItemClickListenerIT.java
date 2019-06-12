/*
 * Copyright 2000-2018 Vaadin Ltd.
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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.AbstractNoW3c;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.grid.testbench.GridTRElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("item-click-listener")
public class ItemClickListenerIT extends AbstractNoW3c {

    @Test
    public void doubleClickGoesWithSingleClicks() throws InterruptedException {
        open();

        GridTRElement firstRow = $(GridElement.class).first().getRow(0);
        firstRow.doubleClick();

        WebElement singleClickCount = findElement(By.id("clickMsg"));

        Assert.assertEquals("Click event", singleClickCount.getText());

        String yCoord = findElement(By.id("dblClickMsg")).getText();

        Assert.assertThat(Integer.parseInt(yCoord),
                CoreMatchers.allOf(
                        Matchers.greaterThan(firstRow.getLocation().getY()),
                        Matchers.lessThan(firstRow.getLocation().getY()
                                + firstRow.getSize().getHeight())));
    }
}
