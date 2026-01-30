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
package com.vaadin.flow.component.stepper;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouteParameters;

/**
 * Tests for {@link Step}.
 */
public class StepTest {

    @Test
    public void createEmptyStep() {
        Step step = new Step();

        Assert.assertEquals("Tag name is invalid", "vaadin-step",
                step.getElement().getTag());
        Assert.assertEquals("Label should be empty", "", step.getLabel());
        Assert.assertEquals("Description should be empty", "",
                step.getDescription());
        Assert.assertEquals("Href should be empty", "", step.getHref());
        Assert.assertEquals("Default state should be inactive",
                Step.State.INACTIVE, step.getState());
    }

    @Test
    public void createStepWithLabel() {
        Step step = new Step("Personal Info");

        Assert.assertEquals("Label is invalid", "Personal Info",
                step.getLabel());
        Assert.assertEquals("Href should be empty", "", step.getHref());
    }

    @Test
    public void createStepWithLabelAndDescription() {
        Step step = new Step("Personal Info", "Enter your details");

        Assert.assertEquals("Label is invalid", "Personal Info",
                step.getLabel());
        Assert.assertEquals("Description is invalid", "Enter your details",
                step.getDescription());
    }

    @Test
    public void createStepWithLabelAndHref() {
        Step step = Step.withHref("Personal Info", "/personal");

        Assert.assertEquals("Label is invalid", "Personal Info",
                step.getLabel());
        Assert.assertEquals("Href is invalid", "/personal", step.getHref());
    }

    @Test
    public void createStepWithComponent() {
        Div div = new Div("Content");
        Step step = new Step(div);

        Assert.assertEquals("Child count is invalid", 1,
                step.getElement().getChildCount());
        Assert.assertEquals("Child component is invalid", div.getElement(),
                step.getElement().getChild(0));
    }

    @Test
    public void createStepWithComponentAndHref() {
        Div div = new Div("Content");
        Step step = Step.withHref(div, "/step");

        Assert.assertEquals("Child count is invalid", 1,
                step.getElement().getChildCount());
        Assert.assertEquals("Href is invalid", "/step", step.getHref());
    }

    @Test
    public void setHref() {
        Step step = new Step();

        step.setHref("/products");
        Assert.assertEquals("Href is invalid", "/products", step.getHref());

        step.setHref(null);
        Assert.assertEquals("Href should be empty", "", step.getHref());

        step.setHref("");
        Assert.assertEquals("Href should be empty", "", step.getHref());
    }

    @Test
    public void setTarget() {
        Step step = new Step();

        step.setTarget("_blank");
        Assert.assertEquals("Target is invalid", "_blank", step.getTarget());

        step.setTarget(null);
        Assert.assertEquals("Target should be empty", "", step.getTarget());

        step.setTarget("");
        Assert.assertEquals("Target should be empty", "", step.getTarget());
    }

    @Test
    public void setLabel() {
        Step step = new Step();

        step.setLabel("Payment");
        Assert.assertEquals("Label is invalid", "Payment", step.getLabel());

        step.setLabel(null);
        Assert.assertEquals("Label should be empty", "", step.getLabel());
    }

    @Test
    public void setDescription() {
        Step step = new Step();

        step.setDescription("Choose payment method");
        Assert.assertEquals("Description is invalid", "Choose payment method",
                step.getDescription());

        step.setDescription(null);
        Assert.assertEquals("Description should be empty", "",
                step.getDescription());
    }

    @Test
    public void setState() {
        Step step = new Step();

        Assert.assertEquals("Default state should be inactive",
                Step.State.INACTIVE, step.getState());

        step.setState(Step.State.ACTIVE);
        Assert.assertEquals("State should be active", Step.State.ACTIVE,
                step.getState());

        step.setState(Step.State.COMPLETED);
        Assert.assertEquals("State should be completed", Step.State.COMPLETED,
                step.getState());

        step.setState(Step.State.ERROR);
        Assert.assertEquals("State should be error", Step.State.ERROR,
                step.getState());

        step.setState(null);
        Assert.assertEquals("State should be inactive", Step.State.INACTIVE,
                step.getState());
    }

    @Test
    public void stateConvenienceMethods() {
        Step step = new Step();

        // Test isInactive and setInactive
        Assert.assertTrue("Step should be inactive by default",
                step.isInactive());
        step.setInactive();
        Assert.assertTrue("Step should be inactive", step.isInactive());
        Assert.assertFalse("Step should not be active", step.isActive());
        Assert.assertFalse("Step should not be completed", step.isCompleted());
        Assert.assertFalse("Step should not be error", step.isError());

        // Test isActive and setActive
        step.setActive();
        Assert.assertTrue("Step should be active", step.isActive());
        Assert.assertFalse("Step should not be inactive", step.isInactive());
        Assert.assertFalse("Step should not be completed", step.isCompleted());
        Assert.assertFalse("Step should not be error", step.isError());

        // Test isCompleted and setCompleted
        step.setCompleted();
        Assert.assertTrue("Step should be completed", step.isCompleted());
        Assert.assertFalse("Step should not be inactive", step.isInactive());
        Assert.assertFalse("Step should not be active", step.isActive());
        Assert.assertFalse("Step should not be error", step.isError());

        // Test isError and setError
        step.setError();
        Assert.assertTrue("Step should be error", step.isError());
        Assert.assertFalse("Step should not be inactive", step.isInactive());
        Assert.assertFalse("Step should not be active", step.isActive());
        Assert.assertFalse("Step should not be completed", step.isCompleted());
    }

    @Test
    public void setRouterIgnore() {
        Step step = new Step();

        Assert.assertFalse("Default routerIgnore should be false",
                step.isRouterIgnore());

        step.setRouterIgnore(true);
        Assert.assertTrue("RouterIgnore should be true", step.isRouterIgnore());

        step.setRouterIgnore(false);
        Assert.assertFalse("RouterIgnore should be false",
                step.isRouterIgnore());
    }

    @Test
    public void setText() {
        Step step = new Step();

        step.setText("Step Content");
        Assert.assertEquals("Text is invalid", "Step Content", step.getText());

        step.setText(null);
        Assert.assertEquals("Text should be empty", "", step.getText());
    }

    @Test
    public void addComponent() {
        Step step = new Step();
        Div div1 = new Div("First");
        Div div2 = new Div("Second");

        step.add(div1, div2);

        Assert.assertEquals("Child count is invalid", 2,
                step.getElement().getChildCount());
        Assert.assertEquals("First child is invalid", div1.getElement(),
                step.getElement().getChild(0));
        Assert.assertEquals("Second child is invalid", div2.getElement(),
                step.getElement().getChild(1));
    }

    @Test
    public void removeComponent() {
        Step step = new Step();
        Div div1 = new Div("First");
        Div div2 = new Div("Second");

        step.add(div1, div2);
        step.remove(div1);

        Assert.assertEquals("Child count is invalid", 1,
                step.getElement().getChildCount());
        Assert.assertEquals("Remaining child is invalid", div2.getElement(),
                step.getElement().getChild(0));
    }

    @Test
    public void removeAllComponents() {
        Step step = new Step();
        Div div1 = new Div("First");
        Div div2 = new Div("Second");

        step.add(div1, div2);
        step.removeAll();

        Assert.assertEquals("Child count should be 0", 0,
                step.getElement().getChildCount());
    }

    @Test
    public void setEnabled() {
        Step step = new Step();

        Assert.assertTrue("Default enabled should be true", step.isEnabled());

        step.setEnabled(false);
        Assert.assertFalse("Enabled should be false", step.isEnabled());

        step.setEnabled(true);
        Assert.assertTrue("Enabled should be true", step.isEnabled());
    }

    @Test
    public void setTooltip() {
        Step step = new Step();

        // HasTooltip creates a tooltip immediately, so we check if text is
        // null/empty
        Assert.assertTrue("Default tooltip text should be null or empty",
                step.getTooltip() == null || step.getTooltip().getText() == null
                        || step.getTooltip().getText().isEmpty());

        step.setTooltipText("This is a tooltip");
        Assert.assertNotNull("Tooltip should not be null", step.getTooltip());
        Assert.assertEquals("Tooltip text is invalid", "This is a tooltip",
                step.getTooltip().getText());
    }

    @Test
    public void stateFromValue() {
        Assert.assertEquals("Active state conversion", Step.State.ACTIVE,
                Step.State.fromValue("active"));
        Assert.assertEquals("Completed state conversion", Step.State.COMPLETED,
                Step.State.fromValue("completed"));
        Assert.assertEquals("Error state conversion", Step.State.ERROR,
                Step.State.fromValue("error"));
        Assert.assertEquals("Inactive state conversion", Step.State.INACTIVE,
                Step.State.fromValue("inactive"));
        Assert.assertEquals("Unknown state conversion", Step.State.INACTIVE,
                Step.State.fromValue("unknown"));
        Assert.assertEquals("Null state conversion", Step.State.INACTIVE,
                Step.State.fromValue(null));
    }

    @Test
    public void constructorsWithNavigationTarget() {
        // Test constructor with navigation target - will throw without router
        try {
            Step step1 = new Step("Test", TestView.class);
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
            Step step2 = new Step("Test", TestView.class, params);
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