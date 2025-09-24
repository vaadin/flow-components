/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.vaadin.flow.component.charts.model.Title;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * Serializer for {@link Title}
 */
public class TitleBeanSerializer extends BeanSerializationDelegate<Title> {

    @Override
    public void serialize(Title bean, BeanSerializerDelegator<Title> serializer,
            JsonGenerator jgen, SerializationContext context) {
        jgen.writeStartObject();

        if (bean != null && bean.getText() == null) {
            jgen.writeNullProperty("text");
        } else {
            // write fields as per normal serialization rules
            serializer.serializeProperties(bean, jgen, context);
        }

        jgen.writeEndObject();
    }

    @Override
    public Class<Title> getBeanClass() {
        return Title.class;
    }
}
