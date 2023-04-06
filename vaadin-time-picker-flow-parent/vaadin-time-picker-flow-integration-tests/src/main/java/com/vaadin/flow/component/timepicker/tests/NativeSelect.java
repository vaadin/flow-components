
package com.vaadin.flow.component.timepicker.tests;

import java.util.List;

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;

@Tag("select")
public class NativeSelect
        extends AbstractSinglePropertyField<NativeSelect, String>
        implements HasSize, HasStyle, Focusable<NativeSelect> {

    public NativeSelect() {
        super("value", "", false);
        setSynchronizedEvent("change");
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        // Workaround for Flow setting "value" property too early before the
        // children (<option> elements) are added
        getElement().setProperty("_value", value);
    }

    public void setOptions(List<String> options) {
        getElement().removeAllChildren();
        for (String value : options) {
            Element option = new Element("option");
            option.setText(value);
            getElement().appendChild(option);
        }
        // Workaround for Flow setting "value" property too early before the
        // children (<option> elements) are added
        getElement().executeJs(
                "if (this._value !== undefined) { this.value = this._value; this._value = undefined; }");
    }
}
