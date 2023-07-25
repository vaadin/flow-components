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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.IOException;
import java.util.Set;

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

    public BeanSerializerDelegator(BeanSerializerBase source,
            String[] toIgnore) {
        super(source, toIgnore);
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
    protected BeanSerializerBase withIgnorals(Set<String> toIgnore) {
        return new BeanSerializerDelegator(this, toIgnore);
    }

    @Override
    protected BeanSerializerBase withByNameInclusion(Set<String> set, Set<String> set1) {
        return null;
    }

    @Override
    public BeanSerializerBase withFilterId(Object filterId) {
        return new BeanSerializerDelegator(this, filterId);
    }

    @Override
    protected BeanSerializerBase withProperties(BeanPropertyWriter[] beanPropertyWriters, BeanPropertyWriter[] beanPropertyWriters1) {
        return null;
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
        delegate.serialize(delegate.getBeanClass().cast(bean), this, jgen,
                provider);
    }

    @Override
    public void serializeFields(Object bean, JsonGenerator jgen,
            SerializerProvider provider)
            throws IOException, JsonGenerationException {
        super.serializeFields(bean, jgen, provider);
    }
}
