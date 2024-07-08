/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.osgi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.vaadin.flow.osgi.support.OsgiVaadinContributor;
import com.vaadin.flow.osgi.support.OsgiVaadinStaticResource;

/**
 * Grid connector and selection column resources registration.
 *
 * @author Vaadin Ltd
 *
 */
@Component(immediate = true, service = OsgiVaadinContributor.class)
public class GridContributor implements OsgiVaadinContributor, Serializable {

    @Override
    public List<OsgiVaadinStaticResource> getContributions() {
        return Arrays.asList(
                OsgiVaadinStaticResource.create(
                        "/META-INF/resources/frontend/gridConnector.js",
                        "/frontend/gridConnector.js"),
                OsgiVaadinStaticResource.create(
                        "/META-INF/resources/frontend/vaadin-grid-flow-selection-column.html",
                        "/frontend/vaadin-grid-flow-selection-column.html"));
    }

}
