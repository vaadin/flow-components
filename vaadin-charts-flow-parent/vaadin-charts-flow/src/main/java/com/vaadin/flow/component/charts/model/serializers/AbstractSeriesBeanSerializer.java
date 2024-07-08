/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */

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
