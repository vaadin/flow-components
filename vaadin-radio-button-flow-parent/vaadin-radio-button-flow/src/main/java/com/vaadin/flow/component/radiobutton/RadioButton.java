/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.data.binder.HasItemComponents;

/**
 * Server-side component for the {@code vaadin-radio-button} element.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-radio-button")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.4.20")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/radio-group", version = "24.4.20")
@JsModule("@vaadin/radio-group/src/vaadin-radio-button.js")
class RadioButton<T> extends Component
        implements ClickNotifier<RadioButton<T>>, Focusable<RadioButton<T>>,
        HasComponents, HasItemComponents.ItemComponent<T>, HasStyle {

    private T item;

    private final Label labelElement = appendLabelElement();

    RadioButton(String key, T item) {
        this.item = item;
        getElement().setProperty("value", key);
    }

    @Override
    public T getItem() {
        return item;
    }

    /**
     * Replaces the label content with the given label component.
     *
     * @param component
     *            the component to be added to the label.
     */
    public void setLabelComponent(Component component) {
        labelElement.removeAll();
        labelElement.add(component);
    }

    private Label appendLabelElement() {
        Label label = new Label();
        SlotUtils.addToSlot(this, "label", label);
        return label;
    }

    /**
     * True if the radio button is checked.
     *
     * @return the {@code checked} property from the webcomponent
     */
    @Synchronize(property = "checked", value = "checked-changed")
    boolean isCheckedBoolean() {
        return getElement().getProperty("checked", false);
    }

    /**
     * True if the radio button is checked.
     *
     * @param checked
     *            the boolean value to set
     */
    void setChecked(boolean checked) {
        getElement().setProperty("checked", checked);
    }

    /**
     * If true, the user cannot interact with this element.
     *
     * @param disabled
     *            the boolean value to set
     */
    void setDisabled(boolean disabled) {
        getElement().setProperty("disabled", disabled);
    }

}
