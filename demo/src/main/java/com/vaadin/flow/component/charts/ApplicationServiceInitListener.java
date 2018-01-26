package com.vaadin.flow.component.charts;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.shared.ui.Dependency;
import com.vaadin.flow.shared.ui.LoadMode;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addDependencyFilter((dependencies, filterContext) -> {
            if (filterContext.getService().getDeploymentConfiguration()
                    .isProductionMode()) {
                dependencies.removeIf(e -> e.getType().equals(Dependency.Type.HTML_IMPORT));
                dependencies.add(new Dependency(Dependency.Type.HTML_IMPORT,
                        "src/charts-demo-app.html", LoadMode.EAGER));
            }

            return dependencies;
        });
    }
}
