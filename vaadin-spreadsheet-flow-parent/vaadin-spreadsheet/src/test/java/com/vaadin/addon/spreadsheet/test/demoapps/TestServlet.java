package com.vaadin.addon.spreadsheet.test.demoapps;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.vaadin.server.VaadinServlet;

@WebServlet(value = "/*", asyncSupported = true, initParams = {
        @WebInitParam(name = "heartbeatInterval", value = "10"),
        @WebInitParam(name = "UIProvider", value = "com.vaadin.addon.spreadsheet.test.demoapps.TestUIProviderImpl") })
public class TestServlet extends VaadinServlet {
}
