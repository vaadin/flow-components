package com.vaadin.flow.component.charts;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.shared.ui.Dependency;
import com.vaadin.flow.shared.ui.LoadMode;
import org.apache.commons.io.IOUtils;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addDependencyFilter((dependencies, filterContext) -> {
            if (filterContext.getService().getDeploymentConfiguration()
                    .isProductionMode()) {

                dependencies.removeIf(e -> e.getType().equals(Dependency.Type.HTML_IMPORT)
                        && !e.getUrl().contains("examples"));

                dependencies.add(new Dependency(Dependency.Type.HTML_IMPORT,
                        "src/charts-demo-app.html", LoadMode.EAGER));
            }

            return dependencies;
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
