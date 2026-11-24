/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.OhlcItem;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * Custom bean serializer for {@link DataSeriesItem}
 *
 * @since 6.0.1
 */
public class DataSeriesItemBeanSerializer
        extends BeanSerializationDelegate<DataSeriesItem> {

    @Override
    public Class<DataSeriesItem> getBeanClass() {
        return DataSeriesItem.class;
    }

    @Override
    public void serialize(DataSeriesItem bean,
            BeanSerializerDelegator<DataSeriesItem> serializer,
            JsonGenerator jgen, SerializationContext context) {

        if (bean.isCustomized()) {
            jgen.writeStartObject();
            // write fields as per normal serialization rules
            serializer.serializeProperties(bean, jgen, context);
            jgen.writeEndObject();

        } else if (bean instanceof OhlcItem) {
            OhlcItem ohlcBean = (OhlcItem) bean;
            writeArray(jgen, context, ohlcBean.getX(), ohlcBean.getOpen(),
                    ohlcBean.getHigh(), ohlcBean.getLow(), ohlcBean.getClose());

        } else {
            Number x = bean.getX();
            Number y = bean.getY();
            if (x != null) {
                if (y != null) {
                    writeArray(jgen, context, x, y);
                } else if (bean.getLow() != null) {
                    writeArray(jgen, context, x, bean.getLow(), bean.getHigh());
                } else {
                    writeArray(jgen, context, x, null, null);
                }
            } else {
                // If no x set, make it like list series, just number or
                // min-max pairs
                if (y != null) {
                    context.writeValue(jgen, y);
                } else {
                    writeArray(jgen, context, bean.getLow(), bean.getHigh());
                }
            }
        }
    }

    private static void writeArray(JsonGenerator jgen,
            SerializationContext context, Number... values) {
        jgen.writeStartArray();
        for (Number value : values) {
            context.writeValue(jgen, value);
        }
        jgen.writeEndArray();
    }
}
