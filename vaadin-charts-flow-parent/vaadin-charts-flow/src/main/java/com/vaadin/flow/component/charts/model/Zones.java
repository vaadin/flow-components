package com.vaadin.flow.component.charts.model;

import javax.annotation.Generated;
/**
 * <p>
 * An array defining zones within a series. Zones can be applied to the X axis,
 * Y axis or Z axis for bubbles, according to the <code>zoneAxis</code> option.
 * </p>
 * 
 * <p>
 * In <a
 * href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the color zones are styled with the
 * <code>.highcharts-zone-{n}</code> class, or custom classed from the
 * <code>className</code> option (<a href=
 * "http://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/css/color-zones/"
 * >view live demo</a>).
 * </p>
 */
@Generated(value = "This class is generated and shouldn't be modified", comments = "Incorrect and missing API should be reported to https://github.com/vaadin/vaadin-charts-flow/issues/new")
public class Zones extends AbstractConfigurationObject {

	private String className;
	private Number value;

	public Zones() {
	}

	/**
	 * @see #setClassName(String)
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * <a href=
	 * "http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
	 * >Styled mode</a> only. A custom class name for the zone.
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @see #setValue(Number)
	 */
	public Number getValue() {
		return value;
	}

	/**
	 * The value up to where the zone extends, if undefined the zones stretches
	 * to the last value in the series.
	 * <p>
	 * Defaults to: undefined
	 */
	public void setValue(Number value) {
		this.value = value;
	}
}