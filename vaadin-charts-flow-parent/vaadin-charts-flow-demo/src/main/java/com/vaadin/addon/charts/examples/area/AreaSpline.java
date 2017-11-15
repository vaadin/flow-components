package com.vaadin.addon.charts.examples.area;

import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.PlotBand;
import com.vaadin.addon.charts.model.PlotOptionsArea;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;

public class AreaSpline extends AbstractChartExample {

	private static final SolidColor LIGHT_BLUE = new SolidColor(68, 170, 213, .2);

	@Override
	public void initDemo() {
		Chart chart = new Chart(ChartType.AREASPLINE);

		Configuration conf = chart.getConfiguration();

		conf.setTitle(new Title("Average fruit consumption during one week"));

		Legend legend = new Legend();
		legend.setLayout(LayoutDirection.VERTICAL);
		legend.setAlign(HorizontalAlign.LEFT);
		legend.setFloating(true);
		legend.setVerticalAlign(VerticalAlign.TOP);
		legend.setX(150);
		legend.setY(100);
		conf.setLegend(legend);

		XAxis xAxis = new XAxis();
		xAxis.setCategories(new String[] { "Monday", "Tuesday", "Wednesday",
				"Thursday", "Friday", "Saturday", "Sunday" });
		// add blue background for the weekend
		PlotBand plotBand = new PlotBand(4.5, 6.5, LIGHT_BLUE);
		plotBand.setZIndex(1);
		xAxis.setPlotBands(plotBand);
		conf.addxAxis(xAxis);

		YAxis yAxis = new YAxis();
		yAxis.setTitle(new AxisTitle("Fruit units"));
		conf.addyAxis(yAxis);

		Tooltip tooltip = new Tooltip();
		// Customize tooltip formatting
		tooltip.setHeaderFormat("");
		tooltip.setPointFormat("{series.name}: {point.y} units");
		conf.setTooltip(tooltip);

		PlotOptionsArea plotOptions = new PlotOptionsArea();
		plotOptions.setFillOpacity(0.5);
		conf.setPlotOptions(plotOptions);

		ListSeries o = new ListSeries("John", 3, 4, 3, 5, 4, 10);
		// You can also add values separately
		o.addData(12);
		conf.addSeries(o);
		conf.addSeries(new ListSeries("Jane", 1, 3, 4, 3, 3, 5, 4));

		add(chart);
	}
}
