/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.examples.gantt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.GanttSeries;
import com.vaadin.flow.component.charts.model.GanttSeriesItem;
import com.vaadin.flow.component.charts.model.PlotOptionsGantt;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;

@SuppressWarnings("unused")
@SkipFromDemo
public class GanttResourceManagement extends AbstractChartExample {
    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.GANTT);

        var rentalData = getRentalData();

        final Configuration configuration = chart.getConfiguration();

        configuration.setTitle("Car Rental Schedule");

        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat(
                "<span>Rented To: {point.custom.rentedTo}</span><br/><span>From: {point.start:%e. %b}</span><br/><span>To: {point.end:%e. %b}</span>");
        configuration.setTooltip(tooltip);

        XAxis xAxis = configuration.getxAxis();
        xAxis.setCurrentDateIndicator(true);

        YAxis yAxis = configuration.getyAxis();
        yAxis.setType(AxisType.CATEGORY);
        rentalData.forEach(car -> yAxis.addCategory(car.model));

        PlotOptionsGantt plotOptionsGantt = new PlotOptionsGantt();
        configuration.setPlotOptions(plotOptionsGantt);

        for (int i = 0; i < rentalData.size(); i++) {
            CarRental car = rentalData.get(i);
            GanttSeries series = new GanttSeries();
            series.setName(car.model);
            for (CarRentalDeal deal : car.deals) {
                GanttSeriesItem item = new GanttSeriesItem(i, deal.from,
                        deal.to);
                item.setId("deal-" + i);
                item.setCustom(new TaskCustomData(deal.rentedTo));
                series.add(item);
            }
            configuration.addSeries(series);
        }

        add(chart);
    }

    private List<CarRental> getRentalData() {
        var today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return List.of(
                new CarRental("Nissan Leaf")
                        .addDeal(new CarRentalDeal("Lisa Star",
                                plusDays(today, -1), plusDays(today, 2)))
                        .addDeal(new CarRentalDeal("Shane Long",
                                plusDays(today, -3), plusDays(today, -2)))
                        .addDeal(new CarRentalDeal("Jack Coleman",
                                plusDays(today, 5), plusDays(today, 6))),
                new CarRental("Jaguar E-type")
                        .addDeal(new CarRentalDeal("Martin Hammond",
                                plusDays(today, -2), plusDays(today, 1)))
                        .addDeal(new CarRentalDeal("Linda Jackson",
                                plusDays(today, -2), plusDays(today, 1)))
                        .addDeal(new CarRentalDeal("Robert Sailor",
                                plusDays(today, 2), plusDays(today, 6))),
                new CarRental("Volvo V60")
                        .addDeal(new CarRentalDeal("Mona Ricci", today,
                                plusDays(today, 3)))
                        .addDeal(new CarRentalDeal("Jane Dockerman",
                                plusDays(today, 3), plusDays(today, 4)))
                        .addDeal(new CarRentalDeal("Bob Shurro",
                                plusDays(today, 6), plusDays(today, 8))),
                new CarRental("Volkswagen Golf")
                        .addDeal(new CarRentalDeal("Hailie Marshall",
                                plusDays(today, -1), plusDays(today, 1)))
                        .addDeal(new CarRentalDeal("Morgan Nicholson",
                                plusDays(today, -3), plusDays(today, -2)))
                        .addDeal(new CarRentalDeal("William Harriet",
                                plusDays(today, 2), plusDays(today, 3))),
                new CarRental("Peugeot 208")
                        .addDeal(new CarRentalDeal("Harry Peterson",
                                plusDays(today, -1), plusDays(today, 2)))
                        .addDeal(new CarRentalDeal("Emma Wilson",
                                plusDays(today, 3), plusDays(today, 4)))
                        .addDeal(new CarRentalDeal("Ron Donald",
                                plusDays(today, 5), plusDays(today, 6))));
    }

    private Instant plusDays(Instant date, int days) {
        return date.plus(days, ChronoUnit.DAYS);
    }

    static class CarRental {
        private final String model;
        private final List<CarRentalDeal> deals = new ArrayList<>();

        public CarRental(String model) {
            this.model = model;
        }

        public CarRental addDeal(CarRentalDeal deal) {
            deals.add(deal);
            return this;
        }
    }

    static class CarRentalDeal {
        private final String rentedTo;
        private final Instant from;
        private final Instant to;

        public CarRentalDeal(String rentedTo, Instant from, Instant to) {
            this.rentedTo = rentedTo;
            this.from = from;
            this.to = to;
        }
    }

    @SuppressWarnings("unused")
    static class TaskCustomData extends AbstractConfigurationObject {
        private String rentedTo;

        public TaskCustomData(String rentedTo) {
            this.rentedTo = rentedTo;
        }

        public String getRentedTo() {
            return rentedTo;
        }

        public void setRentedTo(String rentedTo) {
            this.rentedTo = rentedTo;
        }
    }

}
