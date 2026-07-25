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
package com.vaadin.flow.component.orderedlayout.it;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

/**
 * View for {@link VerticalLayout}.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ordered-layout/vertical-layout")
public class VerticalLayoutView extends AbstractLayout {

    public VerticalLayoutView() {
        createDefaultVerticalLayout();
        createVerticalLayoutWithJustifyContent();
        createVerticalLayoutWithDefaultAlignment();
        createVerticalLayoutWithIndividualAlignments();
        createVerticalLayoutWithExpandRatios();
        createVerticalLayoutWithCenterComponent();
        createVerticalLayoutWithBoxSizing();
    }

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
}
