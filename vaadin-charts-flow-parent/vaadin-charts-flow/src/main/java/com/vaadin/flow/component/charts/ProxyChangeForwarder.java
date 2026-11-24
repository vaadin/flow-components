/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.charts.events.internal.AbstractSeriesEvent;
import com.vaadin.flow.component.charts.events.internal.AxisRescaledEvent;
import com.vaadin.flow.component.charts.events.internal.ConfigurationChangeListener;
import com.vaadin.flow.component.charts.events.internal.DataAddedEvent;
import com.vaadin.flow.component.charts.events.internal.DataRemovedEvent;
import com.vaadin.flow.component.charts.events.internal.DataUpdatedEvent;
import com.vaadin.flow.component.charts.events.internal.ItemSlicedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesAddedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesChangedEvent;
import com.vaadin.flow.component.charts.events.internal.SeriesStateEvent;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.AxisDimension;
import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.component.charts.util.ChartSerialization.JsonWithFunctions;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

class ProxyChangeForwarder implements ConfigurationChangeListener {

    private static final String SERIES_FUNCTION = "__callSeriesFunction";

    private final Chart chart;

    ProxyChangeForwarder(Chart chart) {
        this.chart = chart;
    }

    @Override
    public void dataAdded(DataAddedEvent event) {
        if (event.getItem() != null) {
            callWithConfiguration(SERIES_FUNCTION, "addPoint",
                    getSeriesIndex(event), toJson(event.getItem()), true,
                    event.isShift());
        }
    }

    @Override
    public void dataRemoved(DataRemovedEvent event) {
        chart.getElement().callJsFunction("__callPointFunction", "remove",
                getSeriesIndex(event), event.getIndex());
    }

    @Override
    public void dataUpdated(DataUpdatedEvent event) {
        if (event.getValue() != null) {
            chart.getElement().callJsFunction("__callPointFunction", "update",
                    getSeriesIndex(event), event.getPointIndex(),
                    event.getValue().doubleValue());
        } else {
            callWithConfiguration("__callPointFunction", "update",
                    getSeriesIndex(event), event.getPointIndex(),
                    toJson(event.getItem()));
        }
    }

    @Override
    public void seriesStateChanged(SeriesStateEvent event) {
        if (event.isEnabled()) {
            chart.getElement().callJsFunction(SERIES_FUNCTION, "show",
                    getSeriesIndex(event));
        } else {
            chart.getElement().callJsFunction(SERIES_FUNCTION, "hide",
                    getSeriesIndex(event));
        }
    }

    @Override
    public void axisRescaled(AxisRescaledEvent event) {
        chart.getElement().callJsFunction("__callAxisFunction", "setExtremes",
                event.getAxis(), event.getAxisIndex(),
                event.getMinimum() == null ? null
                        : event.getMinimum().doubleValue(),
                event.getMaximum() == null ? null
                        : event.getMaximum().doubleValue(),
                event.isRedrawingNeeded(), event.isAnimated());
    }

    @Override
    public void itemSliced(ItemSlicedEvent event) {
        chart.getElement().callJsFunction("__callPointFunction", "slice",
                getSeriesIndex(event), event.getIndex(), event.isSliced(),
                event.isRedraw(), event.isAnimation());
    }

    @Override
    public void seriesAdded(SeriesAddedEvent event) {
        callWithConfiguration("__callChartFunction", "addSeries",
                toJson((AbstractConfigurationObject) event.getSeries()));
    }

    @Override
    public void seriesChanged(SeriesChangedEvent event) {
        callWithConfiguration(SERIES_FUNCTION, "update", getSeriesIndex(event),
                toJson((AbstractConfigurationObject) event.getSeries()));
    }

    @Override
    public void resetZoom(boolean redraw, boolean animate) {
        for (int i = 0; i < chart.getConfiguration().getNumberOfxAxes(); i++) {
            chart.getElement().callJsFunction("__callAxisFunction",
                    "setExtremes", AxisDimension.X_AXIS.getIndex(), i, null,
                    null, redraw, animate);
        }
        for (int i = 0; i < chart.getConfiguration().getNumberOfyAxes(); i++) {
            chart.getElement().callJsFunction("__callAxisFunction",
                    "setExtremes", AxisDimension.Y_AXIS.getIndex(), i, null,
                    null, redraw, animate);
        }
    }

    private int getSeriesIndex(AbstractSeriesEvent event) {
        return chart.getConfiguration().getSeries().indexOf(event.getSeries());
    }

    private static JsonWithFunctions toJson(
            AbstractConfigurationObject object) {
        return StringFunctionReporter
                .report(ChartSerialization.toJsonWithFunctions(object));
    }

    /**
     * Calls a function of the chart element, where an argument can be a
     * serialized configuration object. When the configuration objects have
     * functions, they are sent as arguments of their own and set at their paths
     * in the browser.
     */
    private void callWithConfiguration(String functionName,
            Object... arguments) {
        Object[] plainArguments = new Object[arguments.length];
        ArrayNode functionPaths = JacksonUtils.createArrayNode();
        List<ChartFunction> functions = new ArrayList<>();
        for (int index = 0; index < arguments.length; index++) {
            if (arguments[index] instanceof JsonWithFunctions json) {
                plainArguments[index] = json.json();
                for (JsonNode path : json.functionPaths()) {
                    ArrayNode argumentPath = JacksonUtils.createArrayNode()
                            .add(index);
                    argumentPath.addAll((ArrayNode) path);
                    functionPaths.add(argumentPath);
                }
                functions.addAll(json.functions());
            } else {
                plainArguments[index] = arguments[index];
            }
        }
        if (functions.isEmpty()) {
            chart.getElement().callJsFunction(functionName, plainArguments);
        } else {
            ArrayNode argumentArray = JacksonUtils.createArrayNode();
            for (Object argument : plainArguments) {
                argumentArray
                        .add(JacksonUtils.getMapper().valueToTree(argument));
            }
            chart.getElement().executeJs(ChartJs.class).callElementFunction(
                    functionName, argumentArray, functionPaths,
                    functions.toArray(ChartFunction[]::new));
        }
    }

}
