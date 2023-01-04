/**
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.IOException;

/**
 * Abstract class for bean serializers. It is called by
 * {@link BeanSerializerDelegator}. This class can be used instead of
 * implementing {@link BeanSerializerBase}.
 *
 * @param <T>
 */
public abstract class BeanSerializationDelegate<T> {
    public abstract Class<T> getBeanClass();

    public abstract void serialize(T bean,
            BeanSerializerDelegator<T> serializer, JsonGenerator jgen,
            SerializerProvider provider) throws IOException;
}
