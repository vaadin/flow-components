package com.vaadin.flow.component.charts.events;

import com.vaadin.flow.component.charts.Chart;

public interface HasAxis<T> {
	Chart getSource();

	int getAxisIndex();

	T getAxis();
}
