/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Route("vaadin-multi-select-combo-box/polymer-wrapper")
public class MultiSelectComboBoxPolymerWrapperPage extends Div {
    public MultiSelectComboBoxPolymerWrapperPage() {
        add(new MultiSelectComboBoxPolymerWrapper());
    }

    @JsModule("./src/multi-select-combo-box-polymer-wrapper.js")
    @Tag("multi-select-combo-box-polymer-wrapper")
    public static class MultiSelectComboBoxPolymerWrapper
            extends PolymerTemplate<TemplateModel> {
        @Id("combo-box")
        private MultiSelectComboBox<String> comboBox;

        public MultiSelectComboBoxPolymerWrapper() {
            List<String> items = IntStream.range(0, 100)
                    .mapToObj(i -> "Item " + (i + 1))
                    .collect(Collectors.toList());
            comboBox.setItems(items);
        }
    }
}
