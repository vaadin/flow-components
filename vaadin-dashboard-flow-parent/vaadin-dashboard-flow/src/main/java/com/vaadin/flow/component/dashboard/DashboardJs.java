/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.dashboard;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

import tools.jackson.databind.node.ArrayNode;

/**
 * The JavaScript that {@link Dashboard} runs on its element in the browser, for
 * {@link Element#executeJs(Class)}.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface DashboardJs extends JsInvokers {

    /**
     * Patches the dashboard element so that it can hold the virtual children
     * that Flow adds to it.
     */
    @JsExpression("Vaadin.FlowComponentHost.patchVirtualContainer(this);")
    void patchVirtualContainer();

    /**
     * Sets the items of the dashboard, after resolving the element of each
     * widget and section from its node id.
     *
     * @param items
     *            the items, each with the node {@code id} of the component and,
     *            for a section, its {@code items}
     * @param appId
     *            the id of the application that the dashboard belongs to
     */
    @JsExpression("""
            const items = $0;
            const appId = $1;
            function populateComponents(items) {
              items.forEach(item => {
                item.component = window.Vaadin.Flow.clients[appId].getByNodeId(item.id);
                if (item.items) {
                  populateComponents(item.items);
                }
              });
            }
            populateComponents(items);
            this.items = items;
            """)
    void updateItems(ArrayNode items, String appId);

    /**
     * Adds a listener that fires a {@code dashboard-item-moved-flow} event,
     * with only the node ids of the items, when an item is moved.
     */
    @JsExpression("""
            this.addEventListener('dashboard-item-moved', (e) => {
              function mapItems(items) {
                return items.map(({id, items}) => ({
                  id,
                  ...(items && { items: mapItems(items) })
                }));
              }
              const flowItemMovedEvent = new CustomEvent('dashboard-item-moved-flow', {
                detail: {
                  item: e.detail.item.id,
                  items: mapItems(e.detail.items),
                  section: e.detail.section?.id
                }
              });
              this.dispatchEvent(flowItemMovedEvent);
            });""")
    void customizeItemMovedEvent();
}
