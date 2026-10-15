/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
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
