package com.vaadin.flow.component.crud;

import com.vaadin.flow.internal.JsonSerializer;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.vaadin.flow.internal.JsonUtils.jsonEquals;

public class CrudI18nTest {

    @Test
    public void createDefaultAndRender() throws IOException {
        JsonValue reference = new JreJsonFactory()
                .parse(IOUtils.toString(getClass().getResource("/i18n.json"), "UTF-8"));

        JsonValue generated = JsonSerializer.toJson(CrudI18n.createDefault());

        Assert.assertTrue(jsonEquals(reference, generated));
    }
}
