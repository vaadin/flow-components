/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datepicker.osgi;

import java.io.Serializable;

import org.osgi.service.component.annotations.Component;

import com.vaadin.flow.osgi.support.OsgiVaadinStaticResource;

/**
 * Date picker connector resource registration.
 *
 * @author Vaadin Ltd
 *
 */
@Component(immediate = true, service = OsgiVaadinStaticResource.class)
public class DatePickerResource
        implements OsgiVaadinStaticResource, Serializable {

    @Override
    public String getPath() {
        return "/META-INF/resources/frontend/datepickerConnector.js";
    }

    @Override
    public String getAlias() {
        return "/frontend/datepickerConnector.js";
    }

}
