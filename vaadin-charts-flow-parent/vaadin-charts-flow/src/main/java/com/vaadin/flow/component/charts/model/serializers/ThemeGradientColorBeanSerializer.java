package com.vaadin.flow.component.charts.model.serializers;

/*
 * #%L
 * Vaadin Charts
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
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.vaadin.flow.component.charts.model.style.GradientColor;

import java.io.IOException;
import java.util.Set;

/**
 * Custom bean serializer for {@link GradientColor} inside {@link ChartOptions}
 *
 */
public class ThemeGradientColorBeanSerializer extends BeanSerializerBase {

    public ThemeGradientColorBeanSerializer(BeanSerializerBase source) {
        super(source);
    }

    public ThemeGradientColorBeanSerializer(BeanSerializerBase source,
            ObjectIdWriter objectIdWriter) {
        super(source, objectIdWriter);
    }

    public ThemeGradientColorBeanSerializer(BeanSerializerBase source,
            String[] toIgnore) {
        super(source, toIgnore);
    }

    public ThemeGradientColorBeanSerializer(BeanSerializerBase source,
            Object filterId) {
        super(source, null, filterId);
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(
            ObjectIdWriter objectIdWriter) {
        return new ThemeGradientColorBeanSerializer(this, objectIdWriter);
    }

    @Override
    protected BeanSerializerBase withIgnorals(Set<String> set) {
        return new ThemeGradientColorBeanSerializer(this,
                set.toArray(new String[set.size()]));
    }

    @Override
    protected BeanSerializerBase withByNameInclusion(Set<String> set,
            Set<String> set1) {
        throw new IllegalArgumentException("Method has not been implemented.");
    }

    @Override
    protected BeanSerializerBase withIgnorals(String[] toIgnore) {
        return new ThemeGradientColorBeanSerializer(this, toIgnore);
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new ThemeGradientColorBeanSerializer(this, filterId);
    }

    @Override
    protected BeanSerializerBase withProperties(
            BeanPropertyWriter[] beanPropertyWriters,
            BeanPropertyWriter[] beanPropertyWriters1) {
        throw new IllegalArgumentException("Method has not been implemented.");
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        // copied from BeanSerializer
        if ((_objectIdWriter == null) && (_anyGetterWriter == null)
                && (_propertyFilterId == null)) {
            return new BeanAsArraySerializer(this);
        }
        return this;
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen,
            SerializerProvider provider) throws IOException {

        // linearGradient: [0, 0, 250, 500],
        // stops: [
        // [0, 'rgb(48, 96, 48)'],
        // [1, 'rgb(0, 0, 0)']
        // ]

        GradientColor value = (GradientColor) bean;

        jgen.writeStartObject();

        if (value.getLinearGradient() != null) {
            ArrayNode array = JsonNodeFactory.instance.arrayNode();
            GradientColor.LinearGradient linearGradient = value
                    .getLinearGradient();
            array.addPOJO(linearGradient.getX1());
            array.addPOJO(linearGradient.getY1());
            array.addPOJO(linearGradient.getX2());
            array.addPOJO(linearGradient.getY2());
            jgen.writeObjectField("linearGradient", array);
        } else {
            GradientColor.RadialGradient radialGradient = value
                    .getRadialGradient();
            ArrayNode array = JsonNodeFactory.instance.arrayNode();
            array.addPOJO(radialGradient.getCx());
            array.addPOJO(radialGradient.getCy());
            array.addPOJO(radialGradient.getR());
            jgen.writeObjectField("radialGradient", array);
        }

        serializeFields(bean, jgen, provider);

        jgen.writeEndObject();
    }
}
