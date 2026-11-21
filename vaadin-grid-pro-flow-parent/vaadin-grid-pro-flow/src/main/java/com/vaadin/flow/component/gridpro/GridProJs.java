/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsInvokers;

/**
 * The JavaScript that {@link GridPro} and its edit columns run in the browser,
 * for {@link Element#executeJs(Class)}. Each method says which element it runs
 * on.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
interface GridProJs extends JsInvokers {

    /**
     * Sets up the animation of the cells that are being updated. Runs on the
     * grid element.
     */
    @JsExpression("window.Vaadin.Flow.gridProConnector.initUpdatingCellAnimation(this);")
    void initUpdatingCellAnimation();

    /**
     * Clears the cell that is being updated. Runs on the grid element.
     */
    @JsExpression("window.Vaadin.Flow.gridProConnector.clearUpdatingCell(this);")
    void clearUpdatingCell();

    /**
     * Sets up the cell editable provider of an edit column. Runs on the column
     * element.
     */
    @JsExpression("window.Vaadin.Flow.gridProConnector.initCellEditableProvider(this)")
    void initCellEditableProvider();

    /**
     * Updates the value of a custom editor with the value of the edited item.
     * Called on the page rather than on an element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}.
     *
     * @param editor
     *            the element of the custom editor
     * @param itemKey
     *            the key of the edited item
     * @param grid
     *            the grid element
     */
    @JsExpression("window.Vaadin.Flow.gridProConnector.handleCustomEditorValueUpdate($0, $1, $2)")
    void handleCustomEditorValueUpdate(Element editor, String itemKey,
            Element grid);

    /**
     * Patches the edit mode renderer of a column. Called on the page rather
     * than on an element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}.
     *
     * @param column
     *            the column element
     */
    @JsExpression("window.Vaadin.Flow.gridProConnector.patchEditModeRenderer($0)")
    void patchEditModeRenderer(Element column);

    /**
     * Sets the component that a column renders in edit mode. Called on the page
     * rather than on an element, with
     * {@link com.vaadin.flow.component.page.Page#executeJs(Class)}.
     *
     * @param column
     *            the column element
     * @param editor
     *            the element of the editor component
     */
    @JsExpression("window.Vaadin.Flow.gridProConnector.setEditModeRenderer($0, $1)")
    void setEditModeRenderer(Element column, Element editor);
}
