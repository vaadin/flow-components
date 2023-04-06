/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;

import java.io.IOException;

/**
 * Custom bean serializer for {@link AbstractSeries} that adds the type field.
 * We need to use a bean serializer so that annotations work.
 *
 */
public class AbstractSeriesBeanSerializer
        extends BeanSerializationDelegate<AbstractSeries> {

    @Override
    public Class<AbstractSeries> getBeanClass() {
        return AbstractSeries.class;
    }

    @Override
    public void serialize(AbstractSeries bean,
            BeanSerializerDelegator<AbstractSeries> serializer,
            JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        AbstractSeries series = bean;

        jgen.writeStartObject();

        // write other fields as per normal serialization rules
        serializer.serializeFields(bean, jgen, provider);

        if (series.getPlotOptions() != null
                && !(bean.getPlotOptions() instanceof PlotOptionsSeries)) {
            jgen.writeObjectField("type",
                    series.getPlotOptions().getChartType());
        }

        jgen.writeEndObject();
    }
}
