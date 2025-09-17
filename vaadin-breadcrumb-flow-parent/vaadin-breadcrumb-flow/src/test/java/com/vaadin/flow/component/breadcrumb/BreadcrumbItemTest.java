/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.breadcrumb;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouteParameters;

/**
 * Tests for {@link BreadcrumbItem}.
 */
public class BreadcrumbItemTest {

    @Test
    public void createEmptyItem() {
        BreadcrumbItem item = new BreadcrumbItem();

        Assert.assertEquals("Tag name is invalid", "vaadin-breadcrumb-item",
                item.getElement().getTag());
        Assert.assertEquals("Text should be empty", "", item.getText());
        Assert.assertEquals("Href should be empty", "", item.getHref());
    }

    @Test
    public void createItemWithText() {
        BreadcrumbItem item = new BreadcrumbItem("Home");

        Assert.assertEquals("Text is invalid", "Home", item.getText());
        Assert.assertEquals("Href should be empty", "", item.getHref());
    }

    @Test
    public void createItemWithTextAndHref() {
        BreadcrumbItem item = new BreadcrumbItem("Home", "/home");

        Assert.assertEquals("Text is invalid", "Home", item.getText());
        Assert.assertEquals("Href is invalid", "/home", item.getHref());
    }

    @Test
    public void createItemWithComponent() {
        Div div = new Div("Content");
        BreadcrumbItem item = new BreadcrumbItem(div);

        Assert.assertEquals("Child count is invalid", 1,
                item.getElement().getChildCount());
        Assert.assertEquals("Child component is invalid", div.getElement(),
                item.getElement().getChild(0));
    }

    @Test
    public void createItemWithComponentAndHref() {
        Div div = new Div("Content");
        BreadcrumbItem item = new BreadcrumbItem(div, "/home");

        Assert.assertEquals("Child count is invalid", 1,
                item.getElement().getChildCount());
        Assert.assertEquals("Href is invalid", "/home", item.getHref());
    }

    @Test
    public void setHref() {
        BreadcrumbItem item = new BreadcrumbItem();

        item.setHref("/products");
        Assert.assertEquals("Href is invalid", "/products", item.getHref());

        item.setHref(null);
        Assert.assertEquals("Href should be empty", "", item.getHref());

        item.setHref("");
        Assert.assertEquals("Href should be empty", "", item.getHref());
    }

    @Test
    public void setTarget() {
        BreadcrumbItem item = new BreadcrumbItem();

        item.setTarget("_blank");
        Assert.assertEquals("Target is invalid", "_blank", item.getTarget());

        item.setTarget(null);
        Assert.assertEquals("Target should be empty", "", item.getTarget());

        item.setTarget("");
        Assert.assertEquals("Target should be empty", "", item.getTarget());
    }

    @Test
    public void setRouterIgnore() {
        BreadcrumbItem item = new BreadcrumbItem();

        Assert.assertFalse("Default routerIgnore should be false",
                item.isRouterIgnore());

        item.setRouterIgnore(true);
        Assert.assertTrue("RouterIgnore should be true", item.isRouterIgnore());

        item.setRouterIgnore(false);
        Assert.assertFalse("RouterIgnore should be false",
                item.isRouterIgnore());
    }

    @Test
    public void setText() {
        BreadcrumbItem item = new BreadcrumbItem();

        item.setText("Products");
        Assert.assertEquals("Text is invalid", "Products", item.getText());

        item.setText(null);
        Assert.assertEquals("Text should be empty", "", item.getText());
    }

    @Test
    public void addComponent() {
        BreadcrumbItem item = new BreadcrumbItem();
        Div div1 = new Div("First");
        Div div2 = new Div("Second");

        item.add(div1, div2);

        Assert.assertEquals("Child count is invalid", 2,
                item.getElement().getChildCount());
        Assert.assertEquals("First child is invalid", div1.getElement(),
                item.getElement().getChild(0));
        Assert.assertEquals("Second child is invalid", div2.getElement(),
                item.getElement().getChild(1));
    }

    @Test
    public void removeComponent() {
        BreadcrumbItem item = new BreadcrumbItem();
        Div div1 = new Div("First");
        Div div2 = new Div("Second");

        item.add(div1, div2);
        item.remove(div1);

        Assert.assertEquals("Child count is invalid", 1,
                item.getElement().getChildCount());
        Assert.assertEquals("Remaining child is invalid", div2.getElement(),
                item.getElement().getChild(0));
    }

    @Test
    public void removeAllComponents() {
        BreadcrumbItem item = new BreadcrumbItem();
        Div div1 = new Div("First");
        Div div2 = new Div("Second");

        item.add(div1, div2);
        item.removeAll();

        Assert.assertEquals("Child count should be 0", 0,
                item.getElement().getChildCount());
    }

    @Test
    public void setEnabled() {
        BreadcrumbItem item = new BreadcrumbItem();

        Assert.assertTrue("Default enabled should be true", item.isEnabled());

        item.setEnabled(false);
        Assert.assertFalse("Enabled should be false", item.isEnabled());

        item.setEnabled(true);
        Assert.assertTrue("Enabled should be true", item.isEnabled());
    }

    @Test
    public void setTooltip() {
        BreadcrumbItem item = new BreadcrumbItem();

        // HasTooltip creates a tooltip immediately, so we check if text is
        // null/empty
        Assert.assertTrue("Default tooltip text should be null or empty",
                item.getTooltip() == null || item.getTooltip().getText() == null
                        || item.getTooltip().getText().isEmpty());

        item.setTooltipText("This is a tooltip");
        Assert.assertNotNull("Tooltip should not be null", item.getTooltip());
        Assert.assertEquals("Tooltip text is invalid", "This is a tooltip",
                item.getTooltip().getText());
    }

    @Test
    public void constructorsWithNavigationTarget() {
        // Test constructor with navigation target - will throw without router
        try {
            BreadcrumbItem item1 = new BreadcrumbItem("Test", TestView.class);
            Assert.fail("Should throw exception without router");
        } catch (IllegalStateException | NullPointerException e) {
            // Expected if router configuration is not available
            // Can be either IllegalStateException or NullPointerException
            // depending on the state
            Assert.assertTrue("Expected router exception",
                    e.getMessage() != null
                            && (e.getMessage().contains("Cannot find a router")
                                    || e.getMessage().contains(
                                            "VaadinService.getCurrent()")));
        }

        // Test constructor with navigation target and route parameters - will
        // throw without router
        try {
            RouteParameters params = RouteParameters.empty();
            BreadcrumbItem item2 = new BreadcrumbItem("Test", TestView.class,
                    params);
            Assert.fail("Should throw exception without router");
        } catch (IllegalStateException | NullPointerException e) {
            // Expected if router configuration is not available
            // Can be either IllegalStateException or NullPointerException
            // depending on the state
            Assert.assertTrue("Expected router exception",
                    e.getMessage() != null
                            && (e.getMessage().contains("Cannot find a router")
                                    || e.getMessage().contains(
                                            "VaadinService.getCurrent()")));
        }
    }

    // Test view class for navigation tests
    private static class TestView extends Div {
    }
}
