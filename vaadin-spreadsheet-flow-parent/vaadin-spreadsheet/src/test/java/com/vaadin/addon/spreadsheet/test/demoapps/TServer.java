package com.vaadin.addon.spreadsheet.test.demoapps;

import java.io.File;
import java.util.Properties;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.vaadin.addon.spreadsheet.test.tb3.PrivateTB3Configuration;
import com.vaadin.server.VaadinServlet;

public class TServer {

    /**
     * 
     * Test server for the addon.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.load(TServer.class.getResourceAsStream(File.separator
                + "test.properties"));
        String port = properties
                .getProperty(PrivateTB3Configuration.PORT_PROPERTY);

        if (port == null || "".equals(port)) {
            port = PrivateTB3Configuration.DEFAULT_PORT;
        }

        startServer(Integer.parseInt(port));
    }

    public static Server startServer(int port) throws Exception {
        Server server = new Server();

        final Connector connector = new SelectChannelConnector();

        connector.setPort(port);
        server.setConnectors(new Connector[] { connector });

        WebAppContext context = new WebAppContext();
        VaadinServlet vaadinServlet = new VaadinServlet();
        ServletHolder servletHolder = new ServletHolder(vaadinServlet);
        servletHolder.setInitParameter("widgetset",
                "com.vaadin.addon.spreadsheet.Widgetset");
        servletHolder.setInitParameter("UIProvider",
                TestUIProviderImpl.class.getName());

        File file = new File("target/testwebapp");
        context.setWar(file.getPath());
        context.setContextPath("/");

        context.addServlet(servletHolder, "/*");
        server.setHandler(context);
        server.start();
        return server;
    }
}
