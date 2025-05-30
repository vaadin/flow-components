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
package com.vaadin.flow.component.orderedlayout.tests;

import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class LayoutDefaultsTest {

    @Test
    public void testHorizontalLayout_byDefault_spacingIsOn() {
        Assert.assertTrue("Spacing should be on by default",
                new HorizontalLayout().isSpacing());
        Assert.assertFalse("Padding shouldn't be on by default",
                new HorizontalLayout().isPadding());
        Assert.assertFalse("Margin shouldn't be on by default",
                new HorizontalLayout().isMargin());
    }

    @Test
    public void testHorizontalLayout_withJustifyContentModeAndChildren_justifyContentModeIsSet() {
        HorizontalLayout layout = new HorizontalLayout(
                FlexComponent.JustifyContentMode.END, new Span(),
                new NativeButton());
        Assert.assertEquals("JustifyContentMode should be set by constructor",
                FlexComponent.JustifyContentMode.END,
                layout.getJustifyContentMode());
        Assert.assertEquals("Children components must be added by constructor",
                2, layout.getChildren().count());
    }

    @Test
    public void testHorizontalLayout_withAlignmentAndChildren_DefaultVerticalAlignmentIsSet() {
        HorizontalLayout layout = new HorizontalLayout(Alignment.STRETCH,
                new Span(), new NativeButton());
        Assert.assertEquals(
                "DefaultVerticalAlignment should be set by constructor",
                Alignment.STRETCH, layout.getAlignItems());
        Assert.assertEquals("Children components must be added by constructor",
                2, layout.getChildren().count());
    }

    @Test
    public void testVerticalLayout_withJustifyContentModeAndChildren_justifyContentModeIsSet() {
        VerticalLayout layout = new VerticalLayout(
                FlexComponent.JustifyContentMode.END, new Span(),
                new NativeButton());
        Assert.assertEquals("JustifyContentMode should be set by constructor",
                FlexComponent.JustifyContentMode.END,
                layout.getJustifyContentMode());
        Assert.assertEquals("Children components must be added by constructor",
                2, layout.getChildren().count());
    }

    @Test
    public void testVerticalLayout_withAlignmentAndChildren_DefaultVerticalAlignmentIsSet() {
        VerticalLayout layout = new VerticalLayout(Alignment.STRETCH,
                new Span(), new NativeButton());
        Assert.assertEquals(
                "DefaultHorizontalAlignment should be set by constructor",
                Alignment.STRETCH, layout.getAlignItems());
        Assert.assertEquals("Children components must be added by constructor",
                2, layout.getChildren().count());
    }

    @Test
    public void testVerticalLayout_byDefault_spacingAndPaddingIsOn() {
        Assert.assertTrue("Padding should be on by default",
                new VerticalLayout().isPadding());
        Assert.assertTrue("Spacing should be on by default",
                new VerticalLayout().isSpacing());
        Assert.assertFalse("Margin shouldn't be on by default",
                new VerticalLayout().isMargin());
    }

    @Test
    public void create_Layout() {
        // Just testing that creating layout actually compiles and doesn't
        // throw. Test is on purpose, so that the implementation not
        // accidentally removed.
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addClickListener(event -> {
        });

        FlexLayout flexLayout = new FlexLayout();
        flexLayout.addClickListener(event -> {
        });

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addClickListener(event -> {
        });
    }

    @Test
    public void defaultAlignmentValues() {
        VerticalLayout verticalLayout = new VerticalLayout();
        Assert.assertEquals(Alignment.START,
                verticalLayout.getDefaultHorizontalComponentAlignment());

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Assert.assertEquals(Alignment.STRETCH,
                horizontalLayout.getDefaultVerticalComponentAlignment());

    }

    @Test
    public void expandable_Layout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addAndExpand(new Span("Foo"), new Span("bar"));
        testExpandableComponent(horizontalLayout.getWidth(),
                horizontalLayout.getChildren());

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addAndExpand(new Span("Foo"), new Span("bar"));
        testExpandableComponent(verticalLayout.getHeight(),
                verticalLayout.getChildren());
    }

    private void testExpandableComponent(String size,
            Stream<Component> components) {
        Assert.assertEquals(size, "100%");

        components.forEach(component -> Assert.assertEquals(
                component.getElement().getStyle().get("flex-grow"), "1.0"));
    }
}
