/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.Tooltip.TooltipPosition;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.change.NodeAttachChange;
import com.vaadin.flow.internal.change.NodeChange;

public class TooltipTest {

    private TestComponent component;
    private UI ui;

    @Before
    public void setup() {
        component = new TestComponent();
        ui = new UI();
        UI.setCurrent(ui);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
    }

    @Test
    public void createTooltip_tooltipNotAttached() {
        Tooltip.forComponent(component);
        Assert.assertFalse(getTooltipElement().isPresent());
    }

    @Test
    public void createTooltip_addComponent_tooltipAttached() {
        Tooltip.forComponent(component);
        ui.add(component);
        Assert.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    public void addComponent_createTooltip_tooltipAttached() {
        ui.add(component);
        Tooltip.forComponent(component);
        Assert.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    public void createTooltip_removeComponent_tooltipNotAttached() {
        Tooltip.forComponent(component);
        ui.add(component);
        ui.remove(component);
        Assert.assertFalse(getTooltipElement().isPresent());
    }

    @Test
    public void addComponent_createTooltip_changeUI_tooltipAttached() {
        ui.add(component);
        Tooltip.forComponent(component);

        // Create a new UI and move the component to it (@PreserveOnRefresh)
        ui = new UI();
        UI.setCurrent(ui);
        component.getElement().removeFromTree(false);
        ui.add(component);

        Assert.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    public void addComponent_createTooltip_moveComponentWithinUI_tooltipNodeIdPreserved() {
        var parent1 = new Element("div");
        var parent2 = new Element("div");
        ui.getElement().appendChild(parent1, parent2);
        parent1.appendChild(component.getElement());
        Tooltip.forComponent(component);
        fakeClientCommunication();
        var nodeId = getTooltipElement().orElseThrow().getNode().getId();

        parent2.appendChild(component.getElement());
        fakeClientCommunication();

        Assert.assertEquals(nodeId,
                getTooltipElement().orElseThrow().getNode().getId());
    }

    @Test
    public void addComponent_createTooltip_resync_tooltipNodeAttached() {
        ui.add(component);
        Tooltip.forComponent(component);
        fakeClientCommunication();

        var stateTree = ui.getInternals().getStateTree();
        stateTree.prepareForResync();
        var tooltipNode = getTooltipElement().orElseThrow().getNode();
        var changes = new ArrayList<NodeChange>();
        stateTree.collectChanges(changes::add);

        Assert.assertTrue(changes.stream()
                .anyMatch(change -> change instanceof NodeAttachChange
                        && change.getNode() == tooltipNode));
    }

    @Test
    public void createTooltip_setText() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setText("foo");
        ui.add(component);
        Assert.assertEquals("foo",
                getTooltipElement().get().getProperty("text"));
        Assert.assertEquals("foo", tooltip.getText());
    }

    @Test
    public void createTooltip_setFocusDelay() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setFocusDelay(1000);
        ui.add(component);
        Assert.assertEquals(1000,
                getTooltipElement().get().getProperty("focusDelay", 0));
        Assert.assertEquals(1000, tooltip.getFocusDelay());
    }

    @Test
    public void createTooltip_setHideDelay() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setHideDelay(1000);
        ui.add(component);
        Assert.assertEquals(1000,
                getTooltipElement().get().getProperty("hideDelay", 0));
        Assert.assertEquals(1000, tooltip.getHideDelay());
    }

    @Test
    public void createTooltip_setHoverDelay() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setHoverDelay(1000);
        ui.add(component);
        Assert.assertEquals(1000,
                getTooltipElement().get().getProperty("hoverDelay", 0));
        Assert.assertEquals(1000, tooltip.getHoverDelay());
    }

    @Test
    public void createTooltip_setPosition() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setPosition(TooltipPosition.END);
        ui.add(component);
        Assert.assertEquals("end",
                getTooltipElement().get().getProperty("position"));
        Assert.assertEquals(TooltipPosition.END, tooltip.getPosition());
    }

    @Test
    public void createTooltip_defaultPosition() {
        var tooltip = Tooltip.forComponent(component);
        Assert.assertEquals(null, tooltip.getPosition());
    }

    @Test
    public void createTooltip_setManual() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setManual(true);
        ui.add(component);
        Assert.assertEquals(true,
                getTooltipElement().get().getProperty("manual", false));
        Assert.assertEquals(true, tooltip.isManual());
    }

    @Test
    public void createTooltip_setOpened() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setOpened(true);
        ui.add(component);
        Assert.assertEquals(true,
                getTooltipElement().get().getProperty("opened", false));
        Assert.assertEquals(true, tooltip.isOpened());
    }

    @Test
    public void tooltipForCompopnentTwice_sameReference() {
        var tooltip = Tooltip.forComponent(component);
        var tooltip2 = Tooltip.forComponent(component);
        Assert.assertSame(tooltip, tooltip2);
    }

    @Test
    public void createTooltip_fluentAPI() {
        ui.add(component);

        var tooltip = Tooltip.forComponent(component).withText("foo")
                .withFocusDelay(200).withHideDelay(1000).withHoverDelay(500)
                .withPosition(TooltipPosition.BOTTOM_END).withManual(true);

        tooltip.setOpened(true);

        Assert.assertEquals("foo",
                getTooltipElement().get().getProperty("text"));
        Assert.assertEquals(200,
                getTooltipElement().get().getProperty("focusDelay", 0));
        Assert.assertEquals(1000,
                getTooltipElement().get().getProperty("hideDelay", 0));
        Assert.assertEquals(500,
                getTooltipElement().get().getProperty("hoverDelay", 0));
        Assert.assertEquals("bottom-end",
                getTooltipElement().get().getProperty("position"));
        Assert.assertEquals(true,
                getTooltipElement().get().getProperty("manual", false));
    }

    private void fakeClientCommunication() {
        // Collect the pending changes as a client round trip would do, which
        // marks the attached nodes as sent to the client
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private Optional<Element> getTooltipElement() {
        return ui.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"))
                .findFirst();
    }

    @Tag("test")
    private static class TestComponent extends Component {
    }
}
