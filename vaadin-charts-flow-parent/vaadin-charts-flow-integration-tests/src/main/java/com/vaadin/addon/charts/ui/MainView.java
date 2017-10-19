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

import com.google.gson.Gson;
import com.vaadin.addon.charts.VaadinChart;
import com.vaadin.addon.charts.model.Title;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.flow.html.Div;
import com.vaadin.flow.router.HasChildView;
import com.vaadin.flow.router.View;
import com.vaadin.ui.Composite;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;

@StyleSheet("context://styles.css")
public class MainView extends Composite<Div> implements HasChildView {

	public MainView() {
		final VaadinChart chart = new VaadinChart();
		final Title title = new Title();
		title.setText("First Chart for Flow!");
		applyTitle(chart, title);
		getContent().add(chart);
	}

	private void applyTitle(VaadinChart chart, Title title) {
		final JreJsonFactory jsonFactory = new JreJsonFactory();
		final JsonObject titleNode = jsonFactory.parse((new Gson().toJson(title)));
		final JsonObject config = jsonFactory.createObject();
		config.put("title", titleNode);
		chart.getElement().callFunction("update", config);
	}

	@Override
	public void setChildView(View childView) {
	}
}
