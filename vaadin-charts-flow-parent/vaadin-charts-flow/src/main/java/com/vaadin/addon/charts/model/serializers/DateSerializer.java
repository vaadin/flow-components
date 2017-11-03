package com.vaadin.addon.charts.model.serializers;

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

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vaadin.addon.charts.util.Util;

/**
 * Serializes all {@link Date} objects as UTC long.
 */
public class DateSerializer extends JsonSerializer<Date> {

    public static Module getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer());

        return module;
    }

    @Override
    public void serialize(Date value, JsonGenerator gen,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        final Instant instantUTC = value.toInstant(); // converting to UTC
        gen.writeNumber(Util.toHighchartsTS(instantUTC));
    }

}
