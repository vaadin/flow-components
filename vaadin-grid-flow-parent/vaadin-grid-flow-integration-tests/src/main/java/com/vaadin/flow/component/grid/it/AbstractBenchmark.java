package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

/**
 * Abstract base class for benchmark views. The base collects and gives the
 * actual implementation the requested metric and benchmark features.
 * <p>
 * </p>
 * In case metric or features is not given will show target with all features
 * and rendertime metric.
 */
public abstract class AbstractBenchmark extends Div
        implements HasUrlParameter<String> {

    @Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter String parameter) {
        var parametersMap = event.getLocation().getQueryParameters()
                .getParameters();

        if (!parametersMap.containsKey("metric")
                || !parametersMap.containsKey("features")) {
            final String routeValue = this.getClass().getAnnotation(Route.class)
                    .value();
            add(new Text(
                    "Provide query parameters: metric and features. Example: "));
            add(new Anchor(
                    "/" + routeValue + "?metric=rendertime&features=mixed",
                    routeValue + "?metric=rendertime&features=mixed",
                    AnchorTarget.PARENT));
            return;
        }

        Features features = new Features(parametersMap.containsKey("features")
                ? parametersMap.get("features").get(0).split(",")
                : new String[0]);
        initBenchmark(features);

        runMetric(parametersMap.get("metric").get(0));

    }

    /**
     * Initialize benchmark component(s).
     *
     * @param features
     *            requested features for benchmark.
     */
    protected abstract void initBenchmark(Features features);

    /**
     * Execute metric test for given metric.
     *
     * @param metric
     *            metric to run for benchmark view
     */
    protected abstract void runMetric(String metric);
}
