/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Route("vaadin-date-picker/injected-datepicker")
@JsModule("injected-datepicker-i18n.js")
@Tag("injected-datepicker-i18n")
public class InjectedDatePickerI18nPage extends PolymerTemplate<TemplateModel> {

    @Id("date-picker")
    private DatePicker datePicker;

    public InjectedDatePickerI18nPage() {
        datePicker.setI18n(TestI18N.FINNISH);
    }
}
