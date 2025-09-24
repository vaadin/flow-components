/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.bean.BeanSerializerBase;

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
            SerializationContext context);
}
