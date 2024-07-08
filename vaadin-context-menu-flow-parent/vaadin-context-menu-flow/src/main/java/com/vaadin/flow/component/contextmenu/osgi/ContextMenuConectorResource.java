/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.osgi;

import java.io.Serializable;

import org.osgi.service.component.annotations.Component;

import com.vaadin.flow.osgi.support.OsgiVaadinStaticResource;

/**
 *
 * Context menu connector resource registration.
 *
 * @author Vaadin Ltd
 *
 */
@Component(immediate = true, service = OsgiVaadinStaticResource.class)
public class ContextMenuConectorResource
        implements OsgiVaadinStaticResource, Serializable {

    @Override
    public String getPath() {
        return "/META-INF/resources/frontend/contextMenuConnector.js";
    }

    @Override
    public String getAlias() {
        return "/frontend/contextMenuConnector.js";
    }

}
