package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
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

/**
 * Options for the hovered series
 */
public class Hover extends AbstractConfigurationObject {

    private Boolean animation;
    private Boolean enabled;
    private Halo halo;
    private Number radius;
    private Number radiusPlus;
    private Marker marker;

    public Hover() {
    }

    /**
     * @see #setAnimation(Boolean)
     */
    public Boolean getAnimation() {
        return animation;
    }

    /**
     * Animation setting for hovering the graph in line-type series.
     * <p>
     * Defaults to: { "duration": 50 }
     */
    public void setAnimation(Boolean animation) {
        this.animation = animation;
    }

    public Hover(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable separate styles for the hovered series to visualize that the user
     * hovers either the series itself or the legend. .
     * <p>
     * Defaults to: true
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setHalo(Halo)
     */
    public Halo getHalo() {
        if (halo == null) {
            halo = new Halo();
        }
        return halo;
    }

    /**
     * <p>
     * Options for the halo appearing around the hovered point in line-type
     * series as well as outside the hovered slice in pie charts. By default the
     * halo is filled by the current point or series color with an opacity of
     * 0.25. The halo can be disabled by setting the <code>halo</code> option to
     * <code>false</code>.
     * </p>
     *
     * <p>
     * In <a href=
     * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
     * >styled mode</a>, the halo is styled with the
     * <code>.highcharts-halo</code> class, with colors inherited from
     * <code>.highcharts-color-{n}</code>.
     * </p>
     */
    public void setHalo(Halo halo) {
        this.halo = halo;
    }

    /**
     * @see #setRadius(Number)
     */
    public Number getRadius() {
        return radius;
    }

    /**
     * The radius of the point marker. In hover state, it defaults to the normal
     * state's radius + 2 as per the <a href=
     * "#plotOptions.series.marker.states.hover.radiusPlus">radiusPlus</a>
     * option.
     */
    public void setRadius(Number radius) {
        this.radius = radius;
    }

    /**
     * @see #setRadiusPlus(Number)
     */
    public Number getRadiusPlus() {
        return radiusPlus;
    }

    /**
     * The number of pixels to increase the radius of the hovered point.
     * <p>
     * Defaults to: 2
     */
    public void setRadiusPlus(Number radiusPlus) {
        this.radiusPlus = radiusPlus;
    }

    /**
     * @see #setMarker(Marker)
     */
    public Marker getMarker() {
        if (marker == null) {
            marker = new Marker();
        }
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
