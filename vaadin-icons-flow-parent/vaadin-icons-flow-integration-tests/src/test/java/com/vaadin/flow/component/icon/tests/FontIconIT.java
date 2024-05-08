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
package com.vaadin.flow.component.icon.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-icons/font-icons")
public class FontIconIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void fontIconUsingFont() {
        var icon = findElement(By.className("fa-user"));
        // Get the ::before element's width and height separated by a space
        var beforeBounds = executeScript(
                """
                        const { width, height } = window.getComputedStyle(arguments[0], '::before');
                        return `${width} ${height}`;
                        """,
                icon);

        // Expect the icon size to match FontAwesome "user" icon's size
        Assert.assertEquals("21px 24px", beforeBounds);
    }
}
