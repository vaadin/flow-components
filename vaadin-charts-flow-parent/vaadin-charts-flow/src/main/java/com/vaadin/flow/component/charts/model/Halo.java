/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * <p>
 * Options for the halo appearing around the hovered point in line-type series
 * as well as outside the hovered slice in pie charts. By default the halo is
 * filled by the current point or series color with an opacity of 0.25. The halo
 * can be disabled by setting the <code>halo</code> option to <code>false</code>
 * .
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the halo is styled with the <code>.highcharts-halo</code>
 * class, with colors inherited from <code>.highcharts-color-{n}</code>.
 * </p>
 */
public class Halo extends AbstractConfigurationObject {

    private Number size;

    public Halo() {
    }

    /**
     * @see #setSize(Number)
     */
    public Number getSize() {
        return size;
    }

    /**
     * The pixel size of the halo. For point markers this is the radius of the
     * halo. For pie slices it is the width of the halo outside the slice. For
     * bubbles it defaults to 5 and is the width of the halo outside the bubble.
     * <p>
     * Defaults to: 10
     */
    public void setSize(Number size) {
        this.size = size;
    }
}
