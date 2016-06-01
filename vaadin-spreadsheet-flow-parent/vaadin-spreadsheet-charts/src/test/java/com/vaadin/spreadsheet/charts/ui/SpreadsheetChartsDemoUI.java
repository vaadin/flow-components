package com.vaadin.spreadsheet.charts.ui;

import com.vaadin.addon.spreadsheet.test.demoapps.SpreadsheetDemoUI;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import javax.servlet.annotation.WebServlet;

@Theme("demo")
@Title("Spreadsheet-Charts Integration Demo")
@SuppressWarnings("serial")
public class SpreadsheetChartsDemoUI extends SpreadsheetDemoUI {
    @WebServlet(value = {"/*","/VAADIN/*"}, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = SpreadsheetChartsDemoUI.class, widgetset = "com.vaadin.addon.spreadsheet.charts.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }
}
