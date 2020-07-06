package com.vaadin.flow.component.charts;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Element;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addBootstrapListener((response) -> {
            Element head = response.getDocument().head();
            head.append(
                    "<meta name=\"viewport\" content=\"width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes\">"
                +   "<meta name=\"theme-color\" content=\"#00B4F0\">");
        });

        event.addRequestHandler(((session, request, response) -> {
            final String requestPath = request.getPathInfo();
            final String examplesBase = "/examples";
            if (requestPath.contains(examplesBase)) {
                response.setContentType("text/html");
                response.getWriter().write(IOUtils.toString(getClass().getResourceAsStream(
                        requestPath.substring(requestPath.indexOf(examplesBase)))));
                return true;
            }
            return false;
        }));
    }
}
