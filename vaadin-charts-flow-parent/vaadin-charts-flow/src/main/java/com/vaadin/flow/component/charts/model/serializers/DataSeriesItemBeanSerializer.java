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
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.OhlcItem;

import java.io.IOException;

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
            JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        if (bean.isCustomized()) {
            jgen.writeStartObject();
            // write fields as per normal serialization rules
            serializer.serializeFields(bean, jgen, provider);
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
                    jgen.writeObject(y);
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
