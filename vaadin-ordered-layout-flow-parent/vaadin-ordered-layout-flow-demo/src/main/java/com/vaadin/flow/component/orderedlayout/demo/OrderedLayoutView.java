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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        Div themeSettings = new Div(new Label(
                "Current theme supports 'padding', 'margin' and 'spacing: "),
                createToggleThemeButton(layout, "padding", layout::setPadding),
                createToggleThemeButton(layout, "margin", layout::setMargin),
                createToggleThemeButton(layout, "spacing", layout::setSpacing));

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

        Div buttons = new Div();
        buttons.add(createSpacingButton(layout, "justify-content-start-button",
                FlexComponent.JustifyContentMode.START));
        buttons.add(createSpacingButton(layout, "justify-content-end-button",
                FlexComponent.JustifyContentMode.END));
        buttons.add(
                createSpacingButton(layout, "justify-content-between-button",
                        FlexComponent.JustifyContentMode.BETWEEN));
        buttons.add(createSpacingButton(layout, "justify-content-around-button",
                FlexComponent.JustifyContentMode.AROUND));
        buttons.add(createSpacingButton(layout, "justify-content-evenly-button",
                FlexComponent.JustifyContentMode.EVENLY));

        layout.setId("layout-with-justify-content");

        addCard("HorizontalLayout", "HorizontalLayout with justify content",
                layout, buttons);
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

        Div buttons = new Div();
        buttons.add(createAlignmentButton(layout, "align-start-button",
                FlexComponent.Alignment.START));
        buttons.add(createAlignmentButton(layout, "align-end-button",
                FlexComponent.Alignment.END));
        buttons.add(createAlignmentButton(layout, "align-center-button",
                FlexComponent.Alignment.CENTER));
        buttons.add(createAlignmentButton(layout, "align-stretch-button",
                FlexComponent.Alignment.STRETCH));
        buttons.add(createAlignmentButton(layout, "align-baseline-button",
                FlexComponent.Alignment.BASELINE));

        layout.setId("layout-with-alignment");

        addCard("HorizontalLayout", "HorizontalLayout with general alignment",
                layout, buttons);
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

        addCard("HorizontalLayout", "Horizontal layout with expanding content", layout);
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

        Div themeSettings = new Div(new Label(
                "Current theme supports 'padding', 'margin' and 'spacing: "),
                createToggleThemeButton(layout, "padding", layout::setPadding),
                createToggleThemeButton(layout, "margin", layout::setMargin),
                createToggleThemeButton(layout, "spacing", layout::setSpacing));

        addCard("VerticalLayout", "Default vertical layout", layout,
                themeSettings);
    }

    private void createVerticalLayoutWithJustifyContent() {
        // begin-source-example
        // source-example-heading: VerticalLayout with justify content
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set("border", "1px solid #9E9E9E");

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

        Div buttons = new Div();
        buttons.add(createSpacingButton(layout, "justify-content-start-button",
                FlexComponent.JustifyContentMode.START));
        buttons.add(createSpacingButton(layout, "justify-content-end-button",
                FlexComponent.JustifyContentMode.END));
        buttons.add(
                createSpacingButton(layout, "justify-content-between-button",
                        FlexComponent.JustifyContentMode.BETWEEN));
        buttons.add(createSpacingButton(layout, "justify-content-around-button",
                FlexComponent.JustifyContentMode.AROUND));
        buttons.add(createSpacingButton(layout, "justify-content-evenly-button",
                FlexComponent.JustifyContentMode.EVENLY));

        layout.setId("layout-with-justify-content");

        addCard("VerticalLayout", "VerticalLayout with justify content", layout,
                buttons);
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

        Div buttons = new Div();
        buttons.add(createAlignmentButton(layout, "align-start-button",
                FlexComponent.Alignment.START));
        buttons.add(createAlignmentButton(layout, "align-end-button",
                FlexComponent.Alignment.END));
        buttons.add(createAlignmentButton(layout, "align-center-button",
                FlexComponent.Alignment.CENTER));
        buttons.add(createAlignmentButton(layout, "align-stretch-button",
                FlexComponent.Alignment.STRETCH));

        layout.setId("layout-with-alignment");

        addCard("VerticalLayout", "VerticalLayout with general alignment",
                layout, buttons);
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

        addCard("VerticalLayout", "Vertical layout with expanding content", layout);
    }

    /* Override setParameter to redirect to horizontal tab */
    @Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter String parameter) {
        super.setParameter(event,
                parameter == null ? "horizontallayout" : parameter);
    }

}
