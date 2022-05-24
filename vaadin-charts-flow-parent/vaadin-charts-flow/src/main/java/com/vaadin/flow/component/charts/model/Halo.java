package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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

    private Attributes attributes;
    private Number opacity;
    private Number size;

    public Halo() {
    }

    /**
     * @see #setAttributes(Attributes)
     */
    public Attributes getAttributes() {
        if (attributes == null) {
            attributes = new Attributes();
        }
        return attributes;
    }

    /**
     * A collection of SVG attributes to override the appearance of the halo,
     * for example <code>fill</code>, <code>stroke</code> and
     * <code>stroke-width</code>.
     */
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     * @see #setOpacity(Number)
     */
    public Number getOpacity() {
        return opacity;
    }

    /**
     * Opacity for the halo unless a specific fill is overridden using the
     * <code>attributes</code> setting. Note that Highcharts is only able to
     * apply opacity to colors of hex or rgb(a) formats.
     * <p>
     * Defaults to: 0.25
     */
    public void setOpacity(Number opacity) {
        this.opacity = opacity;
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
