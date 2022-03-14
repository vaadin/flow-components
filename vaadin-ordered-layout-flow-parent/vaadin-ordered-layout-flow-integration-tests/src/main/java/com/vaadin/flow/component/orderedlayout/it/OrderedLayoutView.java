/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

import java.util.function.Consumer;

/**
 * View for the ordered layouts {@link HorizontalLayout} and
 * {@link VerticalLayout}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ordered-layout")
public class OrderedLayoutView extends AbstractLayout {

    public OrderedLayoutView() {
        createDefaultHorizontalLayout();
        createHorizontalLayoutWithJustifyContent();
        createHorizontalLayoutWithDefaultAlignment();
        createHorizontalLayoutWithIndividualAlignments();
        createHorizontalLayoutWithExpandRatios();
        createHorizontalLayoutWithCenterComponent();
        createHorizontalLayoutWithBoxSizing();
        createHorizontalLayoutWithExpandingContent();
        horizontalLayoutFixedHeight();
        horizontalLayoutAligningItems();
        horizontalLayoutExpandingOneComponent();
        horizontalLayoutExpandingAllComponents();
        horizontalLayoutSplitPositioning();
        horizontalLayoutAdvancedSplitPositioning1();
        horizontalLayoutAdvancedSplitPositioning2();

        createDefaultVerticalLayout();
        createVerticalLayoutWithJustifyContent();
        createVerticalLayoutWithDefaultAlignment();
        createVerticalLayoutWithIndividualAlignments();
        createVerticalLayoutWithExpandRatios();
        createVerticalLayoutWithCenterComponent();
        createVerticalLayoutWithBoxSizing();
        createVerticalLayoutWithExpandingContent();
        verticalLayoutAligningItems();
        verticalLayoutExpandingOneComponent();
        verticalLayoutExpandingAllComponents();
        verticalLayoutSplitPositioning();
        verticalLayoutAdvancedSplitPositioning1();
        verticalLayoutAdvancedSplitPositioning2();

        createFlexLayoutWithAlignmentContent();
        createFlexLayoutWithFlexBasis();
        createFlexLayoutWithFlexDirection();
        createFlexLayoutWithFlexShrink();
        createFlexLayoutWithOrderedItems();

        createScroller();
        createScrollerWithVerticalLayout();
    }

    /* FlexLayout demos */

    private void createFlexLayoutWithAlignmentContent() {
        FlexLayout layout = new FlexLayout();
        layout.setWidth("130px");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        layout.setId("flex-layout-with-alignment-content");

        Consumer<FlexLayout.ContentAlignment> changeLayout = alignment -> layout
                .setAlignContent(alignment);
        addCard("FlexLayout", "FlexLayout with alignment content", layout,
                createRadioButtonGroup(FlexLayout.ContentAlignment.values(),
                        changeLayout, layout.getAlignContent()));
    }

    private void createFlexLayoutWithFlexBasis() {
        FlexLayout layout = new FlexLayout();
        layout.setWidth("100%");
        layout.setHeight("50px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        RadioButtonGroup<String> widths = new RadioButtonGroup<>();
        widths.setItems("200px", "100%", "auto");
        widths.setValue("auto");
        widths.addValueChangeListener(
                event -> layout.setFlexBasis(event.getValue(), component1));

        layout.setId("flex-layout-with-flex-basis");

        addCard("FlexLayout", "FlexLayout with flex basis", layout, widths);
    }

    private void createFlexLayoutWithFlexDirection() {
        FlexLayout layout = new FlexLayout();
        layout.setWidth("100%");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        Consumer<FlexLayout.FlexDirection> flexDirectionConsumer = flexDirection -> layout
                .setFlexDirection(flexDirection);
        RadioButtonGroup<FlexLayout.FlexDirection> rbg = createRadioButtonGroup(
                FlexLayout.FlexDirection.values(), flexDirectionConsumer,
                FlexLayout.FlexDirection.ROW);

        layout.setId("flex-layout-with-flex-direction");

        addCard("FlexLayout", "FlexLayout with flex direction", layout, rbg);
    }

    private void createFlexLayoutWithFlexShrink() {
        FlexLayout layout = new FlexLayout();
        layout.setWidth("100%");
        layout.setHeight("50px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.setFlexBasis("500px", component1, component2, component3);
        layout.add(component1, component2, component3);

        RadioButtonGroup<Integer> shrinkValues = new RadioButtonGroup<>();
        shrinkValues.setItems(0, 1, 2);
        shrinkValues.setValue(1);
        shrinkValues.addValueChangeListener(
                event -> layout.setFlexShrink(event.getValue(), component1));

        layout.setId("flex-layout-with-flex-shrink");

        addCard("FlexLayout", "FlexLayout with flex shrink", layout,
                shrinkValues);
    }

    private void createFlexLayoutWithOrderedItems() {
        FlexLayout layout = new FlexLayout();
        layout.setWidth("100%");
        layout.setHeight("50px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        layout.setOrder(0, component3);
        layout.setOrder(1, component1);
        layout.setOrder(2, component2);

        add(layout);

        layout.setId("flex-layout-with-ordered-items");

        addCard("FlexLayout", "FlexLayout with ordered items", layout);
    }

    /* HorizontalLayout demos */

    private void createDefaultHorizontalLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        // shorthand methods for changing the component theme variants
        layout.setPadding(false);
        layout.setMargin(true);
        // just a demonstration of the API, by default the spacing is on
        layout.setSpacing(true);

        add(layout);

        layout.setId("default-layout");

        Div themeSettings = new Div(
                createToggleThemeCheckbox("padding", layout::setPadding,
                        layout.isPadding()),
                createToggleThemeCheckbox("margin", layout::setMargin,
                        layout.isMargin()),
                createToggleThemeCheckbox("spacing", layout::setSpacing,
                        layout.isSpacing()));

        addCard("HorizontalLayout", "Default horizontal layout", layout,
                themeSettings);
    }

    private void createHorizontalLayoutWithJustifyContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        // the default is JustifyContentMode.START
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        layout.add(component1, component2, component3);

        add(layout);

        component2.getElement().setText("Component 2 with long text");
        component3.getElement().setText("C 3");

        RadioButtonGroup<FlexComponent.JustifyContentMode> justifyContentMode = new RadioButtonGroup<>();
        justifyContentMode.setItems(FlexComponent.JustifyContentMode.values());
        justifyContentMode.setRenderer(new TextRenderer<>(
                justifyContent -> justifyContent.name().toLowerCase()));
        justifyContentMode.addValueChangeListener(
                event -> layout.setJustifyContentMode(event.getValue()));
        justifyContentMode
                .setId("horizontal-layout-justify-content-radio-button");
        justifyContentMode.setValue(FlexComponent.JustifyContentMode.BETWEEN);
        layout.setId("layout-with-justify-content");

        addCard("HorizontalLayout", "HorizontalLayout with justify content",
                layout, justifyContentMode);
    }

    private void createHorizontalLayoutWithDefaultAlignment() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // the default is Alignment.BASELINE
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        add(layout);

        component2.getElement().getStyle().set("fontSize", "24px");
        component3.getElement().getStyle().set("fontSize", "9px");

        RadioButtonGroup<Alignment> alignments = new RadioButtonGroup<>();
        alignments.setItems(Alignment.values());
        alignments.setRenderer(new TextRenderer<>(
                alignment -> alignment.name().toLowerCase()));
        alignments.setValue(Alignment.CENTER);
        alignments.setId("horizontal-layout-alignment-radio-button");
        alignments.addValueChangeListener(event -> layout
                .setDefaultVerticalComponentAlignment(event.getValue()));

        layout.setId("layout-with-alignment");

        addCard("HorizontalLayout", "HorizontalLayout with general alignment",
                layout, alignments);
    }

    private void createHorizontalLayoutWithIndividualAlignments() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Component component1 = createComponent(1, "#78909C");
        layout.setVerticalComponentAlignment(Alignment.START, component1);

        Component component2 = createComponent(2, "#546E7A");
        layout.setVerticalComponentAlignment(Alignment.CENTER, component2);

        Component component3 = createComponent(3, "#37474F");
        layout.setVerticalComponentAlignment(Alignment.END, component3);

        Component component4 = createComponent(4, "#263238");
        layout.setVerticalComponentAlignment(Alignment.STRETCH, component4);

        layout.add(component1, component2, component3, component4);

        add(layout);

        component1.setId("start-aligned");
        component2.setId("center-aligned");
        component3.setId("end-aligned");
        component4.setId("stretch-aligned");
        layout.setId("layout-with-individual-alignments");

        addCard("HorizontalLayout",
                "HorizontalLayout with individual alignments", layout);
    }

    private void createHorizontalLayoutWithExpandRatios() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        layout.expand(component1);

        Component component2 = createComponent(2, "#546E7A");
        layout.setFlexGrow(2, component2);

        Component component3 = createComponent(3, "#37474F");
        layout.setFlexGrow(0.5, component3);

        layout.add(component1, component2, component3);

        add(layout);

        component1.setId("ratio-1");
        component2.setId("ratio-2");
        component3.setId("ratio-0.5");
        layout.setId("layout-with-expand-ratios");

        addCard("HorizontalLayout", "HorizontalLayout with expand ratios",
                layout);
    }

    private void createHorizontalLayoutWithCenterComponent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("200px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component = createComponent(1, "#78909C");
        layout.add(component);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        add(layout);

        component.setId("center");
        layout.setId("layout-with-center");

        addCard("HorizontalLayout",
                "HorizontalLayout with component in the center", layout);
    }

    private void createHorizontalLayoutWithBoxSizing() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("300px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setPadding(true);

        /*
         * Makes the component consider border and paddings when computing the
         * size
         */
        layout.setBoxSizing(BoxSizing.BORDER_BOX);

        Div component1 = createComponent(1, "#78909C");
        component1.setWidth("50%");
        Div component2 = createComponent(2, "#546E7A");
        component2.setWidth("50%");
        layout.add(component1, component2);

        add(layout);

        layout.setId("horizontal-layout-with-box-sizing");

        addCard("HorizontalLayout", "HorizontalLayout with box-sizing settings",
                layout,
                createBoxSizingButtons(layout, layout.getId().orElse(null)));
    }

    private void createHorizontalLayoutWithExpandingContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Div component1 = createLoremIpsum();
        Div component2 = createLoremIpsum();
        Div component3 = createLoremIpsum();

        layout.addAndExpand(component1, component2, component3);

        add(layout);

        layout.setId("horizontal-layout-with-expanding-content");

        addCard("HorizontalLayout", "Horizontal layout with expanding content",
                layout);
    }

    private void horizontalLayoutFixedHeight() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);
        layout.setHeight("150px");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        layout.add(component1, component2);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        component1.getElement().getStyle().set("height", "fit-content");
        component2.getElement().getStyle().set("height", "fit-content");
        addCard("HorizontalLayout", "Fixed height", layout);
    }

    private void horizontalLayoutAligningItems() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);
        layout.setHeight("150px");
        layout.setAlignItems(Alignment.END);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        layout.add(component1, component2);

        add(layout);
        component1.getElement().getStyle().set("height", "fit-content");
        component2.getElement().getStyle().set("height", "fit-content");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("HorizontalLayout", "Horizontally aligning items", layout);
    }

    private void horizontalLayoutExpandingOneComponent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        layout.add(component1, component2);
        // this expands the button
        layout.setFlexGrow(1, component1);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("HorizontalLayout", "Horizontally expanding one component",
                layout);
    }

    private void horizontalLayoutExpandingAllComponents() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        // adds and flex-grows both components
        layout.addAndExpand(component1, component2);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("HorizontalLayout", "Horizontally expanding all components",
                layout);
    }

    private void horizontalLayoutSplitPositioning() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        // expands the empty space left of button two
        component2.getElement().getStyle().set("margin-left", "auto");
        layout.add(component1, component2);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("HorizontalLayout", "Horizontally split positioning", layout);
    }

    private void horizontalLayoutAdvancedSplitPositioning1() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        component2.getElement().getStyle().set("margin-right", "auto");
        layout.add(component1, component2, component3);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("HorizontalLayout", "Advanced horizontally split positioning 1",
                layout);
    }

    private void horizontalLayoutAdvancedSplitPositioning2() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(true);
        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");
        // expands the empty space left of button two
        component2.getElement().getStyle().set("margin-left", "auto");
        layout.add(component1, component2, component3);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("HorizontalLayout", "Advanced horizontally split positioning 2",
                layout);
    }

    /* VerticalLayout demos */

    private void createDefaultVerticalLayout() {
        // padding and spacing is on by default
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        // shorthand methods for changing the component theme variants
        layout.setPadding(false);
        layout.setMargin(true);
        // just a demonstration of the API, by default the spacing is on
        layout.setSpacing(true);

        add(layout);

        layout.setId("vertical-default-layout");

        Div themeSettings = new Div(
                createToggleThemeCheckbox("vertical-padding",
                        layout::setPadding, layout.isPadding()),
                createToggleThemeCheckbox("vertical-margin", layout::setMargin,
                        layout.isMargin()),
                createToggleThemeCheckbox("vertical-spacing",
                        layout::setSpacing, layout.isSpacing()));

        addCard("VerticalLayout", "Default vertical layout", layout,
                themeSettings);
    }

    private void createVerticalLayoutWithJustifyContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setHeight("300px");

        // the default is JustifyContentMode.START
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        add(layout);

        component2.getElement().setProperty("innerHTML",
                "Component 2<br>With long text");
        component3.getElement().getStyle().set("fontSize", "9px");

        RadioButtonGroup<FlexComponent.JustifyContentMode> justifyContentMode = new RadioButtonGroup<>();
        justifyContentMode.setItems(FlexComponent.JustifyContentMode.values());
        justifyContentMode.setRenderer(new TextRenderer<>(
                justifyContent -> justifyContent.name().toLowerCase()));
        justifyContentMode
                .setId("vertical-layout-justify-content-radio-button");
        justifyContentMode.addValueChangeListener(
                event -> layout.setJustifyContentMode(event.getValue()));
        justifyContentMode.setValue(FlexComponent.JustifyContentMode.BETWEEN);
        layout.setId("vertical-layout-with-justify-content");

        addCard("VerticalLayout", "VerticalLayout with justify content", layout,
                justifyContentMode);
    }

    private void createVerticalLayoutWithDefaultAlignment() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // the default is Alignment.START
        layout.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        add(layout);

        component2.getElement().setText("Component 2 with long text");
        component3.getElement().setText("C 3");

        RadioButtonGroup<Alignment> alignments = new RadioButtonGroup<>();
        alignments.setItems(Alignment.values());
        alignments.setRenderer(new TextRenderer<>(
                alignment -> alignment.name().toLowerCase()));
        alignments.setValue(Alignment.STRETCH);
        alignments.setId("vertical-layout-alignment-radio-button");
        alignments.addValueChangeListener(event -> layout
                .setDefaultHorizontalComponentAlignment(event.getValue()));

        layout.setId("vertical-layout-with-alignment");

        addCard("VerticalLayout", "VerticalLayout with general alignment",
                layout, alignments);
    }

    private void createVerticalLayoutWithIndividualAlignments() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Component component1 = createComponent(1, "#78909C");
        layout.setHorizontalComponentAlignment(Alignment.START, component1);

        Component component2 = createComponent(2, "#546E7A");
        layout.setHorizontalComponentAlignment(Alignment.CENTER, component2);

        Component component3 = createComponent(3, "#37474F");
        layout.setHorizontalComponentAlignment(Alignment.END, component3);

        Component component4 = createComponent(4, "#263238");
        layout.setHorizontalComponentAlignment(Alignment.STRETCH, component4);

        layout.add(component1, component2, component3, component4);

        add(layout);

        component1.setId("start-aligned");
        component2.setId("center-aligned");
        component3.setId("end-aligned");
        component4.setId("stretch-aligned");
        layout.setId("vertical-layout-with-individual-alignments");

        addCard("VerticalLayout", "VerticalLayout with individual alignments",
                layout);
    }

    private void createVerticalLayoutWithExpandRatios() {
        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("200px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setDefaultHorizontalComponentAlignment(Alignment.STRETCH);

        Component component1 = createComponent(1, "#78909C");
        layout.expand(component1);

        Component component2 = createComponent(2, "#546E7A");
        layout.setFlexGrow(2, component2);

        Component component3 = createComponent(3, "#37474F");
        layout.setFlexGrow(0.5, component3);

        layout.add(component1, component2, component3);

        add(layout);

        component1.setId("ratio-1");
        component2.setId("ratio-2");
        component3.setId("ratio-0.5");
        layout.setId("vertical-layout-with-expand-ratios");

        addCard("VerticalLayout", "VerticalLayout with expand ratios", layout);
    }

    private void createVerticalLayoutWithCenterComponent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("200px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component = createComponent(1, "#78909C");
        layout.add(component);
        layout.setHorizontalComponentAlignment(Alignment.CENTER, component);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        add(layout);

        component.setId("center");
        layout.setId("vertical-layout-with-center");

        addCard("VerticalLayout", "VerticalLayout with component in the center",
                layout);
    }

    private void createVerticalLayoutWithBoxSizing() {
        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("200px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setPadding(true);

        /*
         * Makes the component consider border and paddings when computing the
         * size
         */
        layout.setBoxSizing(BoxSizing.BORDER_BOX);

        Div component1 = createComponent(1, "#78909C");
        component1.setHeight("50%");
        Div component2 = createComponent(2, "#546E7A");
        component2.setHeight("50%");
        layout.add(component1, component2);

        add(layout);

        layout.setId("vertical-layout-with-box-sizing");

        addCard("VerticalLayout", "VerticalLayout with box-sizing settings",
                layout,
                createBoxSizingButtons(layout, layout.getId().orElse(null)));
    }

    private void createVerticalLayoutWithExpandingContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Div component1 = createLoremIpsum();
        Div component2 = createLoremIpsum();
        Div component3 = createLoremIpsum();

        layout.addAndExpand(component1, component2, component3);

        add(layout);

        layout.setId("vertical-layout-with-expanding-content");

        addCard("VerticalLayout", "Vertical layout with expanding content",
                layout);
    }

    private void verticalLayoutAligningItems() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setAlignItems(Alignment.END);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        layout.add(component1, component2);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("VerticalLayout", "Vertically aligning items", layout);
    }

    private void verticalLayoutExpandingOneComponent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setHeight("300px");
        Component component1 = createComponent(1, "#78909C");
        component1.getElement().getStyle().set("flex-shrink", "0");
        Component component2 = createComponent(2, "#546E7A");
        layout.add(component1, component2);
        // this expands the component
        layout.setFlexGrow(1, component1);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("VerticalLayout", "Vertically expanding one component", layout);
    }

    private void verticalLayoutExpandingAllComponents() {
        VerticalLayout layout = new VerticalLayout();

        layout.setPadding(true);
        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");

        layout.addAndExpand(component1, component2);
        // setHeight needs to be defined last because of
        // https://github.com/vaadin/vaadin-ordered-layout-flow/issues/134
        layout.setHeight("300px");

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("VerticalLayout", "Vertically expanding all components",
                layout);
    }

    private void verticalLayoutSplitPositioning() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setHeight("300px");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        // expands the empty space above button two
        component2.getElement().getStyle().set("margin-top", "auto");
        layout.add(component1, component2);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("VerticalLayout", "Vertically Split positioning", layout);
    }

    private void verticalLayoutAdvancedSplitPositioning1() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setHeight("400px");
        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        // expands the empty space below button two
        component2.getElement().getStyle().set("margin-bottom", "auto");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        add(layout);

        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("VerticalLayout", "Advanced vertically split positioning 1",
                layout);
    }

    private void verticalLayoutAdvancedSplitPositioning2() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setHeight("400px");
        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        // expands the empty space above button two
        component2.getElement().getStyle().set("margin-top", "auto");
        Component component3 = createComponent(3, "#37474F");
        layout.add(component1, component2, component3);

        add(layout);
        layout.getStyle().set("border", "1px solid #9E9E9E");
        addCard("VerticalLayout", "Advanced vertically split positioning 2",
                layout);
    }

    private void createScroller() {
        Scroller scroller = new Scroller();
        scroller.setHeight("100px");
        scroller.setWidth("200px");
        scroller.getStyle().set("border", "1px solid #9E9E9E");

        Div content = createLoremIpsum();

        scroller.setContent(content);

        add(scroller);

        scroller.setId("small-scroller-large-content");

        addCard("Scroller", "Small Scroller with large content", scroller);
    }

    private void createScrollerWithVerticalLayout() {
        String[] colors = new String[] { "#33C3F3", "#66D2F6", "#99E1F9",
                "#CCF0FC", "#E5F7FD", "#E5F7FD", "#CCF0FC", "#99E1F9",
                "#66D2F6", "#33C3F3" };

        Scroller scroller = new Scroller();
        scroller.setHeight("200px");

        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(Alignment.STRETCH);

        for (int i = 0; i < colors.length; i++) {
            Span component = new Span("Component " + i);
            component.getStyle().set("backgroundColor", colors[i])
                    .set("padding", "5px 10px");
            content.add(component);
        }

        scroller.setContent(content);

        add(scroller);

        scroller.setId("scroller-with-vertical-layout");

        addCard("Scroller", "Scroller with VerticalLayout content", scroller);
    }

    private void addCard(String title, String description,
            Component... components) {

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        if (description != null) {
            layout.add(new Span(description));
        }
        layout.add(components);
        add(layout);
    }
}
