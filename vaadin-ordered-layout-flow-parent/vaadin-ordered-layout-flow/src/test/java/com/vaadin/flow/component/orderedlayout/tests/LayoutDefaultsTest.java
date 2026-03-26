/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;

class LayoutDefaultsTest {

    @Test
    void testHorizontalLayout_byDefault_spacingIsOn() {
        Assertions.assertTrue(new HorizontalLayout().isSpacing(),
                "Spacing should be on by default");
        Assertions.assertFalse(new HorizontalLayout().isPadding(),
                "Padding shouldn't be on by default");
        Assertions.assertFalse(new HorizontalLayout().isMargin(),
                "Margin shouldn't be on by default");
    }

    @Test
    void testHorizontalLayout_withJustifyContentModeAndChildren_justifyContentModeIsSet() {
        HorizontalLayout layout = new HorizontalLayout(
                FlexComponent.JustifyContentMode.END, new Span(),
                new NativeButton());
        Assertions.assertEquals(FlexComponent.JustifyContentMode.END,
                layout.getJustifyContentMode(),
                "JustifyContentMode should be set by constructor");
        Assertions.assertEquals(2, layout.getChildren().count(),
                "Children components must be added by constructor");
    }

    @Test
    void testHorizontalLayout_withAlignmentAndChildren_DefaultVerticalAlignmentIsSet() {
        HorizontalLayout layout = new HorizontalLayout(Alignment.STRETCH,
                new Span(), new NativeButton());
        Assertions.assertEquals(Alignment.STRETCH, layout.getAlignItems(),
                "DefaultVerticalAlignment should be set by constructor");
        Assertions.assertEquals(2, layout.getChildren().count(),
                "Children components must be added by constructor");
    }

    @Test
    void testVerticalLayout_withJustifyContentModeAndChildren_justifyContentModeIsSet() {
        VerticalLayout layout = new VerticalLayout(
                FlexComponent.JustifyContentMode.END, new Span(),
                new NativeButton());
        Assertions.assertEquals(FlexComponent.JustifyContentMode.END,
                layout.getJustifyContentMode(),
                "JustifyContentMode should be set by constructor");
        Assertions.assertEquals(2, layout.getChildren().count(),
                "Children components must be added by constructor");
    }

    @Test
    void testVerticalLayout_withAlignmentAndChildren_DefaultVerticalAlignmentIsSet() {
        VerticalLayout layout = new VerticalLayout(Alignment.STRETCH,
                new Span(), new NativeButton());
        Assertions.assertEquals(Alignment.STRETCH, layout.getAlignItems(),
                "DefaultHorizontalAlignment should be set by constructor");
        Assertions.assertEquals(2, layout.getChildren().count(),
                "Children components must be added by constructor");
    }

    @Test
    void testVerticalLayout_byDefault_spacingAndPaddingIsOn() {
        Assertions.assertTrue(new VerticalLayout().isPadding(),
                "Padding should be on by default");
        Assertions.assertTrue(new VerticalLayout().isSpacing(),
                "Spacing should be on by default");
        Assertions.assertFalse(new VerticalLayout().isMargin(),
                "Margin shouldn't be on by default");
    }

    @Test
    void create_Layout() {
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
    void defaultAlignmentValues() {
        VerticalLayout verticalLayout = new VerticalLayout();
        Assertions.assertEquals(Alignment.START,
                verticalLayout.getDefaultHorizontalComponentAlignment());

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Assertions.assertEquals(Alignment.STRETCH,
                horizontalLayout.getDefaultVerticalComponentAlignment());

    }

    @Test
    void expandable_Layout() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addAndExpand(new Span("Foo"), new Span("bar"));
        testExpandableComponent(horizontalLayout.getWidth(),
                horizontalLayout.getChildren());

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addAndExpand(new Span("Foo"), new Span("bar"));
        testExpandableComponent(verticalLayout.getHeight(),
                verticalLayout.getChildren());
    }

    @Test
    void horizontalLayoutImplementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(HorizontalLayout.class));
    }

    @Test
    void verticalLayoutImplementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(VerticalLayout.class));
    }

    private void testExpandableComponent(String size,
            Stream<Component> components) {
        Assertions.assertEquals("100%", size);

        components.forEach(component -> Assertions.assertEquals("1.0",
                component.getElement().getStyle().get("flex-grow")));
    }
}
