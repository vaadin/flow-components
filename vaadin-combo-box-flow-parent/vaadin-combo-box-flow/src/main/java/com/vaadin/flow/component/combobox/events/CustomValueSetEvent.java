package com.vaadin.flow.component.combobox.events;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.combobox.ComboBoxBase;

/**
 * Event that is dispatched from a combo box component, if the component allows
 * setting custom values, and the user has entered a non-empty value that does
 * not match any of the existing items
 *
 * @param <TComponent>
 *            The specific combo box component type
 */
@DomEvent("custom-value-set")
public class CustomValueSetEvent<TComponent extends ComboBoxBase<TComponent, ?, ?>>
        extends ComponentEvent<TComponent> {
    private final String detail;

    public CustomValueSetEvent(TComponent source, boolean fromClient,
            @EventData("event.detail") String detail) {
        super(source, fromClient);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }
}
