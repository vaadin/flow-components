package com.vaadin.flow.component.charts.examples.other;

import com.vaadin.flow.component.charts.AbstractChartExample;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartOptions;
import com.vaadin.flow.component.charts.SkipFromDemo;
import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.IntervalUnit;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.html.NativeButton;

@SkipFromDemo
public class GlobalOptions extends AbstractChartExample {

    @Override
    public void initDemo() {
        NativeButton changeTitleButton = new NativeButton();
        changeTitleButton.setId("add_chart");
        changeTitleButton.setText("Add chart");
        changeTitleButton.addClickListener(e -> {
            final Chart chart = new Chart();

            Configuration configuration = chart.getConfiguration();
            configuration.setTitle("First Chart for Flow");
            chart.getConfiguration().getChart().setType(ChartType.AREA);
            Tooltip tooltip = configuration.getTooltip();
            tooltip.setEnabled(true);
            tooltip.setShared(true);

            PlotOptionsSeries options = new PlotOptionsSeries();
            options.setPointStart(0);
            options.setPointIntervalUnit(IntervalUnit.DAY);
            configuration.setPlotOptions(options);
            configuration.addSeries(new ListSeries("Tokyo", 20, 12, 34, 23, 65,
                    8, 4, 7, 76, 19, 20, 8));
            configuration.addSeries(new ListSeries("Miami", 34, 29, 23, 65, 8,
                    4, 7, 7, 59, 8, 9, 19));

            XAxis x = new XAxis();
            x.setType(AxisType.DATETIME);
            x.getLabels().setFormat("{value:%a}");
            configuration.addxAxis(x);
            YAxis y = new YAxis();
            y.setMin(0);
            y.setTitle("Rainfall (mm)");
            configuration.addyAxis(y);
            add(chart);
        });
        add(changeTitleButton);

        NativeButton changeLangButton = new NativeButton();
        changeLangButton.setId("change_lang");
        changeLangButton.setText("Change lang");
        changeLangButton.addClickListener(e -> {
            Lang lang = new Lang();
            lang.setShortMonths(new String[] { "Tammi", "Helmi", "Maalis",
                    "Huhti", "Touko", "Kesä", "Heinä", "Elo", "Syys", "Loka",
                    "Marras", "Joulu" });
            lang.setMonths(new String[] { "Tammikuu", "Helmikuu", "Maaliskuu",
                    "Huhtikuu", "Toukokuu", "Kesäkuu", "Heinäkuu", "Elokuu",
                    "Syyskuu", "Lokakuu", "Marraskuu", "Joulukuu" });
            lang.setWeekdays(new String[] { "Sunnuntai", "Maanantai", "Tiistai",
                    "Keskiviikko", "Torstai", "Perjantai", "Lauantai" });
            lang.setShortWeekdays(
                    new String[] { "su", "ma", "ti", "ke", "to", "pe", "la" });
            ChartOptions.get().setLang(lang);
        });
        add(changeLangButton);
    }

}
