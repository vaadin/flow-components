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

import com.vaadin.addon.charts.VaadinChart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.flow.router.HasChildView;
import com.vaadin.flow.router.View;
import com.vaadin.ui.Composite;
import com.vaadin.ui.common.StyleSheet;
import com.vaadin.ui.html.Div;
import com.vaadin.ui.html.NativeButton;

@StyleSheet("context://styles.css")
public class MainView extends Composite<Div> implements HasChildView {

	public MainView() {
		final VaadinChart chart = new VaadinChart();

		Configuration configuration = chart.getConfiguration();
		configuration.setTitle("First Chart for Flow");
		chart.getConfiguration().getChart().setType(ChartType.AREA);
		getContent().add(chart);

		configuration.addSeries(new ListSeries("Tokyo", 20, 12, 34, 23, 65, 8, 4, 7, 76, 19, 20, 8));
		configuration.addSeries(new ListSeries("Miami", 34, 29, 23, 65, 8, 4, 7, 7, 59, 8, 9, 19));

		XAxis x = new XAxis();
		x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
				"Sep", "Oct", "Nov", "Dec");
		configuration.addxAxis(x);

		YAxis y = new YAxis();
		y.setMin(0);
		y.setTitle("Rainfall (mm)");
		configuration.addyAxis(y);

		NativeButton changeTitleButton = new NativeButton();
		changeTitleButton.setId("change_title");
		changeTitleButton.setText("Change title");
		changeTitleButton.addClickListener(e -> {
			configuration.setTitle("First Chart for Flow - title changed");
			chart.drawChart();
		});

		getContent().add(changeTitleButton);
	}

	@Override
	public void setChildView(View childView) {
	}
}
