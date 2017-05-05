package com.vaadin.addon.board;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.vaadin.server.VaadinServlet;

@WebServlet(value = "/*", asyncSupported = true, initParams = {
    @WebInitParam(name = "heartbeatInterval", value = "10"),
    @WebInitParam(name = "widgetset", value = "com.vaadin.addon.board.AppWidgetSet"),
    @WebInitParam(name = "UIProvider", value = "com.vaadin.addon.board.testUI.TestUIProvider") })
public class TestServlet extends VaadinServlet {
}