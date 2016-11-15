package com.vaadin.spreadsheet.charts.ui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.addon.spreadsheet.test.demoapps.SpreadsheetDemoUI;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinServlet;

@Theme("demo")
@Title("Spreadsheet-Charts Integration Demo")
@SuppressWarnings("serial")
@Widgetset("com.vaadin.addon.spreadsheet.charts.WidgetSet")
public class SpreadsheetChartsDemoUI extends SpreadsheetDemoUI {
    @WebServlet(value = {"/*","/VAADIN/*"}, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = SpreadsheetChartsDemoUI.class)
    public static class Servlet extends VaadinServlet {
    }
}
