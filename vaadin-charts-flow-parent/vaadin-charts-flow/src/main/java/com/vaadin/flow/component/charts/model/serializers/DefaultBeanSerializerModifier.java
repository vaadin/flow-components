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


import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.vaadin.flow.component.charts.model.AbstractSeries;
import com.vaadin.flow.component.charts.model.AxisTitle;
import com.vaadin.flow.component.charts.model.DataProviderSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.LegendTitle;
import com.vaadin.flow.component.charts.model.Title;

/**
 * Logic for altering the bean serialization process. Mainly used when
 * serialization needs to be customized with a bean serializer.
 */
public class DefaultBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config,
            BeanDescription beanDesc, JsonSerializer<?> serializer) {
        if (DataProviderSeries.class
                .isAssignableFrom(beanDesc.getBeanClass())) {
            return new BeanSerializerDelegator<>(
                    (BeanSerializerBase) serializer,
                    new DataProviderSeriesBeanSerializer());
        } else if (DataSeriesItem.class
                .isAssignableFrom(beanDesc.getBeanClass())) {
            return new BeanSerializerDelegator<>(
                    (BeanSerializerBase) serializer,
                    new DataSeriesItemBeanSerializer());
        } else if (Title.class.isAssignableFrom(beanDesc.getBeanClass())) {
            return new BeanSerializerDelegator<>(
                    (BeanSerializerBase) serializer, new TitleBeanSerializer());
        } else if (AxisTitle.class.isAssignableFrom(beanDesc.getBeanClass())) {
            return new BeanSerializerDelegator<>(
                    (BeanSerializerBase) serializer,
                    new AxisTitleBeanSerializer());
        } else if (LegendTitle.class
                .isAssignableFrom(beanDesc.getBeanClass())) {
            return new BeanSerializerDelegator<>(
                    (BeanSerializerBase) serializer,
                    new LegendTitleBeanSerializer());
        } else if (AbstractSeries.class
                .isAssignableFrom(beanDesc.getBeanClass())) {
            return new BeanSerializerDelegator<>(
                    (BeanSerializerBase) serializer,
                    new AbstractSeriesBeanSerializer());
        } else {
            return super.modifySerializer(config, beanDesc, serializer);
        }
    }
}
