/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

@Route("vaadin-date-picker/injected-datepicker")
@JsModule("injected-datepicker-i18n.js")
@Tag("injected-datepicker-i18n")
public class InjectedDatePickerI18nPage extends LitTemplate {

    @Id("date-picker")
    private DatePicker datePicker;

    public InjectedDatePickerI18nPage() {
        datePicker.setI18n(TestI18N.FINNISH);
    }
}
