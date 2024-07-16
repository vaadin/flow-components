/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.osgi;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright (C) 2018 - 2019 Vaadin Ltd
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
