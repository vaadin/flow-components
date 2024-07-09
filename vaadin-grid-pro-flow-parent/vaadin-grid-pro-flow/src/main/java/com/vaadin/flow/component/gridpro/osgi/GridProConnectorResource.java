/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.osgi;

import com.vaadin.flow.osgi.support.OsgiVaadinStaticResource;
import org.osgi.service.component.annotations.Component;

import java.io.Serializable;

/**
 *
 * Grid pro connector resource registration.
 *
 * @author Vaadin Ltd
 *
 */
@Component(immediate = true, service = OsgiVaadinStaticResource.class)
public class GridProConnectorResource
        implements OsgiVaadinStaticResource, Serializable {
    @Override
    public String getPath() {
        return "/META-INF/resources/frontend/gridProConnector.js";
    }

    @Override
    public String getAlias() {
        return "/frontend/gridProConnector.js";
    }
}
