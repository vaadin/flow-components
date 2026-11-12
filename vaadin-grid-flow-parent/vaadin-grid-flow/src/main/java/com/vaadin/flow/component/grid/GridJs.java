/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.io.Serializable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;

import tools.jackson.databind.node.ArrayNode;

/**
 * The JavaScript that {@link Grid} and its columns run in the browser, for
 * {@link Element#executeJs(Class)}. Unless stated otherwise, the methods are
 * called on the grid element.
 * <p>
 * Most methods call the grid connector, <code>this.$connector</code>, which
 * {@link #initConnector(Element)} installs. The methods that can run before the
 * connector is installed skip the call in that case.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
@JsDefinition
interface GridJs extends Serializable {

    /**
     * Installs the grid connector, <code>$connector</code>, on the grid
     * element, if the element exists on the client. Called on the page rather
     * than on the grid element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}, so that it
     * runs before the other methods.
     *
     * @param grid
     *            the grid element
     */
    @JsExpression("if ($0) window.Vaadin.Flow.gridConnector.initLazy($0)")
    void initConnector(Element grid);

    /**
     * Sets the internal id of a column on its client-side element, looked up by
     * its node id so that it also works for a hidden column.
     *
     * @param appId
     *            the id of the application the column belongs to
     * @param nodeId
     *            the node id of the column element
     * @param columnId
     *            the internal id of the column
     */
    @JsExpression("Vaadin.Flow.clients[$0].getByNodeId($1)._flowId = $2")
    void setColumnFlowId(String appId, int nodeId, String columnId);

    /**
     * Resets the data of the grid connector, if it is initialized.
     */
    @JsExpression("this.$connector?.reset()")
    void resetConnector();

    /**
     * Sets the selection mode of the grid connector, if it is initialized.
     *
     * @param selectionMode
     *            the name of the selection mode
     */
    @JsExpression("this.$connector?.setSelectionMode($0)")
    void setSelectionMode(String selectionMode);

    /**
     * Sets the sort directions shown by the sorters of the grid, if the grid
     * connector is initialized.
     *
     * @param directions
     *            the sort directions, as objects with the column id and the
     *            direction
     * @return the pending result of the call
     */
    @JsExpression("this.$connector?.setSorterDirections($0)")
    PendingJavaScriptResult setSorterDirections(ArrayNode directions);

    /**
     * Scrolls to the last row of the grid.
     */
    @JsExpression("this.scrollToIndex(this._flatSize)")
    void scrollToEnd();

    /**
     * Sets the header content of a column.
     *
     * @param column
     *            the column element
     * @param content
     *            the header content: the {@link Element} of the header
     *            component (see {@link Component#getElement()}), which the
     *            browser receives as its DOM element, or the header text as a
     *            {@link String}
     * @param showSorter
     *            whether to show a sorter in the header
     * @param sorterPath
     *            the path of the sorter, or {@code null} if no sorter is shown
     */
    @JsExpression("this.$connector.setHeaderRenderer($0, { content: $1, showSorter: $2, sorterPath: $3 });")
    void setHeaderRenderer(Element column, Serializable content,
            boolean showSorter, String sorterPath);

    /**
     * Sets the footer content of a column.
     *
     * @param column
     *            the column element
     * @param content
     *            the footer content: the {@link Element} of the footer
     *            component (see {@link Component#getElement()}), which the
     *            browser receives as its DOM element, or the footer text as a
     *            {@link String}
     */
    @JsExpression("this.$connector.setFooterRenderer($0, { content: $1 })")
    void setFooterRenderer(Element column, Serializable content);

    /**
     * Assigns a tooltip text generator that returns the column-specific or the
     * row-specific tooltip text of an item. Called on the tooltip element of
     * the grid.
     */
    @JsExpression("""
            this.generator = ({ item, column }) => {
                const { gridtooltips } = item ?? {};
                if (gridtooltips) {
                    return (column ? gridtooltips[column._flowId] : null) ?? gridtooltips.row;
                }
            }
            """)
    void initTooltipGenerator();
}
