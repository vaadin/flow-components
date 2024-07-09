/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/helpers-view")
public class DatePickerHelpers extends Div {

    public DatePickerHelpers() {
        DatePicker datePickerHelperText = new DatePicker();
        datePickerHelperText.setHelperText("Helper text");
        datePickerHelperText.setId("data-picker-helper-text");

        NativeButton clearHelperText = new NativeButton("Clear helper text");
        clearHelperText.addClickListener(
                e -> datePickerHelperText.setHelperText(null));
        clearHelperText.setId("button-clear-text");

        DatePicker datePickerComponentHelper = new DatePicker();
        datePickerComponentHelper.setId("data-picker-helper-component");

        Span helper = new Span();
        helper.setId("helper-component");
        datePickerComponentHelper.setHelperComponent(helper);

        NativeButton clearHelperComponent = new NativeButton(
                "Clear helper component");
        clearHelperComponent.addClickListener(
                e -> datePickerComponentHelper.setHelperComponent(null));
        clearHelperComponent.setId("button-clear-component");

        add(datePickerHelperText, clearHelperText, datePickerComponentHelper,
                clearHelperComponent);
    }
}
