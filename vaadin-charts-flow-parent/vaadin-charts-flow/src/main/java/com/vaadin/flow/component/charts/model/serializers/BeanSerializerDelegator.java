/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import java.util.Set;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.bean.BeanAsArraySerializer;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.databind.ser.bean.UnwrappingBeanSerializer;
import tools.jackson.databind.ser.impl.ObjectIdWriter;
import tools.jackson.databind.util.NameTransformer;

/**
 * Delegates serialization calls to the given instance of
 * {@link BeanSerializationDelegate}. This class can be used instead of
 * implementing {@link BeanSerializerBase}.
 *
 * @param <T>
 */
public class BeanSerializerDelegator<T> extends BeanSerializerBase {

    private BeanSerializationDelegate<T> delegate;

    public BeanSerializerDelegator(BeanSerializerBase source) {
        super(source);
    }

    public BeanSerializerDelegator(BeanSerializerBase source,
            ObjectIdWriter objectIdWriter) {
        super(source, objectIdWriter);
    }

    public BeanSerializerDelegator(BeanSerializerBase source, Object filterId) {
        super(source, null, filterId);
    }

    public BeanSerializerDelegator(BeanSerializerBase source,
            BeanSerializationDelegate<T> delegate) {
        super(source);

        this.delegate = delegate;
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(
            ObjectIdWriter objectIdWriter) {
        return new BeanSerializerDelegator(this, objectIdWriter);
    }

    @Override
    protected BeanSerializerBase withByNameInclusion(Set<String> set,
            Set<String> set1) {
        throw new IllegalArgumentException("Method has not been implemented.");
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new BeanSerializerDelegator(this, filterId);
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
        if (canCreateArraySerializer()) {
            return BeanAsArraySerializer.construct(this);
        }
        return this;
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen,
            SerializationContext provider) {
        delegate.serialize(delegate.getBeanClass().cast(bean), this, jgen,
                provider);
    }

    @Override
    public ValueSerializer<Object> unwrappingSerializer(
            NameTransformer unwrapper) {
        // copied from BeanSerializer
        return new UnwrappingBeanSerializer(this, unwrapper);
    }

    public void serializeProperties(Object bean, JsonGenerator jgen,
            SerializationContext provider) {
        super._serializeProperties(bean, jgen, provider);
    }
}
