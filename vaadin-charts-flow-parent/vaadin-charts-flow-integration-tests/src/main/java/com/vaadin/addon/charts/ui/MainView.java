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

import java.util.Optional;

import com.vaadin.addon.charts.examples.AbstractChartExample;
import com.vaadin.addon.charts.examples.DefaultExample;
import com.vaadin.router.HasUrlParameter;
import com.vaadin.router.Route;
import com.vaadin.router.WildcardParameter;
import com.vaadin.router.event.BeforeNavigationEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.common.StyleSheet;
import com.vaadin.ui.html.Div;

@StyleSheet("context://styles.css")
@Route("")
public class MainView extends Div implements HasUrlParameter<String> {
    public static String EXAMPLE_BASE_PACKAGE = "com.vaadin.addon.charts.examples.";

    @Override
    public void setParameter(BeforeNavigationEvent event,
            @WildcardParameter String parameter) {
        removeAll();
        Optional<Component> content = getContentFromParameter(parameter);
        if (content.isPresent()) {
            add(content.get());
        } else {
            setText("couldn't find demo for url: " + parameter);
        }
    }

    /**
     * Parses route parameter to obtain the actual example to show
     * 
     * @param route
     *            path that can be either
     *            <ul>
     *            <li>category&#47demo</li>
     *            <li>fully qualified name</li>
     *            </ul>
     * 
     * @return Component for route or {@link Optional#empty()}
     */
    private Optional<Component> getContentFromParameter(String route) {
        // Empty route will show a simple chart by default otherwise parameter
        // will be converted to full qualified class and will be instantiated
        if (route == null || route.isEmpty()) {
            return Optional.of(new DefaultExample());
        }
        String className;
        if (route.startsWith(EXAMPLE_BASE_PACKAGE)) {
            className = route;
        } else {
            className = EXAMPLE_BASE_PACKAGE + route.replace("/", ".");
        }
        try {
            @SuppressWarnings("unchecked")
            Class<? extends AbstractChartExample> forName = (Class<? extends AbstractChartExample>) Class
                    .forName(className);
            return Optional.of(forName.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
