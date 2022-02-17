package com.vaadin.flow.component.map.serialization;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.map.Assets;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MapSerializerTest {

    private UI ui;
    private StreamResourceRegistry streamResourceRegistryMock;

    @Before
    public void setup() throws URISyntaxException {
        ui = Mockito.spy(new UI());

        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        streamResourceRegistryMock = Mockito.mock(StreamResourceRegistry.class);
        StreamRegistration streamRegistrationMock = Mockito
                .mock(StreamRegistration.class);

        Mockito.when(ui.getSession()).thenReturn(mockSession);
        Mockito.when(mockSession.getResourceRegistry())
                .thenReturn(streamResourceRegistryMock);
        Mockito.when(streamResourceRegistryMock.registerResource(Mockito.any()))
                .thenReturn(streamRegistrationMock);
        Mockito.when(streamRegistrationMock.getResourceUri())
                .thenReturn(new URI("https://example.com"));
    }

    @Test
    public void serializationSmokeTest() {
        MapSerializer mapSerializer = new MapSerializer();

        JsonValue jsonValue = mapSerializer.toJson(new OSMSource(
                new OSMSource.Options().setUrl("https://example.com")
                        .setOpaque(false).setCrossOrigin("custom-cors")
                        .setAttributions(List.of("Custom map service"))));

        Assert.assertTrue("Result should be JSON object",
                jsonValue instanceof JsonObject);

        JsonObject jsonSource = (JsonObject) jsonValue;

        Assert.assertEquals("https://example.com", jsonSource.getString("url"));
        Assert.assertFalse(jsonSource.getBoolean("opaque"));
        Assert.assertEquals("custom-cors", jsonSource.getString("crossOrigin"));
        Assert.assertTrue(jsonSource.get("attributions") instanceof JsonArray);
    }

    @Test
    public void serializeStreamResource_shouldRegisterResourceExactlyOnce() {
        UI.setCurrent(ui);
        MapSerializer mapSerializer = new MapSerializer();
        Icon icon = new Icon(
                new Icon.Options().setImg(Assets.PIN.getResource()));

        mapSerializer.toJson(icon);
        mapSerializer.toJson(icon);
        mapSerializer.toJson(icon);

        Mockito.verify(streamResourceRegistryMock, Mockito.times(1))
                .registerResource(Assets.PIN.getResource());
    }
}