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
package com.vaadin.flow.component.checkbox;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.dom.PropertyChangeListener;

/**
 * Checkbox is an input field representing a binary choice.
 * <p>
 * Checkbox also has an indeterminate mode, see {@link #isIndeterminate()} for
 * more info.
 * <p>
 * Use {@link com.vaadin.flow.component.checkbox CheckboxGroup} to group related
 * items. Individual checkboxes should be used for options that are not related
 * to each other in any way.
 *
 * @author Vaadin Ltd
 */
public class Checkbox extends GeneratedVaadinCheckbox<Checkbox, Boolean>
        implements HasSize, HasLabel {

    private final Label labelElement;

    private static final PropertyChangeListener NO_OP = event -> {
    };

    /**
     * Default constructor.
     */
    public Checkbox() {
        // initial value, default value, accept null value
        super(false, false, false, true);
        getElement().addPropertyChangeListener("indeterminate",
                "indeterminate-changed", NO_OP);
        getElement().addPropertyChangeListener("checked", "checked-changed",
                NO_OP);
        // https://github.com/vaadin/vaadin-checkbox/issues/25
        setIndeterminate(false);

        // Initialize custom label
        labelElement = new Label();
        labelElement.getElement().setAttribute("slot", "label");
    }

    /**
     * Constructs a checkbox with the initial label text.
     *
     * @param labelText
     *            the label text to set
     * @see #setLabel(String)
     */
    public Checkbox(String labelText) {
        this();
        setLabel(labelText);
    }

    /**
     * Constructs a checkbox with the initial value.
     *
     * @param initialValue
     *            the initial value
     * @see AbstractField#setValue(Object)
     */
    public Checkbox(boolean initialValue) {
        this();
        setValue(initialValue);
    }

    /**
     * Constructs a checkbox with the initial value.
     *
     * @param labelText
     *            the label text to set
     * @param initialValue
     *            the initial value
     * @see #setLabel(String)
     * @see AbstractField#setValue(Object)
     */
    public Checkbox(String labelText, boolean initialValue) {
        this(labelText);
        setValue(initialValue);
    }

    /**
     * Constructs a checkbox with the initial label text and value change
     * listener.
     *
     * @param label
     *            the label text to set
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Checkbox(String label,
            ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a checkbox with the initial value and value change listener.
     *
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener to add
     * @see AbstractField#setValue(Object)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Checkbox(boolean initialValue,
            ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        this(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a checkbox with the initial value, label text and value change
     * listener.
     *
     * @param labelText
     *            the label text to set
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener to add
     * @see #setLabel(String)
     * @see AbstractField#setValue(Object)
     * @see #addValueChangeListener(ValueChangeListener)
     */
    public Checkbox(String labelText, boolean initialValue,
            ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
        this(labelText, initialValue);
        addValueChangeListener(listener);
    }

    /**
     * Get the current label text.
     *
     * @return the current label text
     */
    @Override
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Set the current label text of this checkbox.
     *
     * @param label
     *            the label text to set
     */
    @Override
    public void setLabel(String label) {
        if (getElement().equals(labelElement.getElement().getParent())) {
            getElement().removeChild(labelElement.getElement());
        }
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Set the current label text of this checkbox with HTML formatting.
     *
     * <p>
     * XSS vulnerability warning: the given HTML is rendered in the browser as
     * is and the developer is responsible for ensuring no harmful HTML is used.
     * </p>
     *
     * @param htmlContent
     *            the label html to set
     */
    public void setLabelAsHtml(String htmlContent) {
        setLabel("");
        labelElement.getElement().setProperty("innerHTML", htmlContent);
        getElement().appendChild(labelElement.getElement());
    }

    /**
     * Replaces the label content with the given label component.
     *
     * @param component
     *            the component to be added to the label.
     *
     * @since 23.1
     */
    public void setLabelComponent(Component component) {
        setLabel("");
        getElement().appendChild(labelElement.getElement());
        labelElement.removeAll();
        labelElement.add(component);
    }

    /**
     * Set the accessibility label of this checkbox.
     *
     * @param ariaLabel
     *            the accessibility label to set
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-label_attribute"
     *      >aria-label at MDN</a>
     */
    public void setAriaLabel(String ariaLabel) {
        getElement().setAttribute("aria-label", ariaLabel);
    }

    /**
     * Set the checkbox to be input focused when the page loads.
     *
     * @param autofocus
     *            the boolean value to set
     */
    @Override
    public void setAutofocus(boolean autofocus) {
        super.setAutofocus(autofocus);
    }

    /**
     * Get the state for the auto-focus property of the checkbox.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code autofocus} property from the checkbox
     */
    public boolean isAutofocus() {
        return isAutofocusBoolean();
    }

    /**
     * Set the indeterminate state of the checkbox.
     * <p>
     * <em>NOTE: As according to the HTML5 standard, this has only effect on the
     * visual appearance, not on the checked value!</em>
     *
     * @param indeterminate
     *            the boolean value to set
     * @see #isIndeterminate()
     */
    @Override
    public void setIndeterminate(boolean indeterminate) {
        super.setIndeterminate(indeterminate);
    }

    /**
     * Get the indeterminate state of the checkbox. The default value is
     * <code>false</code>.
     * <p>
     * An indeterminate checkbox is neither checked nor unchecked. A typical use
     * case is a “Select All” checkbox indicating that some, but not all, items
     * are selected. When the user clicks an indeterminate checkbox, it is no
     * longer indeterminate, and the <code>checked</code> value also changes.
     * <p>
     * <em>NOTE: As according to the HTML5 standard, this has only effect on the
     * visual appearance, not on the checked value!</em>
     *
     * @return the {@code indeterminate} property from the checkbox
     */
    public boolean isIndeterminate() {
        return isIndeterminateBoolean();
    }
}
