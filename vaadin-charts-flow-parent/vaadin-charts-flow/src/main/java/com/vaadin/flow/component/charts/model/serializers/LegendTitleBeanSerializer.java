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
import com.vaadin.flow.component.charts.model.LegendTitle;

import java.io.IOException;

/**
 * Serializer for {@link com.vaadin.flow.component.charts.model.LegendTitle}.
 *
 */
public class LegendTitleBeanSerializer
        extends BeanSerializationDelegate<LegendTitle> {

    @Override
    public Class<LegendTitle> getBeanClass() {
        return LegendTitle.class;
    }

    @Override
    public void serialize(LegendTitle bean,
            BeanSerializerDelegator<LegendTitle> serializer, JsonGenerator jgen,
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
