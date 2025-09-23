/**
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.layer.TileLayer;
import com.vaadin.flow.component.map.configuration.source.OSMSource;
import com.vaadin.flow.component.map.configuration.style.Icon;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.ElementRequestHandler;

public class MapSerializationTest {

    private Map map;
    private UI ui;
    private StreamResourceRegistry streamResourceRegistryMock;
    private StreamRegistration streamRegistrationMock;

    @Before
    public void setup() throws URISyntaxException {
        ui = Mockito.spy(new UI());
        UI.setCurrent(ui);

        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        ui.getInternals().setSession(mockSession);

        streamResourceRegistryMock = Mockito.mock(StreamResourceRegistry.class);
        Mockito.when(mockSession.getResourceRegistry())
                .thenReturn(streamResourceRegistryMock);

        streamRegistrationMock = Mockito.mock(StreamRegistration.class);
        Mockito.when(streamRegistrationMock.getResourceUri())
                .thenReturn(new URI("https://example.com"));

        Mockito.when(streamResourceRegistryMock
                .registerResource((AbstractStreamResource) Mockito.any()))
                .thenReturn(streamRegistrationMock);

        Mockito.when(streamResourceRegistryMock
                .registerResource((ElementRequestHandler) Mockito.any()))
                .thenReturn(streamRegistrationMock, streamRegistrationMock);

        map = new Map();
        ui.add(map);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void serializationSmokeTest() {
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

        fakeClientCommunication();
        ArrayNode syncedItems = getSynchronizedItems();

        // Verify view
        ObjectNode viewNode = findSyncedItem(syncedItems,
                map.getView().getId());
        Assert.assertEquals(13, viewNode.get("zoom").asInt());
        ObjectNode centerNode = (ObjectNode) viewNode.get("center");
        Assert.assertEquals(42, centerNode.get("x").asDouble(), 0.0001);
        Assert.assertEquals(27, centerNode.get("y").asDouble(), 0.0001);

        // Verify custom source
        ObjectNode sourceNode = findSyncedItem(syncedItems, source.getId());
        Assert.assertEquals("https://example.com",
                sourceNode.get("url").asText());
        Assert.assertFalse(sourceNode.get("opaque").asBoolean());
        Assert.assertEquals("custom-cors",
                sourceNode.get("crossOrigin").asText());
        Assert.assertTrue(sourceNode.get("attributions").isArray());
        ArrayNode attributionsNode = (ArrayNode) sourceNode.get("attributions");
        Assert.assertEquals(1, attributionsNode.size());
        Assert.assertEquals("Custom map service",
                attributionsNode.get(0).asText());
    }

    @Test
    public void serializeIcon_registerStreamResourceExactlyOnce() {
        // Initial sync of a marker with an icon to register stream resource
        MarkerFeature marker = setupMarker();
        fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.times(1))
                .registerResource(Assets.PIN.getHandler());
        Mockito.clearInvocations(streamResourceRegistryMock);

        // Force another sync of the same icon
        marker.getIcon().setScale(42);
        fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.never())
                .registerResource(Assets.PIN.getHandler());

        // Sync a different icon with the same resource
        setupMarker();
        fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.never())
                .registerResource(Assets.PIN.getHandler());
    }

    @Test
    public void detachMap_unregisterStreamResources() {
        // Sync a marker with an icon to register the stream resource
        setupMarker();
        fakeClientCommunication();

        // Detach map
        ui.remove(map);

        Mockito.verify(streamRegistrationMock, Mockito.times(1)).unregister();
    }

    @Test
    public void detachMap_reattachMap_streamResourceRegisteredAgain() {
        // Sync a marker with an icon to register the stream resource
        setupMarker();
        fakeClientCommunication();

        // Detach and reattach map
        ui.remove(map);
        Mockito.clearInvocations(streamResourceRegistryMock);
        ui.add(map);
        fakeClientCommunication();

        Mockito.verify(streamResourceRegistryMock, Mockito.times(1))
                .registerResource(Assets.PIN.getHandler());
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
        var syncInvocation = getPendingJavaScriptInvocations().stream()
                .filter(invocation -> invocation.getInvocation().getExpression()
                        .contains("$connector.synchronize"))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "No synchronize invocation found"));

        return (ArrayNode) syncInvocation.getInvocation().getParameters()
                .get(0);
    }

    private ObjectNode findSyncedItem(ArrayNode syncedItems, String id) {
        return (ObjectNode) JacksonUtils.stream(syncedItems)
                .filter(node -> node.get("id").asText().equals(id)).findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No synced item with id " + id + " found"));
    }

    private List<PendingJavaScriptInvocation> getPendingJavaScriptInvocations() {
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    private void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }
}
