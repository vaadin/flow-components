package com.vaadin.addon.board.testUI;

import static com.vaadin.addon.charts.model.Compare.PERCENT;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisTitle;
import com.vaadin.addon.charts.model.Background;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.Cursor;
import com.vaadin.addon.charts.model.DataLabels;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.HorizontalAlign;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.LayoutDirection;
import com.vaadin.addon.charts.model.Legend;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.addon.charts.model.Pane;
import com.vaadin.addon.charts.model.PlotLine;
import com.vaadin.addon.charts.model.PlotOptionsBar;
import com.vaadin.addon.charts.model.PlotOptionsColumn;
import com.vaadin.addon.charts.model.PlotOptionsPie;
import com.vaadin.addon.charts.model.PlotOptionsSeries;
import com.vaadin.addon.charts.model.PlotOptionsSpline;
import com.vaadin.addon.charts.model.RangeSelector;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.VerticalAlign;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.ZoomType;
import com.vaadin.addon.charts.model.style.Color;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.ui.Component;

/**
 *
 */
public class CompatChartComponents {

  public static class BarChartUI extends CompatBasicChartUI {

    @Override
    protected Component nextChartInstance() {
        Chart chart = new Chart(ChartType.BAR);

        Configuration conf = chart.getConfiguration();

        conf.setTitle("Historic World Population by Region");
        conf.setSubTitle("Source: Wikipedia.org");

        XAxis x = new XAxis();
        x.setCategories("Africa", "America", "Asia", "Europe", "Oceania");
        x.setTitle((String) null);
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setMin(0);
        AxisTitle title = new AxisTitle("Population (millions)");
        title.setAlign(VerticalAlign.MIDDLE);
        y.setTitle(title);
        conf.addyAxis(y);

        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("this.series.name +': '+ this.y +' millions'");
        conf.setTooltip(tooltip);

        PlotOptionsBar plot = new PlotOptionsBar();
        plot.setDataLabels(new DataLabels(true));
        conf.setPlotOptions(plot);

        Legend legend = new Legend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(- 100);
        legend.setY(100);
        legend.setFloating(true);
        legend.setBorderWidth(1);
        legend.setBackgroundColor(new SolidColor("#FFFFFF"));
        legend.setShadow(true);
        conf.setLegend(legend);

        conf.disableCredits();

        List<Series> series = new ArrayList<Series>();
        series.add(new ListSeries("Year 1800", 107, 31, 635, 203, 2));
        series.add(new ListSeries("Year 1900", 133, 156, 947, 408, 6));
        series.add(new ListSeries("Year 2008", 973, 914, 4054, 732, 34));
        conf.setSeries(series);

        chart.drawChart(conf);

        return chart;

    }

  }


  //DASH-110
  public static class PieChartUI extends CompatBasicChartUI {


    @Override
    protected Component nextChartInstance() {
      Chart chart = new Chart(ChartType.PIE);

      Configuration conf = chart.getConfiguration();

      conf.setTitle("Browser market shares at a specific website, 2010");

      PlotOptionsPie plotOptions = new PlotOptionsPie();
      plotOptions.setStartAngle(45);
      plotOptions.setEndAngle(180);
      plotOptions.setCursor(Cursor.POINTER);
      DataLabels dataLabels = new DataLabels(true);
      dataLabels
          .setFormatter("'<b>'+ this.point.name +'</b>: '+ this.percentage +' %'");
      plotOptions.setDataLabels(dataLabels);
      conf.setPlotOptions(plotOptions);

      conf.setSeries(getBrowserMarketShareSeries());

      chart.drawChart();
      return chart;
    }

    private DataSeries getBrowserMarketShareSeries() {
      DataSeriesItem firefox = new DataSeriesItem("Firefox", 45.0);

      DataSeriesItem ie = new DataSeriesItem("IE", 26.8);

      DataSeriesItem chrome = new DataSeriesItem("Chrome", 12.8);
      chrome.setSliced(true);
      chrome.setSelected(true);

      DataSeriesItem safari = new DataSeriesItem("Safari", 8.5);

      DataSeriesItem opera = new DataSeriesItem("Opera", 6.2);

      DataSeriesItem others = new DataSeriesItem("Others", 0.7);

      return new DataSeries(firefox, ie, chrome, safari, opera, others);
    }

  }


  public static class CombinationsMultipleAxisUI extends CompatBasicChartUI {


    protected Component getChart() {
      Chart chart = new Chart();
      Configuration conf = chart.getConfiguration();
      Color[] colors = getThemeColors();

      conf.getChart().setZoomType(ZoomType.XY);
      conf.setTitle("Average Monthly Weather Data for Tokyo");
      conf.setSubTitle("Source: WorldClimate.com");

      XAxis x = new XAxis();
      x.setCategories("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
          "Sep", "Oct", "Nov", "Dec");
      conf.addxAxis(x);

      YAxis y1 = new YAxis();
      Labels labels = new Labels();
      labels.setFormatter("return this.value +'°C'");
      Style style = new Style();
      style.setColor(colors[1]);
      labels.setStyle(style);
      y1.setLabels(labels);
      y1.setOpposite(true);
      AxisTitle title = new AxisTitle("Temperature");
      style = new Style();
      style.setColor(colors[1]);
      y1.setTitle(title);
      conf.addyAxis(y1);

      YAxis y2 = new YAxis();
      y2.setGridLineWidth(0);
      title = new AxisTitle("Rainfall");
      style = new Style();
      style.setColor(colors[0]);
      y2.setTitle(title);
      labels = new Labels();
      labels.setFormatter("this.value +' mm'");
      style = new Style();
      style.setColor(colors[0]);
      labels.setStyle(style);
      y2.setLabels(labels);
      conf.addyAxis(y2);

      YAxis y3 = new YAxis();
      y3.setGridLineWidth(0);
      conf.addyAxis(y3);
      title = new AxisTitle("Sea-Level Pressure");
      style = new Style();
      style.setColor(colors[2]);
      y3.setTitle(title);
      labels = new Labels();
      labels.setFormatter("this.value +' mb'");
      style = new Style();
      style.setColor(colors[2]);
      labels.setStyle(style);
      y3.setLabels(labels);
      y3.setOpposite(true);
      chart.drawChart(conf);

      Tooltip tooltip = new Tooltip();
      tooltip.setFormatter("function() { "
          + "var unit = { 'Rainfall': 'mm', 'Temperature': '°C', 'Sea-Level Pressure': 'mb' }[this.series.name];"
          + "return ''+ this.x +': '+ this.y +' '+ unit; }");
      conf.setTooltip(tooltip);

      Legend legend = new Legend();
      legend.setLayout(LayoutDirection.VERTICAL);
      legend.setAlign(HorizontalAlign.LEFT);
      legend.setX(120);
      legend.setVerticalAlign(VerticalAlign.TOP);
      legend.setY(80);
      legend.setFloating(true);
      conf.setLegend(legend);

      DataSeries series = new DataSeries();
      PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
      plotOptionsColumn.setColor(colors[0]);
      series.setPlotOptions(plotOptionsColumn);
      series.setName("Rainfall");
      series.setyAxis(1);
      series.setData(49.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5,
          216.4, 194.1, 95.6, 54.4);
      conf.addSeries(series);

      series = new DataSeries();
      PlotOptionsSpline plotOptionsSpline = new PlotOptionsSpline();
      plotOptionsSpline.setColor(colors[2]);
      series.setPlotOptions(plotOptionsSpline);
      series.setName("Sea-Level Pressure");
      series.setyAxis(2);
      series.setData(1016, 1016, 1015.9, 1015.5, 1012.3, 1009.5, 1009.6,
          1010.2, 1013.1, 1016.9, 1018.2, 1016.7);
      conf.addSeries(series);

      series = new DataSeries();
      plotOptionsSpline = new PlotOptionsSpline();
      plotOptionsSpline.setColor(colors[1]);
      series.setPlotOptions(plotOptionsSpline);
      series.setName("Temperature");
      series.setData(7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3,
          13.9, 9.6);
      conf.addSeries(series);

      chart.drawChart(conf);

      return chart;
    }

    @Override
    protected Component nextChartInstance() {
      return getChart();
    }
  }

  public static class GaugeUI extends CompatBasicChartUI {
    @Override
    protected Component nextChartInstance() {
      final Chart chart = new Chart();
      chart.setWidth(500, Unit.PIXELS);

      final Configuration configuration = chart.getConfiguration();
      configuration.getChart().setType(ChartType.SOLIDGAUGE);

      configuration.getTitle().setText("Speed");

      Pane pane = new Pane();
      pane.setCenter("50%", "85%");
      pane.setSize("140%");
      pane.setStartAngle(- 90);
      pane.setEndAngle(90);
      configuration.addPane(pane);

      configuration.getTooltip().setEnabled(false);

      Background bkg = new Background();
      bkg.setBackgroundColor(new SolidColor("#eeeeee"));
      bkg.setInnerRadius("60%");
      bkg.setOuterRadius("100%");
      bkg.setShape("arc");
      bkg.setBorderWidth(0);
      pane.setBackground(bkg);

      YAxis yaxis = configuration.getyAxis();
      yaxis.setLineWidth(0);
      yaxis.setTickInterval(200);
      yaxis.setTickWidth(0);
      yaxis.setMin(0);
      yaxis.setMax(200);
      yaxis.setTitle("");
      yaxis.getTitle().setY(- 70);
      yaxis.setLabels(new Labels());
      yaxis.getLabels().setY(16);

      final ListSeries series = new ListSeries("Speed", 80);
      configuration.setSeries(series);

      chart.drawChart(configuration);
      return chart;
    }

  }


  public static class TimeLineUI extends CompatBasicChartUI {

    @Override
    protected Component nextChartInstance() {
      final Chart chart = new Chart();
      chart.setHeight("450px");
      chart.setWidth("100%");
      chart.setTimeline(true);

      Configuration configuration = chart.getConfiguration();
      configuration.getTitle().setText("AAPL Stock Price");

      YAxis yAxis = new YAxis();
      Labels label = new Labels();
      label.setFormatter("(this.value > 0 ? ' + ' : '') + this.value + '%'");
      yAxis.setLabels(label);

      PlotLine plotLine = new PlotLine();
      plotLine.setValue(2);
      plotLine.setWidth(2);
      plotLine.setColor(SolidColor.SILVER);
      yAxis.setPlotLines(plotLine);
      configuration.addyAxis(yAxis);

      Tooltip tooltip = new Tooltip();
      tooltip.setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>");
      tooltip.setValueDecimals(2);
      configuration.setTooltip(tooltip);

      DataSeries aaplSeries = new DataSeries();
      aaplSeries.setName("AAPL");
      for (StockPrices.PriceData data : StockPrices.fetchAaplPrice()) {
        DataSeriesItem item = new DataSeriesItem();
        item.setX(data.getDate());
        item.setY(data.getPrice());
        aaplSeries.add(item);
      }
      DataSeries googSeries = new DataSeries();
      googSeries.setName("GOOG");
      for (StockPrices.PriceData data : StockPrices.fetchGoogPrice()) {
        DataSeriesItem item = new DataSeriesItem();
        item.setX(data.getDate());
        item.setY(data.getPrice());
        googSeries.add(item);
      }
      DataSeries msftSeries = new DataSeries();
      msftSeries.setName("MSFT");
      for (StockPrices.PriceData data : StockPrices.fetchMsftPrice()) {
        DataSeriesItem item = new DataSeriesItem();
        item.setX(data.getDate());
        item.setY(data.getPrice());
        msftSeries.add(item);
      }
      configuration.setSeries(aaplSeries, googSeries, msftSeries);

      PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
      plotOptionsSeries.setCompare(PERCENT);
      configuration.setPlotOptions(plotOptionsSeries);

      RangeSelector rangeSelector = new RangeSelector();
      rangeSelector.setSelected(4);
      configuration.setRangeSelector(rangeSelector);

      chart.drawChart(configuration);
      return chart;
    }

    public static class StockPrices {

      protected static class TimeData {

        private long date;

        private TimeData(long date) {
          this.date = date;
        }

        public long getDate() {
          return date;
        }
      }

      public static class PriceData extends TimeData {

        private double price;

        private PriceData(long date, double price) {
          super(date);
          this.price = price;
        }

        public double getPrice() {
          return price;
        }
      }

      public static class OhlcData extends TimeData {

        private double open;
        private double high;
        private double low;
        private double close;

        private OhlcData(long date, double open, double high, double low,
                         double close) {
          super(date);
          this.open = open;
          this.high = high;
          this.low = low;
          this.close = close;
        }

        public double getOpen() {
          return open;
        }

        public double getHigh() {
          return high;
        }

        public double getLow() {
          return low;
        }

        public double getClose() {
          return close;
        }
      }


      public static class JsonData {
        private Number[][] data;

        public Number[][] getData() {
          return data;
        }

        public void setData(Number[][] data) {
          this.data = data;
        }
      }

      public static List<PriceData> fetchAaplPrice() {
        List<PriceData> data = readValueData("aapl-price.json");
        return Collections.unmodifiableList(data);
      }

      public List<PriceData> fetchAaplPriceWithTime() {
        List<PriceData> data = readValueData("aapl-price-withtime.json");
        return Collections.unmodifiableList(data);
      }

      public static List<PriceData> fetchGoogPrice() {
        List<PriceData> data = readValueData("goog-price.json");
        return Collections.unmodifiableList(data);
      }

      public static List<PriceData> fetchMsftPrice() {
        List<PriceData> data = readValueData("msft-price.json");
        return Collections.unmodifiableList(data);
      }

      public List<OhlcData> fetchAaplOhlcPrice() {
        List<OhlcData> data = readOhlcData("aapl-ohlc.json");
        return Collections.unmodifiableList(data);
      }

      private static List<PriceData> readValueData(String filename) {
        JsonData jsonData = readJsonDataFrom(filename);

        List<PriceData> data = new ArrayList<>();
        for (int i = 0; i < jsonData.data.length; ++ i) {
          Number[] row = jsonData.data[i];
          data.add(new PriceData(row[0].longValue(), row[1].doubleValue()));
        }

        return data;
      }

      private List<OhlcData> readOhlcData(String filename) {
        JsonData jsonData = readJsonDataFrom(filename);

        List<OhlcData> data = new ArrayList<>();
        for (int i = 0; i < jsonData.data.length; ++ i) {
          Number[] row = jsonData.data[i];
          data.add(new OhlcData(row[0].longValue(), row[1].doubleValue(),
              row[2].doubleValue(), row[3].doubleValue(), row[4].doubleValue()));
        }

        return data;
      }

      private static JsonData readJsonDataFrom(String filename) {
        JsonData jsonData = null;
        try {
          ObjectMapper mapper = new ObjectMapper();
          jsonData = mapper.readValue(
              new InputStreamReader(
                  CompatChartComponents.class.getResourceAsStream(
                      filename)), JsonData.class);

        } catch (IOException e) {
          throw new RuntimeException("Cannot read data from " + filename, e);
        }
        return jsonData;
      }

    }
  }

}
