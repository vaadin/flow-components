/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.ElementRequestHandler;
import com.vaadin.tests.MockUIExtension;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class MapSerializationTest {
    @RegisterExtension
    public final MockUIExtension ui = new MockUIExtension();

    private Map map;
    private StreamResourceRegistry streamResourceRegistryMock;
    private StreamRegistration streamRegistrationMock;

    @BeforeEach
    void setup() throws URISyntaxException {
        streamResourceRegistryMock = Mockito.mock(StreamResourceRegistry.class);
        Mockito.when(ui.getSession().getResourceRegistry())
                .thenReturn(streamResourceRegistryMock);

        streamRegistrationMock = Mockito.mock(StreamRegistration.class);
        Mockito.when(streamRegistrationMock.getResourceUri())
                .thenReturn(new URI("https://example.com"));

        Mockito.when(streamResourceRegistryMock
                .registerResource((AbstractStreamResource) Mockito.any()))
                .thenReturn(streamRegistrationMock);

        Mockito.when(streamResourceRegistryMock.registerResource(
                (ElementRequestHandler) Mockito.any(),
                Mockito.any(Element.class)))
                .thenReturn(streamRegistrationMock, streamRegistrationMock);

        map = new Map();
        // Set dummy cluster style as the default one registers a stream
        // resource for the cluster icon, which interferes with tests below
        map.getFeatureLayer().setClusterStyle(new Style());
        ui.add(map);
    }

    @Test
    void serializationSmokeTest() {
        // Configure view
        map.getView().setZoom(13);
        map.getView().setCenter(new Coordinate(42, 27));

        // Configure custom source
        OSMSource.Options options = new OSMSource.Options();
        options.setUrl("https://example.com");
        options.setOpaque(false);
        options.setCrossOrigin("custom-cors");
        options.setAttributions(List.of("Custom map service"));
        OSMSource source = new OSMSource(options);

        TileLayer layer = new TileLayer();
        layer.setSource(source);
        map.setBackgroundLayer(layer);

        ui.fakeClientCommunication();
        ArrayNode syncedItems = getSynchronizedItems();

        // Verify view
        ObjectNode viewNode = findSyncedItem(syncedItems,
                map.getView().getId());
        Assertions.assertEquals(13, viewNode.get("zoom").asInt());
        ObjectNode centerNode = (ObjectNode) viewNode.get("center");
        Assertions.assertEquals(42, centerNode.get("x").asDouble(), 0.0001);
        Assertions.assertEquals(27, centerNode.get("y").asDouble(), 0.0001);

        // Verify custom source
        ObjectNode sourceNode = findSyncedItem(syncedItems, source.getId());
        Assertions.assertEquals("https://example.com",
                sourceNode.get("url").asString());
        Assertions.assertFalse(sourceNode.get("opaque").asBoolean());
        Assertions.assertEquals("custom-cors",
                sourceNode.get("crossOrigin").asString());
        Assertions.assertTrue(sourceNode.get("attributions").isArray());
        ArrayNode attributionsNode = (ArrayNode) sourceNode.get("attributions");
        Assertions.assertEquals(1, attributionsNode.size());
        Assertions.assertEquals("Custom map service",
                attributionsNode.get(0).asString());
    }

    @Test
    void serializeIcon_registerStreamResourceExactlyOnce() {
        // Initial sync of a marker with an icon to register stream resource
        MarkerFeature marker = setupMarker();
        ui.fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.times(1))
                .registerResource(Assets.PIN.getHandler(), map.getElement());
        Mockito.clearInvocations(streamResourceRegistryMock);

        // Force another sync of the same icon
        marker.getIcon().setScale(42);
        ui.fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.never())
                .registerResource(Assets.PIN.getHandler(), map.getElement());

        // Sync a different icon with the same resource
        setupMarker();
        ui.fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.never())
                .registerResource(Assets.PIN.getHandler(), map.getElement());
    }

    @Test
    void detachMap_unregisterStreamResources() {
        // Sync a marker with an icon to register the stream resource
        setupMarker();
        ui.fakeClientCommunication();

        // Detach map
        ui.remove(map);

        Mockito.verify(streamRegistrationMock, Mockito.times(1)).unregister();
    }

    @Test
    void detachMap_reattachMap_streamResourceRegisteredAgain() {
        // Sync a marker with an icon to register the stream resource
        setupMarker();
        ui.fakeClientCommunication();

        // Detach and reattach map
        ui.remove(map);
        Mockito.clearInvocations(streamResourceRegistryMock);
        ui.add(map);
        ui.fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.times(1))
                .registerResource(Assets.PIN.getHandler(), map.getElement());
    }

    private MarkerFeature setupMarker() {
        Icon.Options options = new Icon.Options();
        options.setImg(Assets.PIN.getHandler());
        Icon icon = new Icon(options);

        MarkerFeature marker = new MarkerFeature();
        marker.setIcon(icon);
        map.getFeatureLayer().addFeature(marker);

        return marker;
    }

    private ArrayNode getSynchronizedItems() {
        var syncInvocation = ui.dumpPendingJavaScriptInvocations().stream()
                .filter(invocation -> invocation.getInvocation().getExpression()
                        .contains("$connector.synchronize"))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "No synchronize invocation found"));

        return (ArrayNode) syncInvocation.getInvocation().getParameters()
                .get(0);
    }

    private ObjectNode findSyncedItem(ArrayNode syncedItems, String id) {
        return (ObjectNode) JacksonUtils.stream(syncedItems)
                .filter(node -> node.get("id").asString().equals(id))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "No synced item with id " + id + " found"));
    }
}
