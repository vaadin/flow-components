/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2021 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

package com.vaadin.flow.component.charts.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.charts.model.AxisType;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Cursor;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.IntervalUnit;
import com.vaadin.flow.component.charts.model.Lang;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.charts.themes.LumoDarkTheme;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SVGGeneratorTest {

    private SVGGenerator svgGenerator;

    @Before
    public void setup() throws IOException {
        svgGenerator = new SVGGenerator();
    }

    @After
    public void cleanup() throws IOException {
        if (!svgGenerator.isClosed()) {
            svgGenerator.close();
        }
    }

    @Test(expected = NullPointerException.class)
    public void chartConfigurationMustNotBeNull()
            throws IOException, InterruptedException {
        svgGenerator.generate(null);
    }

    @Test(expected = IllegalStateException.class)
    public void throwIllegalStateExceptionOnClosedGenerator()
            throws IOException, InterruptedException {
        svgGenerator.close();
        // it should check to see if the generator is closed before it checks if
        // the config is null
        svgGenerator.generate(null);
    }

    @Test
    public void shouldKnowWhenItIsClosed() throws IOException {
        assertFalse(svgGenerator.isClosed());
        svgGenerator.close();
        assertTrue(svgGenerator.isClosed());
    }

    @Test
    public void generateSVGFromAnEmptyConfiguration()
            throws IOException, InterruptedException {
        Configuration configuration = new Configuration();
        String svg = svgGenerator.generate(configuration);
        Path emptyConfigChart = Paths.get("src", "test", "resources",
                "empty.svg");
        String emptyChartContent = new String(
                Files.readAllBytes(emptyConfigChart));
        assertTrue(replaceIds(emptyChartContent).contains(replaceIds(svg)));
    }

    @Test
    public void generateSVGFromValidConfiguration()
            throws IOException, InterruptedException {
        Configuration conf = createPieChartConfiguration();
        String svg = svgGenerator.generate(conf);
        Path pieChartPath = Paths.get("src", "test", "resources", "pie.svg");
        String expectedSVG = new String(Files.readAllBytes(pieChartPath));
        assertTrue(replaceIds(expectedSVG).contains(replaceIds(svg)));
    }

    @Test
    public void exportWithCustomWidth() throws IOException, InterruptedException {
        Configuration conf = createPieChartConfiguration();
        ExportOptions options = new ExportOptions();
        options.setWidth(999);
        String svg = svgGenerator.generate(conf, options);
        Path pieChartPath = Paths.get("src", "test", "resources", "custom-width.svg");
        String expectedSVG = new String(Files.readAllBytes(pieChartPath));
        assertTrue(replaceIds(expectedSVG).contains(replaceIds(svg)));
    }

    @Test
    public void exportWithCustomHeight() throws IOException, InterruptedException {
        Configuration conf = createPieChartConfiguration();
        ExportOptions options = new ExportOptions();
        options.setHeight(999);
        String svg = svgGenerator.generate(conf, options);
        Path pieChartPath = Paths.get("src", "test", "resources", "custom-height.svg");
        String expectedSVG = new String(Files.readAllBytes(pieChartPath));
        assertTrue(replaceIds(expectedSVG).contains(replaceIds(svg)));
    }

    @Test
    public void exportWithCustomTheme() throws IOException, InterruptedException {
        Configuration conf = createPieChartConfiguration();
        ExportOptions options = new ExportOptions();
        options.setTheme(new LumoDarkTheme());
        String svg = svgGenerator.generate(conf, options);
        Path pieChartPath = Paths.get("src", "test", "resources", "lumo-dark.svg");
        String expectedSVG = new String(Files.readAllBytes(pieChartPath));
        assertTrue(replaceIds(expectedSVG).contains(replaceIds(svg)));
    }

    @Test
    public void exportWithCustomLang() throws IOException, InterruptedException {
        Configuration conf = createAreaChartConfiguration();
        ExportOptions options = new ExportOptions();
        Lang lang = createLang();
        options.setLang(lang);
        String svg = svgGenerator.generate(conf, options);
        Path pieChartPath = Paths.get("src", "test", "resources", "custom-lang.svg");
        String expectedSVG = new String(Files.readAllBytes(pieChartPath));
        assertTrue(replaceIds(expectedSVG).contains(replaceIds(svg)));
    }

    private Configuration createPieChartConfiguration() {
        Configuration conf = new Configuration();
        conf.setTitle("Browser market shares in January, 2018");
        conf.getChart().setType(ChartType.PIE);

        Tooltip tooltip = new Tooltip();
        tooltip.setValueDecimals(1);
        conf.setTooltip(tooltip);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        conf.setPlotOptions(plotOptions);

        DataSeries series = new DataSeries();
        DataSeriesItem chrome = new DataSeriesItem("Chrome", 61.41);
        chrome.setSliced(true);
        chrome.setSelected(true);
        series.add(chrome);
        series.add(new DataSeriesItem("Internet Explorer", 11.84));
        series.add(new DataSeriesItem("Firefox", 10.85));
        series.add(new DataSeriesItem("Edge", 4.67));
        series.add(new DataSeriesItem("Safari", 4.18));
        series.add(new DataSeriesItem("Sogou Explorer", 1.64));
        series.add(new DataSeriesItem("Opera", 6.2));
        series.add(new DataSeriesItem("QQ", 1.2));
        series.add(new DataSeriesItem("Others", 2.61));
        conf.setSeries(series);
        return conf;
    }

    private Configuration createAreaChartConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setTitle("First Chart for Flow");
        configuration.getChart().setType(ChartType.AREA);
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
        return configuration;
    }

    private Lang createLang() {
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
        return lang;
    }

    /**
     * Generated SVG documents have some elements with an "id" attribute having
     * a value that is hard to predict and match to any specific expected value.
     * This method replaces those Ids with simple, predictable values. First one
     * will be replaced by "id-0" and all others just add 1 from the previous.
     * All mentions of the same id will also be replaced.
     *
     * @param svg
     *            the string representation of the svg with Ids to replace.
     * @return the same svg string but with all ids replaced with a predictable
     *         pattern.
     */
    private String replaceIds(String svg) {
        String regex = "id=\"[\\w-]+\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(svg);
        int index = 0;
        while (matcher.find()) {
            String group = matcher.group();
            svg = svg.replaceAll(group.substring(4, group.length() - 1),
                    "id-" + index++);
        }
        return svg;
    }
}
