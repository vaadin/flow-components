package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.Level;
import com.vaadin.flow.component.charts.model.Node;
import com.vaadin.flow.component.charts.model.NodeLayout;
import com.vaadin.flow.component.charts.model.NodeSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsOrganization;
import com.vaadin.flow.component.charts.model.style.SolidColor;

public class Organization extends AbstractChartExample {

    @Override
    public void initDemo() {
        Chart chart = new Chart(ChartType.ORGANIZATION);

        Configuration conf = chart.getConfiguration();
        conf.getChart().setInverted(true);
        conf.getChart().setHeight("500px");
        conf.getTooltip().setOutside(true);
        conf.setTitle("Acme organization chart");

        NodeSeries series = createSeries();
        conf.addSeries(series);

        add(chart);
    }

    private NodeSeries createSeries() {
        NodeSeries series = new NodeSeries();
        series.setName("Acme");
        Node acme = new Node("Acme");
        Node headOffice = new Node("Head Office");
        Node labs = new Node("Labs");
        Node coyoteBuilding = new Node("Coyote Building");
        Node roadRunnerBuilding = new Node("Road Runner Building");
        Node sales = new Node("Sales");
        Node marketing = new Node("Marketing");
        Node accounting = new Node("Accounting");
        Node administration = new Node("Administration");
        Node mdsOffice = new Node("MD's Office");

        Node josephMiler = new Node("Joseph Miler");
        josephMiler.setTitle("Head of Sales");
        josephMiler.setLayout(NodeLayout.HANGING);

        Node erikPerez = new Node("Erik Perez");
        erikPerez.setTitle("Head of Marketing");
        erikPerez.setLayout(NodeLayout.HANGING);

        Node emilyFox = new Node("Emily Fox");
        emilyFox.setTitle("Head of Accounting");

        Node ewanHerbert = new Node("Ewan Herbert");
        ewanHerbert.setTitle("Head of Admin");
        ewanHerbert.setLayout(NodeLayout.HANGING);

        Node kateKirby = new Node("Kate Kirby");
        Node vaughnWhiting = new Node("Vaughn Whiting");
        Node lisaWarner = new Node("Lisa Warner");
        Node mollyDodd = new Node("Molly Dodd");
        Node natashaKelly = new Node("Natasha Kelly");

        Node managingDirector = new Node("Sally Brown", "Sally Brown",
                "Managing Director");
        managingDirector.setColor(new SolidColor("#E4B651"));

        series.add(acme, headOffice);
        series.add(acme, labs);
        series.add(headOffice, coyoteBuilding);
        series.add(headOffice, roadRunnerBuilding);
        series.add(coyoteBuilding, sales);
        series.add(coyoteBuilding, marketing);
        series.add(coyoteBuilding, accounting);
        series.add(roadRunnerBuilding, administration);
        series.add(roadRunnerBuilding, mdsOffice);
        series.add(sales, josephMiler);
        series.add(marketing, erikPerez);
        series.add(accounting, emilyFox);
        series.add(administration, ewanHerbert);
        series.add(josephMiler, kateKirby);
        series.add(josephMiler, vaughnWhiting);
        series.add(erikPerez, lisaWarner);
        series.add(ewanHerbert, mollyDodd);
        series.add(ewanHerbert, natashaKelly);
        series.add(mdsOffice, managingDirector);

        PlotOptionsOrganization plotOptions = createPlotOptions();
        series.setPlotOptions(plotOptions);

        return series;
    }

    private PlotOptionsOrganization createPlotOptions() {
        PlotOptionsOrganization plotOptions = new PlotOptionsOrganization();
        plotOptions.setColorByPoint(false);
        plotOptions.setColor(new SolidColor("#007ad0"));
        DataLabels dataLabels = new DataLabels();
        dataLabels.setColor(SolidColor.BLACK);
        plotOptions.setDataLabels(dataLabels);
        plotOptions.setBorderColor(SolidColor.WHITE);

        Level level0 = new Level();
        level0.setLevel(0);
        level0.setColor(new SolidColor("#99AED3"));

        Level level1 = new Level();
        level1.setLevel(1);
        level1.setColor(new SolidColor("#CCE6C3"));

        Level level2 = new Level();
        level2.setLevel(2);
        level2.setColor(new SolidColor("#E1F39D"));

        Level level3 = new Level();
        level3.setLevel(3);
        level3.setColor(new SolidColor("#CCE6C3"));

        Level level4 = new Level();
        level4.setLevel(4);
        level4.setColor(new SolidColor("#CABEDC"));

        Level level5 = new Level();
        level5.setLevel(5);
        level5.setColor(new SolidColor("#CABDD4"));

        plotOptions.addLevel(level0);
        plotOptions.addLevel(level1);
        plotOptions.addLevel(level2);
        plotOptions.addLevel(level3);
        plotOptions.addLevel(level4);

        return plotOptions;
    }

}
