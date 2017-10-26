package com.vaadin.addon.charts;


import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.util.ChartSerialization;
import com.vaadin.annotations.HtmlImport;
import com.vaadin.annotations.Tag;
import com.vaadin.flow.template.PolymerTemplate;
import com.vaadin.flow.template.model.TemplateModel;
import com.vaadin.ui.AttachEvent;
import com.vaadin.ui.UI;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

@Tag("vaadin-chart")
@HtmlImport("frontend://bower_components/vaadin-charts/vaadin-chart.html")
public class VaadinChart extends PolymerTemplate<TemplateModel> {

	private Configuration configuration;
	private final JreJsonFactory jsonFactory = new JreJsonFactory();

	/**
	 * Creates a new chart with default configuration
	 */
	public VaadinChart() {
		this.configuration = new Configuration();
	}

	/**
	 * Creates a new chart with the given type
	 *
	 * @see #VaadinChart()
	 * @param type
	 */
	public VaadinChart(ChartType type) {
		this();
		getConfiguration().getChart().setType(type);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);

		final UI ui = attachEvent.getUI();

		ui.beforeClientResponse(this, () -> {
			drawChart();
		});
	}

	/**
	 * Draws a chart with the given configuration as a starting point.
	 *
	 * @see #drawChart(boolean)
	 */
	public void drawChart() {
		drawChart(false);
	}

	/**
	 * Draws a chart with the given configuration as a starting point.
	 * <p>
	 *     Note that you don't need to call this method if {@link Configuration} is
	 *     ready before element is attached.
	 * </p>
	 *
	 * @see #getConfiguration()
	 * @param resetConfiguration defines whether the chart should be redrawn or not
	 */
	public void drawChart(boolean resetConfiguration) {
		Configuration configuration = getConfiguration();

		final JsonObject configurationNode = jsonFactory.parse(ChartSerialization.toJSON(configuration));

		getElement().callFunction("update", configurationNode, resetConfiguration);
	}

	/**
	 * @return the chart configuration that is used for this chart
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
}
