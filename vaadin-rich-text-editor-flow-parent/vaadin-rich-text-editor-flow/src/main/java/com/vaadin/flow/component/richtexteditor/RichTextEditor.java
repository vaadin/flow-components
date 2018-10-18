package com.vaadin.flow.component.richtexteditor;

/*
 * #%L
 * Vaadin Rich Text Editor for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Server-side component for the {@code <vaadin-rich-text-editor>} component.
 *
 * @author Vaadin Ltd
 *
 */
@Tag("vaadin-rich-text-editor")
@HtmlImport("frontend://bower_components/vaadin-rich-text-editor/src/vaadin-rich-text-editor.html")
public class RichTextEditor extends GeneratedVaadinRichTextEditor<RichTextEditor, String>
    implements HasSize, HasValueChangeMode, InputNotifier, KeyNotifier, CompositionNotifier {

    private ValueChangeMode currentMode;

    /**
     * Constructs an empty {@code RichTextEditor}.
     */
    public RichTextEditor() {
        super("", "", false);
        setValueChangeMode(ValueChangeMode.ON_CHANGE);
    }

    /**
     * Constructs a {@code RichTextEditor} with the initial value
     *
     * @param initialValue
     *            the initial value
     *
     * @see #setValue(Object)
     */
    public RichTextEditor(String initialValue) {
        this();
        setValue(initialValue);
    }

    /**
     * Constructs an empty {@code TextField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public RichTextEditor(
            ValueChangeListener<? super ComponentValueChangeEvent<RichTextEditor, String>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code RichTextEditor} with a value change
     * listener and an initial value.
     *
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener
     *
     * @see #setValue(Object)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public RichTextEditor(String initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<RichTextEditor, String>> listener) {
        this();
        setValue(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default value is {@link ValueChangeMode#ON_CHANGE}.
     */
    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        setSynchronizedEvent(
                ValueChangeMode.eventForMode(valueChangeMode, "value-changed"));
    }

    /**
     * Sets the value of this editor. If the new value is not equal to
     * {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with the editor, as long as no new converter is defined.
     *
     * @param value
     *            the new value, not {@code null}
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    /**
     * Returns the current value of the text editor. By default, the empty
     * editor will return an empty string.
     *
     * @return the current value.
     */
    @Override
    public String getValue() {
        return super.getValue();
    }


    /**
     * Value of the editor presented as HTML string.
     *
     * @return the {@code htmlValue} property from the webcomponent
     */
    public String getHtmlValue() {
        return getHtmlValueString();
    }
}
