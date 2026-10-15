/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.sidenav.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link SideNavI18NPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-side-nav/side-nav-i18n")
public class SideNavI18NIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void itemsHaveCorrectDefaultI18N() {
        List<SideNavItemElement> elements = $(SideNavItemElement.class).all();
        elements.forEach(element -> Assert.assertEquals("Toggle child items",
                getI18nText(element)));
    }

    @Test
    public void setI18n_i18nIsUpdated() {
        $("button").id("set-i18n").click();

        List<SideNavItemElement> elements = $(SideNavItemElement.class).all();
        elements.forEach(element -> Assert.assertEquals("Updated",
                getI18nText(element)));
    }

    private String getI18nText(SideNavItemElement element) {
        return element.getWrappedElement().getShadowRoot()
                .findElement(By.id("i18n")).getText();
    }
}
