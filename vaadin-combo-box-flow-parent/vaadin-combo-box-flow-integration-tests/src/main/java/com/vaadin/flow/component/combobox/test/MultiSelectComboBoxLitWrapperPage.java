package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-multi-select-combo-box/lit-wrapper")
public class MultiSelectComboBoxLitWrapperPage extends Div {
    public MultiSelectComboBoxLitWrapperPage() {
        add(new MultiSelectComboBoxLitWrapper());
    }

    @JsModule("./src/multi-select-combo-box-lit-wrapper.ts")
    @Tag("multi-select-combo-box-lit-wrapper")
    public static class MultiSelectComboBoxLitWrapper extends LitTemplate {
        @Id("combo-box")
        private MultiSelectComboBox<String> comboBox;

        public MultiSelectComboBoxLitWrapper() {
            List<String> items = IntStream.range(0, 100)
                    .mapToObj(i -> "Item " + (i + 1))
                    .collect(Collectors.toList());
            comboBox.setItems(items);
        }
    }
}
