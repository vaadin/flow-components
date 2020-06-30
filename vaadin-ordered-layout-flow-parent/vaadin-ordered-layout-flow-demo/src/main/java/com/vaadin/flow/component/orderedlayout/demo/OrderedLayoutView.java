/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.orderedlayout.demo;

import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

/**
 * View for the orderred layouts {@link HorizontalLayout} and
 * {@link VerticalLayout}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ordered-layout")
public class OrderedLayoutView extends AbstractLayout {

    @Override
    protected void initView() {
        createDefaultHorizontalLayout();
        createHorizontalLayoutWithJustifyContent();
        createHorizontalLayoutWithDefaultAlignment();
        createHorizontalLayoutWithIndividualAlignments();
        createHorizontalLayoutWithExpandRatios();
        createHorizontalLayoutWithCenterComponent();
        createHorizontalLayoutWithBoxSizing();
        createHorizontalLayoutWithExpandingContent();

        createDefaultVerticalLayout();
        createVerticalLayoutWithJustifyContent();
        createVerticalLayoutWithDefaultAlignment();
        createVerticalLayoutWithIndividualAlignments();
        createVerticalLayoutWithExpandRatios();
        createVerticalLayoutWithCenterComponent();
        createVerticalLayoutWithBoxSizing();
        createVerticalLayoutWithExpandingContent();

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
        // begin-source-example
        // source-example-heading: FlexLayout with alignment content
        FlexLayout layout = new FlexLayout();
        layout.setWidth("130px");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        // end-source-example

        layout.setId("flex-layout-with-alignment-content");

        Consumer<FlexLayout.ContentAlignment> changeLayout = alignment -> layout
              .setAlignContent(alignment);
        addCard("FlexLayout", "FlexLayout with alignment content", layout,
              createRadioButtonGroup(FlexLayout.ContentAlignment.values(),
                    changeLayout, layout.getAlignContent()));
    }

    private void createFlexLayoutWithFlexBasis() {
        // begin-source-example
        // source-example-heading: FlexLayout with flex basis
        FlexLayout layout = new FlexLayout();
        layout.setWidth("100%");
        layout.setHeight("50px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);
        // end-source-example

        RadioButtonGroup<String> widths = new RadioButtonGroup<>();
        widths.setItems("200px", "100%", "auto");
        widths.setValue("auto");
        widths.addValueChangeListener(
              event -> layout.setFlexBasis(event.getValue(), component1));

        layout.setId("flex-layout-with-flex-basis");

        addCard("FlexLayout", "FlexLayout with flex basis", layout, widths);
    }

    private void createFlexLayoutWithFlexDirection() {
        // begin-source-example
        // source-example-heading: FlexLayout with flex direction
        FlexLayout layout = new FlexLayout();
        layout.setWidth("100%");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);

        // end-source-example
        Consumer<FlexLayout.FlexDirection> flexDirectionConsumer = flexDirection -> layout
              .setFlexDirection(flexDirection);
        RadioButtonGroup<FlexLayout.FlexDirection> rbg = createRadioButtonGroup(
              FlexLayout.FlexDirection.values(), flexDirectionConsumer,
              FlexLayout.FlexDirection.ROW);

        layout.setId("flex-layout-with-flex-direction");

        addCard("FlexLayout", "FlexLayout with flex direction", layout, rbg);
    }

    private void createFlexLayoutWithFlexShrink() {
        // begin-source-example
        // source-example-heading: FlexLayout with flex shrink
        FlexLayout layout = new FlexLayout();
        layout.setWidth("100%");
        layout.setHeight("50px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.setFlexBasis("500px", component1, component2, component3);
        layout.add(component1, component2, component3);
        // end-source-example

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
        // begin-source-example
        // source-example-heading: FlexLayout with ordered items
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
        // end-source-example

        layout.setId("flex-layout-with-ordered-items");

        addCard("FlexLayout", "FlexLayout with ordered items", layout);
    }

    /* HorizontalLayout demos */

    private void createDefaultHorizontalLayout() {
        // begin-source-example
        // source-example-heading: Default horizontal layout
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
        // end-source-example

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
        // begin-source-example
        // source-example-heading: HorizontalLayout with justify content
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        // the default is JustifyContentMode.START
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        layout.add(component1, component2, component3);
        // end-source-example

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
        // begin-source-example
        // source-example-heading: HorizontalLayout with general alignment
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // the default is Alignment.BASELINE
        layout.setDefaultVerticalComponentAlignment(
                FlexComponent.Alignment.CENTER);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);
        // end-source-example

        component2.getElement().getStyle().set("fontSize", "24px");
        component3.getElement().getStyle().set("fontSize", "9px");

        RadioButtonGroup<FlexComponent.Alignment> alignments = new RadioButtonGroup<>();
        alignments.setItems(FlexComponent.Alignment.values());
        alignments.setRenderer(new TextRenderer<>(
                alignment -> alignment.name().toLowerCase()));
        alignments.setValue(FlexComponent.Alignment.CENTER);
        alignments.setId("horizontal-layout-alignment-radio-button");
        alignments.addValueChangeListener(event -> layout
                .setDefaultVerticalComponentAlignment(event.getValue()));

        layout.setId("layout-with-alignment");

        addCard("HorizontalLayout", "HorizontalLayout with general alignment",
                layout, alignments);
    }

    private void createHorizontalLayoutWithIndividualAlignments() {
        // begin-source-example
        // source-example-heading: HorizontalLayout with individual alignments
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.setHeight("150px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Component component1 = createComponent(1, "#78909C");
        layout.setVerticalComponentAlignment(FlexComponent.Alignment.START,
                component1);

        Component component2 = createComponent(2, "#546E7A");
        layout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER,
                component2);

        Component component3 = createComponent(3, "#37474F");
        layout.setVerticalComponentAlignment(FlexComponent.Alignment.END,
                component3);

        Component component4 = createComponent(4, "#263238");
        layout.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH,
                component4);

        layout.add(component1, component2, component3, component4);
        // end-source-example

        component1.setId("start-aligned");
        component2.setId("center-aligned");
        component3.setId("end-aligned");
        component4.setId("stretch-aligned");
        layout.setId("layout-with-individual-alignments");

        addCard("HorizontalLayout",
                "HorizontalLayout with individual alignments", layout);
    }

    private void createHorizontalLayoutWithExpandRatios() {
        // begin-source-example
        // source-example-heading: HorizontalLayout with expand ratios
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
        // end-source-example

        component1.setId("ratio-1");
        component2.setId("ratio-2");
        component3.setId("ratio-0.5");
        layout.setId("layout-with-expand-ratios");

        addCard("HorizontalLayout", "HorizontalLayout with expand ratios",
                layout);
    }

    private void createHorizontalLayoutWithCenterComponent() {
        // begin-source-example
        // source-example-heading: HorizontalLayout with component in the center
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("200px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component = createComponent(1, "#78909C");
        layout.add(component);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // end-source-example

        component.setId("center");
        layout.setId("layout-with-center");

        addCard("HorizontalLayout",
                "HorizontalLayout with component in the center", layout);
    }

    private void createHorizontalLayoutWithBoxSizing() {
        // begin-source-example
        // source-example-heading: HorizontalLayout with box-sizing settings
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
        // end-source-example

        layout.setId("horizontal-layout-with-box-sizing");

        addCard("HorizontalLayout", "HorizontalLayout with box-sizing settings",
                layout, createBoxSizingButtons(layout, layout.getId().get()));
    }

    private void createHorizontalLayoutWithExpandingContent() {
        // begin-source-example
        // source-example-heading: Horizontal layout with expanding content
        HorizontalLayout layout = new HorizontalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Div component1 = createLoremIpsum();
        Div component2 = createLoremIpsum();
        Div component3 = createLoremIpsum();

        layout.addAndExpand(component1, component2, component3);
        // end-source-example

        layout.setId("horizontal-layout-with-expanding-content");

        addCard("HorizontalLayout", "Horizontal layout with expanding content",
                layout);
    }

    /* VerticalLayout demos */

    private void createDefaultVerticalLayout() {
        // begin-source-example
        // source-example-heading: Default vertical layout
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
        // end-source-example

        layout.setId("default-layout");

        Div themeSettings = new Div(
                createToggleThemeCheckbox("padding", layout::setPadding,
                        layout.isPadding()),
                createToggleThemeCheckbox("margin", layout::setMargin,
                        layout.isMargin()),
                createToggleThemeCheckbox("spacing", layout::setSpacing,
                        layout.isSpacing()));

        addCard("VerticalLayout", "Default vertical layout", layout,
                themeSettings);
    }

    private void createVerticalLayoutWithJustifyContent() {
        // begin-source-example
        // source-example-heading: VerticalLayout with justify content
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setHeight("300px");

        // the default is JustifyContentMode.START
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);
        // end-source-example

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
        layout.setId("layout-with-justify-content");

        addCard("VerticalLayout", "VerticalLayout with justify content", layout,
                justifyContentMode);
    }

    private void createVerticalLayoutWithDefaultAlignment() {
        // begin-source-example
        // source-example-heading: VerticalLayout with general alignment
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // the default is Alignment.START
        layout.setDefaultHorizontalComponentAlignment(
                FlexComponent.Alignment.STRETCH);

        Component component1 = createComponent(1, "#78909C");
        Component component2 = createComponent(2, "#546E7A");
        Component component3 = createComponent(3, "#37474F");

        layout.add(component1, component2, component3);
        // end-source-example

        component2.getElement().setText("Component 2 with long text");
        component3.getElement().setText("C 3");

        RadioButtonGroup<FlexComponent.Alignment> alignments = new RadioButtonGroup<>();
        alignments.setItems(FlexComponent.Alignment.values());
        alignments.setRenderer(new TextRenderer<>(
                alignment -> alignment.name().toLowerCase()));
        alignments.setValue(FlexComponent.Alignment.STRETCH);
        alignments.setId("vertical-layout-alignment-radio-button");
        alignments.addValueChangeListener(event -> layout
                .setDefaultHorizontalComponentAlignment(event.getValue()));

        layout.setId("layout-with-alignment");

        addCard("VerticalLayout", "VerticalLayout with general alignment",
                layout, alignments);
    }

    private void createVerticalLayoutWithIndividualAlignments() {
        // begin-source-example
        // source-example-heading: VerticalLayout with individual alignments
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Component component1 = createComponent(1, "#78909C");
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.START,
                component1);

        Component component2 = createComponent(2, "#546E7A");
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,
                component2);

        Component component3 = createComponent(3, "#37474F");
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,
                component3);

        Component component4 = createComponent(4, "#263238");
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH,
                component4);

        layout.add(component1, component2, component3, component4);
        // end-source-example

        component1.setId("start-aligned");
        component2.setId("center-aligned");
        component3.setId("end-aligned");
        component4.setId("stretch-aligned");
        layout.setId("layout-with-individual-alignments");

        addCard("VerticalLayout", "VerticalLayout with individual alignments",
                layout);
    }

    private void createVerticalLayoutWithExpandRatios() {
        // begin-source-example
        // source-example-heading: VerticalLayout with expand ratios
        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("200px");
        layout.getStyle().set("border", "1px solid #9E9E9E");
        layout.setDefaultHorizontalComponentAlignment(
                FlexComponent.Alignment.STRETCH);

        Component component1 = createComponent(1, "#78909C");
        layout.expand(component1);

        Component component2 = createComponent(2, "#546E7A");
        layout.setFlexGrow(2, component2);

        Component component3 = createComponent(3, "#37474F");
        layout.setFlexGrow(0.5, component3);

        layout.add(component1, component2, component3);
        // end-source-example

        component1.setId("ratio-1");
        component2.setId("ratio-2");
        component3.setId("ratio-0.5");
        layout.setId("layout-with-expand-ratios");

        addCard("VerticalLayout", "VerticalLayout with expand ratios", layout);
    }

    private void createVerticalLayoutWithCenterComponent() {
        // begin-source-example
        // source-example-heading: VerticalLayout with component in the center
        VerticalLayout layout = new VerticalLayout();
        layout.setHeight("200px");
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Component component = createComponent(1, "#78909C");
        layout.add(component);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,
                component);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // end-source-example

        component.setId("center");
        layout.setId("layout-with-center");

        addCard("VerticalLayout", "VerticalLayout with component in the center",
                layout);
    }

    private void createVerticalLayoutWithBoxSizing() {
        // begin-source-example
        // source-example-heading: VerticalLayout with box-sizing settings
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
        // end-source-example

        layout.setId("vertical-layout-with-box-sizing");

        addCard("VerticalLayout", "VerticalLayout with box-sizing settings",
                layout, createBoxSizingButtons(layout, layout.getId().get()));
    }

    private void createVerticalLayoutWithExpandingContent() {
        // begin-source-example
        // source-example-heading: Vertical layout with expanding content
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");

        Div component1 = createLoremIpsum();
        Div component2 = createLoremIpsum();
        Div component3 = createLoremIpsum();

        layout.addAndExpand(component1, component2, component3);
        // end-source-example

        layout.setId("vertical-layout-with-expanding-content");

        addCard("VerticalLayout", "Vertical layout with expanding content",
                layout);
    }

    private void createScroller() {
        // begin-source-example
        // source-example-heading: Small Scroller with large content
        Scroller scroller = new Scroller();
        scroller.setHeight("100px");
        scroller.setWidth("200px");
        scroller.getStyle().set("border", "1px solid #9E9E9E");

        Div content = createLoremIpsum();

        scroller.setContent(content);
        // end-source-example

        scroller.setId("small-scroller-large-content");

        addCard("Scroller", "Small Scroller with large content", scroller);
    }

    private void createScrollerWithVerticalLayout() {
        String[] colors = new String[] { "#33C3F3", "#66D2F6", "#99E1F9",
                "#CCF0FC", "#E5F7FD", "#E5F7FD", "#CCF0FC", "#99E1F9",
                "#66D2F6", "#33C3F3" };

        // begin-source-example
        // source-example-heading: Scroller with VerticalLayout content
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
        // end-source-example

        scroller.setId("scroller-with-vertical-layout");

        addCard("Scroller", "Scroller with VerticalLayout content", scroller);
    }

    /* Override setParameter to redirect to horizontal tab */
    @Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter String parameter) {
        super.setParameter(event,
                parameter == null ? "horizontallayout" : parameter);
    }

}
