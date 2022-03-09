package com.vaadin.flow.component.charts.examples.other;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartOptions;
import com.vaadin.flow.component.charts.examples.AbstractChartExample;
import com.vaadin.flow.component.charts.examples.SkipFromDemo;
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
import com.vaadin.flow.component.charts.themes.LumoDarkTheme;
import com.vaadin.flow.component.charts.themes.LumoLightTheme;
import com.vaadin.flow.component.html.NativeButton;

@SkipFromDemo
public class GlobalOptions extends AbstractChartExample {

    @Override
    public void initDemo() {
        List<Chart> charts = new ArrayList();
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
            charts.add(chart);
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

        NativeButton changeThemeLightButton = new NativeButton();
        changeThemeLightButton.setId("change_theme_light");
        changeThemeLightButton.setText("Change theme light");
        changeThemeLightButton.addClickListener(e -> {
            ChartOptions.get().setTheme(new LumoLightTheme());
            for (Chart chart : charts) {
                chart.drawChart(true);
            }
        });
        add(changeThemeLightButton);

        NativeButton changeThemeDarkButton = new NativeButton();
        changeThemeDarkButton.setId("change_theme_dark");
        changeThemeDarkButton.setText("Change theme Dark");
        changeThemeDarkButton.addClickListener(e -> {
            ChartOptions.get().setTheme(new LumoDarkTheme());
            for (Chart chart : charts) {
                chart.drawChart(true);
            }
        });
        add(changeThemeDarkButton);
    }

}
