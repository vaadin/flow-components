/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.avatar.tests;

import com.vaadin.flow.component.avatar.demo.AvatarView;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for the {@link AvatarView}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("avatar-test")
public class AvatarIT extends AbstractComponentIT {

    private WebElement getPropsBtn;

    @Before
    public void init() {
        open();
        getPropsBtn = findElement(By.id("get-props"));
    }

    @Test
    public void propertiesAreSet() {
        WebElement toggleImg = findElement(By.id("toggle-img"));
        WebElement toggleAbbr = findElement(By.id("toggle-abbr"));
        WebElement toggleName = findElement(By.id("toggle-name"));

        WebElement imgBlock = findElement(By.id("data-block-img"));
        WebElement abbrBlock = findElement(By.id("data-block-abbr"));
        WebElement nameBlock = findElement(By.id("data-block-name"));

        toggleImg.click();
        getPropsBtn.click();
        Assert.assertEquals("https://vaadin.com/", imgBlock.getText());

        toggleAbbr.click();
        getPropsBtn.click();
        Assert.assertEquals("BB", abbrBlock.getText());

        toggleName.click();
        getPropsBtn.click();
        Assert.assertEquals("Foo Bar", nameBlock.getText());
    }

    @Test
    public void propertiesAreUnset() {
        WebElement toggleImg = findElement(By.id("toggle-img"));
        WebElement toggleAbbr = findElement(By.id("toggle-abbr"));
        WebElement toggleName = findElement(By.id("toggle-name"));

        WebElement imgBlock = findElement(By.id("data-block-img"));
        WebElement abbrBlock = findElement(By.id("data-block-abbr"));
        WebElement nameBlock = findElement(By.id("data-block-name"));

        toggleImg.click();
        toggleAbbr.click();
        toggleName.click();
        getPropsBtn.click();

        toggleImg.click();
        getPropsBtn.click();
        Assert.assertEquals("", imgBlock.getText());

        toggleAbbr.click();
        getPropsBtn.click();
        Assert.assertEquals("", abbrBlock.getText());

        toggleName.click();
        getPropsBtn.click();
        Assert.assertEquals("", nameBlock.getText());
    }
}
