/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */
package com.vaadin.addon.charts.ui;

import com.vaadin.addon.charts.ui.Servlet.MyRouterConfigurator;
import com.vaadin.flow.router.RouterConfiguration;
import com.vaadin.flow.router.RouterConfigurator;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletConfiguration;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/*", name = "UIServlet", asyncSupported = true)
@VaadinServletConfiguration(routerConfigurator = MyRouterConfigurator.class, productionMode = false)
public class Servlet extends VaadinServlet {

    /**
     * The router configurator defines the how to map URLs to views.
     */
    public static class MyRouterConfigurator implements RouterConfigurator {
        @Override
        public void configure(RouterConfiguration configuration) {
            /*
             * For the root, only show the main view without any sub view
             */
            configuration.setRoute("", MainView.class);
        }
    }
}
