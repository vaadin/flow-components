/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vaadin.flow.component.charts.model.AxisTitle;

import java.io.IOException;

/**
 * Serializer for {@link com.vaadin.flow.component.charts.model.AxisTitle}.
 *
 */
public class AxisTitleBeanSerializer
        extends BeanSerializationDelegate<AxisTitle> {

    @Override
    public Class<AxisTitle> getBeanClass() {
        return AxisTitle.class;
    }

    @Override
    public void serialize(AxisTitle bean,
            BeanSerializerDelegator<AxisTitle> serializer, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        if (bean != null && bean.getText() == null) {
            jgen.writeNullField("text");
        } else {
            // write fields as per normal serialization rules
            serializer.serializeFields(bean, jgen, provider);
        }

        jgen.writeEndObject();
    }
}
