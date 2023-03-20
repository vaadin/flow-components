/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.checkbox;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.dom.PropertyChangeListener;

/**
 * Server-side component for the {@code vaadin-checkbox} element.
 * <p>
 * Checkbox is a value component that can be checked or unchecked. The default
 * value is unchecked.
 * <p>
 * Checkbox also has a indeterminate mode, see {@link #isIndeterminate()} for
 * more info.
 *
 * @author Vaadin Ltd
 */
public class Checkbox extends GeneratedVaadinCheckbox<Checkbox, Boolean>
        implements HasSize {

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
     * Get the current label text.
     *
     * @return the current label text
     */
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Set the current label text of this checkbox.
     *
     * @param label
     *            the label text to set
     */
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
