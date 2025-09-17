/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.PlotOptionsSeries;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

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
            JsonGenerator jgen, SerializationContext context) {
        AbstractSeries series = bean;

        jgen.writeStartObject();

        // write other fields as per normal serialization rules
        serializer.serializeProperties(bean, jgen, context);

        if (series.getPlotOptions() != null
                && !(bean.getPlotOptions() instanceof PlotOptionsSeries)) {
            jgen.writePOJOProperty("type",
                    series.getPlotOptions().getChartType());
        }

        jgen.writeEndObject();
    }
}
