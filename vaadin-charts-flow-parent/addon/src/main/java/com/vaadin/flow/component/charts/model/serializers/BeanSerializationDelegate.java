package com.vaadin.flow.component.charts.model.serializers;

/*
 * #%L
 * Vaadin Charts
 * %%
 * Copyright (C) 2014 Vaadin Ltd
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
