/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */
package com.vaadin.flow.component.charts.ui;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.area.AreaChart;
import com.vaadin.flow.component.charts.examples.dynamic.ServerSideEvents;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;

@Route("vaadin-charts")
@StyleSheet("context://styles.css")
@Uses(ServerSideEvents.class)
public class MainView extends Div implements HasUrlParameter<String> {

    public static String EXAMPLE_BASE_PACKAGE = "com.vaadin.flow.component.charts.examples.";

    @Override
    public void setParameter(BeforeEvent event,
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
            return Optional.of(new AreaChart());
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
