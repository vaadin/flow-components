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

import com.vaadin.flow.component.avatar.testbench.AvatarGroupElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for the {@link AvatarGroupPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("avatar-group-test")
public class AvatarGroupIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void setItems_updateItemProperties_avatarsUpdated() {
        WebElement updateItems = findElement(By.id("update-items"));

        Assert.assertEquals("YY", getAvatarAbbr(0));
        Assert.assertEquals("SK", getAvatarAbbr(1));

        updateItems.click();

        Assert.assertEquals("FF", getAvatarAbbr(0));
        Assert.assertEquals("FF", getAvatarAbbr(1));
    }

    private String getAvatarAbbr(int index) {
        return $(AvatarGroupElement.class)
                .waitForFirst().getAvatarElement(index).getAbbr();
    }
}
