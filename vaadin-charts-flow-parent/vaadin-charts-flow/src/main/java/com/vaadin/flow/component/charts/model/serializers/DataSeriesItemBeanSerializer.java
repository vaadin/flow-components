/**
 * Copyright 2000-2025 Vaadin Ltd.
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
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * Custom bean serializer for {@link DataSeriesItem}
 *
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
            ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
            jsonArray.addPOJO(ohlcBean.getX());
            jsonArray.addPOJO(ohlcBean.getOpen());
            jsonArray.addPOJO(ohlcBean.getHigh());
            jsonArray.addPOJO(ohlcBean.getLow());
            jsonArray.addPOJO(ohlcBean.getClose());
            jgen.writeTree(jsonArray);

        } else {
            Number x = bean.getX();
            Number y = bean.getY();
            if (x != null) {
                ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
                jsonArray.addPOJO(x);
                if (y != null) {
                    jsonArray.addPOJO(y);
                } else if (bean.getLow() != null) {
                    jsonArray.addPOJO(bean.getLow());
                    jsonArray.addPOJO(bean.getHigh());
                } else {
                    jsonArray.addNull();
                    jsonArray.addNull();
                }
                jgen.writeTree(jsonArray);
            } else {
                // If no x set, make it like list series, just number or
                // min-max pairs
                if (y != null) {
                    jgen.writePOJO(y);
                } else {
                    ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
                    jsonArray.addPOJO(bean.getLow());
                    jsonArray.addPOJO(bean.getHigh());
                    jgen.writeTree(jsonArray);
                }
            }
        }
    }
}
