/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.pie;

import com.vaadin.flow.component.charts.model.Accessibility;
import com.vaadin.flow.router.Route;

@Route("vaadin-charts/pie/pie-without-accessibility")
public class PieWithoutAccessibility extends PieWithLegend {

    @Override
    public void initDemo() {
        super.initDemo();
        chart.getConfiguration().setAccessibility(new Accessibility(false));
    }

}
