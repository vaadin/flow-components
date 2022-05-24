package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

/**
 * Language object. The language object is global and it can't be set on each
 * chart initiation. Instead, use <code>Highcharts.setOptions</code> to set it
 * before any chart is initiated.
 *
 * <pre>
 * Highcharts.setOptions({
 * 	lang: {
 * 		months: ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',  'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'],
 * 		weekdays: ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi']
 * 	}
 * });
 * </pre>
 */
public class Lang extends AbstractConfigurationObject {

    private String contextButtonTitle;
    private String decimalPoint;
    private String downloadJPEG;
    private String downloadPDF;
    private String downloadPNG;
    private String downloadSVG;
    private String drillUpText;
    private String invalidDate;
    private String loading;
    private String[] months;
    private String noData;
    private Number numericSymbolMagnitude;
    private String[] numericSymbols;
    private String printChart;
    private String resetZoom;
    private String resetZoomTitle;
    private String[] shortMonths;
    private String[] shortWeekdays;
    private String thousandsSep;
    private String[] weekdays;
    private String rangeSelectorFrom;
    private String rangeSelectorTo;
    private String rangeSelectorZoom;

    public Lang() {
    }

    /**
     * @see #setContextButtonTitle(String)
     */
    public String getContextButtonTitle() {
        return contextButtonTitle;
    }

    /**
     * Exporting module menu. The tooltip title for the context menu holding
     * print and export menu items.
     * <p>
     * Defaults to: Chart context menu
     */
    public void setContextButtonTitle(String contextButtonTitle) {
        this.contextButtonTitle = contextButtonTitle;
    }

    /**
     * @see #setDecimalPoint(String)
     */
    public String getDecimalPoint() {
        return decimalPoint;
    }

    /**
     * The default decimal point used in the
     * <code>Highcharts.numberFormat</code> method unless otherwise specified in
     * the function arguments.
     * <p>
     * Defaults to: .
     */
    public void setDecimalPoint(String decimalPoint) {
        this.decimalPoint = decimalPoint;
    }

    /**
     * @see #setDownloadJPEG(String)
     */
    public String getDownloadJPEG() {
        return downloadJPEG;
    }

    /**
     * Exporting module only. The text for the JPEG download menu item.
     * <p>
     * Defaults to: Download JPEG image
     */
    public void setDownloadJPEG(String downloadJPEG) {
        this.downloadJPEG = downloadJPEG;
    }

    /**
     * @see #setDownloadPDF(String)
     */
    public String getDownloadPDF() {
        return downloadPDF;
    }

    /**
     * Exporting module only. The text for the PDF download menu item.
     * <p>
     * Defaults to: Download PDF document
     */
    public void setDownloadPDF(String downloadPDF) {
        this.downloadPDF = downloadPDF;
    }

    /**
     * @see #setDownloadPNG(String)
     */
    public String getDownloadPNG() {
        return downloadPNG;
    }

    /**
     * Exporting module only. The text for the PNG download menu item.
     * <p>
     * Defaults to: Download PNG image
     */
    public void setDownloadPNG(String downloadPNG) {
        this.downloadPNG = downloadPNG;
    }

    /**
     * @see #setDownloadSVG(String)
     */
    public String getDownloadSVG() {
        return downloadSVG;
    }

    /**
     * Exporting module only. The text for the SVG download menu item.
     * <p>
     * Defaults to: Download SVG vector image
     */
    public void setDownloadSVG(String downloadSVG) {
        this.downloadSVG = downloadSVG;
    }

    /**
     * @see #setDrillUpText(String)
     */
    public String getDrillUpText() {
        return drillUpText;
    }

    /**
     * The text for the button that appears when drilling down, linking back to
     * the parent series. The parent series' name is inserted for
     * <code>{series.name}</code>.
     * <p>
     * Defaults to: Back to {series.name}
     */
    public void setDrillUpText(String drillUpText) {
        this.drillUpText = drillUpText;
    }

    /**
     * @see #setInvalidDate(String)
     */
    public String getInvalidDate() {
        return invalidDate;
    }

    /**
     * What to show in a date field for invalid dates. Defaults to an empty
     * string.
     */
    public void setInvalidDate(String invalidDate) {
        this.invalidDate = invalidDate;
    }

    /**
     * @see #setLoading(String)
     */
    public String getLoading() {
        return loading;
    }

    /**
     * The loading text that appears when the chart is set into the loading
     * state following a call to <code>chart.showLoading</code>.
     * <p>
     * Defaults to: Loading...
     */
    public void setLoading(String loading) {
        this.loading = loading;
    }

    /**
     * @see #setMonths(String[])
     */
    public String[] getMonths() {
        return months;
    }

    /**
     * An array containing the months names. Corresponds to the <code>%B</code>
     * format in <code>Highcharts.dateFormat()</code>.
     * <p>
     * Defaults to: [ "January" , "February" , "March" , "April" , "May" ,
     * "June" , "July" , "August" , "September" , "October" , "November" ,
     * "December"]
     */
    public void setMonths(String[] months) {
        this.months = months;
    }

    /**
     * @see #setNoData(String)
     */
    public String getNoData() {
        return noData;
    }

    /**
     * The text to display when the chart contains no data. Requires the no-data
     * module, see <a href="#noData">noData</a>.
     * <p>
     * Defaults to: No data to display
     */
    public void setNoData(String noData) {
        this.noData = noData;
    }

    /**
     * @see #setNumericSymbolMagnitude(Number)
     */
    public Number getNumericSymbolMagnitude() {
        return numericSymbolMagnitude;
    }

    /**
     * The magnitude of <a href="#lang.numericSymbol">numericSymbols</a>
     * replacements. Use 10000 for Japanese, Korean and various Chinese locales,
     * which use symbols for 10^4, 10^8 and 10^12.
     * <p>
     * Defaults to: 1000
     */
    public void setNumericSymbolMagnitude(Number numericSymbolMagnitude) {
        this.numericSymbolMagnitude = numericSymbolMagnitude;
    }

    /**
     * @see #setNumericSymbols(String[])
     */
    public String[] getNumericSymbols() {
        return numericSymbols;
    }

    /**
     * Metric prefixes used to shorten high numbers in axis labels. Setting
     * numericSymbols to null sets default values. Setting numericSymbols to an
     * empty array disables shortening - shows initial numbers.
     * <p>
     * Defaults to: [ "k" , "M" , "G" , "T" , "P" , "E"]
     */
    public void setNumericSymbols(String[] numericSymbols) {
        this.numericSymbols = numericSymbols;
    }

    /**
     * @see #setPrintChart(String)
     */
    public String getPrintChart() {
        return printChart;
    }

    /**
     * Exporting module only. The text for the menu item to print the chart.
     * <p>
     * Defaults to: Print chart
     */
    public void setPrintChart(String printChart) {
        this.printChart = printChart;
    }

    /**
     * @see #setResetZoom(String)
     */
    public String getResetZoom() {
        return resetZoom;
    }

    /**
     * The text for the label appearing when a chart is zoomed.
     * <p>
     * Defaults to: Reset zoom
     */
    public void setResetZoom(String resetZoom) {
        this.resetZoom = resetZoom;
    }

    /**
     * @see #setResetZoomTitle(String)
     */
    public String getResetZoomTitle() {
        return resetZoomTitle;
    }

    /**
     * The tooltip title for the label appearing when a chart is zoomed.
     * <p>
     * Defaults to: Reset zoom level 1:1
     */
    public void setResetZoomTitle(String resetZoomTitle) {
        this.resetZoomTitle = resetZoomTitle;
    }

    /**
     * @see #setShortMonths(String[])
     */
    public String[] getShortMonths() {
        return shortMonths;
    }

    /**
     * An array containing the months names in abbreviated form. Corresponds to
     * the <code>%b</code> format in <code>Highcharts.dateFormat()</code>.
     * <p>
     * Defaults to: [ "Jan" , "Feb" , "Mar" , "Apr" , "May" , "Jun" , "Jul" ,
     * "Aug" , "Sep" , "Oct" , "Nov" , "Dec"]
     */
    public void setShortMonths(String[] shortMonths) {
        this.shortMonths = shortMonths;
    }

    /**
     * @see #setShortWeekdays(String[])
     */
    public String[] getShortWeekdays() {
        return shortWeekdays;
    }

    /**
     * Short week days, starting Sunday. If not specified, Highcharts uses the
     * first three letters of the <code>lang.weekdays</code> option.
     */
    public void setShortWeekdays(String[] shortWeekdays) {
        this.shortWeekdays = shortWeekdays;
    }

    /**
     * @see #setThousandsSep(String)
     */
    public String getThousandsSep() {
        return thousandsSep;
    }

    /**
     * <p>
     * The default thousands separator used in the
     * <code>Highcharts.numberFormat</code> method unless otherwise specified in
     * the function arguments. Since Highcharts 4.1 it defaults to a single
     * space character, which is compatible with ISO and works across
     * Anglo-American and continental European languages.
     * </p>
     *
     * <p>
     * The default is a single space.
     * </p>
     * <p>
     * Defaults to:
     */
    public void setThousandsSep(String thousandsSep) {
        this.thousandsSep = thousandsSep;
    }

    /**
     * @see #setWeekdays(String[])
     */
    public String[] getWeekdays() {
        return weekdays;
    }

    /**
     * An array containing the weekday names.
     * <p>
     * Defaults to: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
     * "Friday", "Saturday"]
     */
    public void setWeekdays(String[] weekdays) {
        this.weekdays = weekdays;
    }

    /**
     * @see #setRangeSelectorFrom(String)
     */
    public String getRangeSelectorFrom() {
        return rangeSelectorFrom;
    }

    /**
     * The text for the label for the "from" input box in the range selector.
     * <p>
     * Defaults to: From
     */
    public void setRangeSelectorFrom(String rangeSelectorFrom) {
        this.rangeSelectorFrom = rangeSelectorFrom;
    }

    /**
     * @see #setRangeSelectorTo(String)
     */
    public String getRangeSelectorTo() {
        return rangeSelectorTo;
    }

    /**
     * The text for the label for the "to" input box in the range selector.
     * <p>
     * Defaults to: To
     */
    public void setRangeSelectorTo(String rangeSelectorTo) {
        this.rangeSelectorTo = rangeSelectorTo;
    }

    /**
     * @see #setRangeSelectorZoom(String)
     */
    public String getRangeSelectorZoom() {
        return rangeSelectorZoom;
    }

    /**
     * The text for the label for the range selector buttons.
     * <p>
     * Defaults to: Zoom
     */
    public void setRangeSelectorZoom(String rangeSelectorZoom) {
        this.rangeSelectorZoom = rangeSelectorZoom;
    }
}
