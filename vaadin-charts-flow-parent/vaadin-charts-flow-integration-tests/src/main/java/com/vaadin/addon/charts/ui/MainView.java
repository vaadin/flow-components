/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.addon.charts.ui;

import com.vaadin.addon.charts.VaadinChart;

import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.flow.html.Div;
import com.vaadin.flow.html.NativeButton;
import com.vaadin.flow.router.HasChildView;
import com.vaadin.flow.router.View;
import com.vaadin.ui.Composite;

@StyleSheet("context://styles.css")
public class MainView extends Composite<Div> implements HasChildView {

	public MainView() {
		final VaadinChart chart = new VaadinChart();

		Configuration configuration = chart.getConfiguration();
		configuration.setTitle("First Chart for Flow");
		chart.getConfiguration().getChart().setType(ChartType.AREA);
		getContent().add(chart);

		configuration.addSeries(new ListSeries("Tokyo", 20, 12,34,23,65,8,4,7,76, 19, 20,8));
		configuration.addSeries(new ListSeries("Miami", 34, 29,23,65,8,4,7,7, 59,8, 9, 19));

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
