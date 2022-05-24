package com.vaadin.flow.component.charts.model.serializers;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.vaadin.flow.component.charts.model.DataProviderSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.vaadin.flow.component.charts.model.DataProviderSeries.CLOSE_PROPERTY;
import static com.vaadin.flow.component.charts.model.DataProviderSeries.HIGH_PROPERTY;
import static com.vaadin.flow.component.charts.model.DataProviderSeries.LOW_PROPERTY;
import static com.vaadin.flow.component.charts.model.DataProviderSeries.OPEN_PROPERTY;

/**
 * Custom bean serializer for {@link DataProviderSeries}
 */
public class DataProviderSeriesBeanSerializer
        extends BeanSerializationDelegate<DataProviderSeries> {

    public static final String xAttribute = DataProviderSeries.X_ATTRIBUTE;
    public static final String yAttribute = DataProviderSeries.Y_ATTRIBUTE;

    private enum Mode {
        ONLY_Y, XY, XLH, XOHLC, OBJECT
    }

    @Override
    public Class<DataProviderSeries> getBeanClass() {
        return DataProviderSeries.class;
    }

    @Override
    public void serialize(DataProviderSeries bean,
            BeanSerializerDelegator<DataProviderSeries> serializer,
            JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();

        if (bean.getPlotOptions() != null
                && !(bean.getPlotOptions() instanceof PlotOptionsSeries)) {
            jgen.writeObjectField("type", bean.getPlotOptions().getChartType());
        }

        // write other fields as per normal serialization rules
        serializer.serializeFields(bean, jgen, provider);

        ArrayNode data = createDataArray(bean);

        jgen.writeObjectField("data", data);

        jgen.writeEndObject();
    }

    private ArrayNode createDataArray(DataProviderSeries<?> chartDataProvider) {
        ArrayNode data = JsonNodeFactory.instance.arrayNode();
        Set<String> attributes = chartDataProvider.getChartAttributes();
        checkRequiredProperties(attributes);
        Mode mode = inferSerializationMode(attributes);

        for (final Map<String, Optional<Object>> chartAttributeToValue : chartDataProvider
                .getValues()) {
            Optional<Object> xValue = chartAttributeToValue
                    .getOrDefault(xAttribute, Optional.empty());
            Optional<Object> yValue = chartAttributeToValue
                    .getOrDefault(yAttribute, Optional.empty());
            Optional<Object> oValue = chartAttributeToValue
                    .getOrDefault(OPEN_PROPERTY, Optional.empty());
            Optional<Object> lValue = chartAttributeToValue
                    .getOrDefault(LOW_PROPERTY, Optional.empty());
            Optional<Object> hValue = chartAttributeToValue
                    .getOrDefault(HIGH_PROPERTY, Optional.empty());
            Optional<Object> cValue = chartAttributeToValue
                    .getOrDefault(CLOSE_PROPERTY, Optional.empty());

            switch (mode) {
            case ONLY_Y:
                final Optional<Object> value = chartAttributeToValue
                        .get(yAttribute);
                addValue(data, value);
                break;
            case XY:
                if (xValue.isPresent() && yValue.isPresent()) {
                    final ArrayNode entryArray = JsonNodeFactory.instance
                            .arrayNode();
                    data.add(entryArray);
                    addValue(entryArray, xValue);
                    addValue(entryArray, yValue);
                } else {
                    data.addNull();
                }
                break;
            case XLH:

                if (xValue.isPresent() && lValue.isPresent()
                        && hValue.isPresent()) {
                    final ArrayNode entryArray = JsonNodeFactory.instance
                            .arrayNode();
                    data.add(entryArray);
                    addValue(entryArray, xValue);
                    addValue(entryArray, lValue);
                    addValue(entryArray, hValue);
                } else {
                    data.addNull();
                }
                break;
            case XOHLC:

                if (xValue.isPresent() && oValue.isPresent()
                        && hValue.isPresent() && lValue.isPresent()
                        && cValue.isPresent()) {

                    final ArrayNode entryArray = JsonNodeFactory.instance
                            .arrayNode();
                    data.add(entryArray);
                    addValue(entryArray, xValue);
                    addValue(entryArray, oValue);
                    addValue(entryArray, hValue);
                    addValue(entryArray, lValue);
                    addValue(entryArray, cValue);
                } else {
                    data.addNull();
                }
                break;

            default:
                // render as json object
                final ObjectNode entryObject = JsonNodeFactory.instance
                        .objectNode();

                xValue.ifPresent(
                        o -> addNamedValue(entryObject, xAttribute, xValue));
                yValue.ifPresent(
                        o -> addNamedValue(entryObject, yAttribute, yValue));

                chartAttributeToValue.entrySet().stream()
                        .filter(e -> !e.getKey().equals(xAttribute))
                        .filter(e -> !e.getKey().equals(yAttribute))
                        .forEach(e -> addNamedValue(entryObject, e.getKey(),
                                e.getValue()));

                data.add(entryObject);

                break;
            }
        }

        return data;
    }

    private void checkRequiredProperties(Set<String> attributes) {

        Boolean hasYProperty = attributes.contains(yAttribute);
        Boolean hasHighProperty = attributes.contains(HIGH_PROPERTY);
        Boolean hasLowProperty = attributes.contains(LOW_PROPERTY);

        if (!hasYProperty && (!hasHighProperty || !hasLowProperty)) {
            throw new IllegalStateException(
                    "ChartDataSeries' must have a property for 'y' values or for "
                            + "both high and low values. Check "
                            + DataProviderSeries.class.getName() + " Javadoc");
        }
    }

    private Mode inferSerializationMode(Set<String> attributes) {
        switch (attributes.size()) {
        case 1:
            if (attributes.contains(yAttribute)) {
                return Mode.ONLY_Y;
            }
        case 2:
            if (attributes.contains(yAttribute)
                    && attributes.contains(xAttribute)) {
                return Mode.XY;
            }
        case 3:
            if (attributes.contains(xAttribute)
                    && attributes.contains(LOW_PROPERTY)
                    && attributes.contains(HIGH_PROPERTY)) {
                return Mode.XLH;
            }
        case 5:
            if (attributes.contains(xAttribute)
                    && attributes.contains(OPEN_PROPERTY)
                    && attributes.contains(HIGH_PROPERTY)
                    && attributes.contains(LOW_PROPERTY)
                    && attributes.contains(CLOSE_PROPERTY)) {
                return Mode.XOHLC;
            }
        default:
            return Mode.OBJECT;
        }
    }

    private void addValue(ArrayNode data, Optional<Object> value) {
        if (value.isPresent()) {
            ValueNode node = JsonNodeFactory.instance.pojoNode(value.get());
            data.add(node);
        }
    }

    private void addNamedValue(ObjectNode data, String name,
            Optional<Object> value) {
        if (value.isPresent()) {
            ValueNode node = JsonNodeFactory.instance.pojoNode(value.get());
            data.set(name, node);

        }
    }
}
