package com.vaadin.flow.component.charts.examples.pie;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.html.Span;

@SkipFromDemo
public class PieWithLegendEvents extends PieWithLegend {

    private Span lastEvent;
    private Span eventDetails;

    @Override
    public void initDemo() {
        super.initDemo();
        listenerRegistration.remove();
        chart.getConfiguration().getAccessibility().setEnabled(false);
        chart.addPointLegendItemClickListener(event -> logEvent(event));
        chart.addPointClickListener(event -> logEvent(event));

        eventDetails = new Span();
        eventDetails.setId("eventDetails");

        lastEvent = new Span();
        lastEvent.setId("lastEvent");
        add(lastEvent, eventDetails);
    }

    private void logEvent(ComponentEvent<Chart> event) {
        String name = event.getClass().getSimpleName();
        String details = createEventString(event);
        lastEvent.setText(name);
        eventDetails.setText(details);
    }

}
