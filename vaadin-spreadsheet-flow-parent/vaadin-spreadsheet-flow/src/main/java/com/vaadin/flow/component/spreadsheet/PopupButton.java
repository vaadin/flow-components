/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.framework.ReflectTools;
import com.vaadin.flow.component.spreadsheet.rpc.PopupButtonServerRpc;
import com.vaadin.flow.component.spreadsheet.shared.PopupButtonState;
import com.vaadin.flow.shared.Registration;

/**
 * A button component that when clicked opens a pop-up next to spreadsheet cell
 * containing the button.
 * <p>
 * Vaadin components can be added inside the pop-up.
 * <p>
 * By default, the pop-up displays a close-button and a header containing the
 * current value of the cell that the pop-up button belongs to.
 * <p>
 * A button can be marked with "active" style with {@link #markActive(boolean)}.
 * <p>
 * To add the pop-up button to a specific spreadsheet, call
 * {@link Spreadsheet#setPopup(CellReference, PopupButton)},
 * {@link Spreadsheet#setPopup(String, PopupButton)} or
 * {@link Spreadsheet#setPopup(int, int, PopupButton)}. The button can be
 * removed from the target cell by giving <code>null</code> as the PopupButton
 * parameter to one of the previously mentioned methods.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
@Tag("div")
public class PopupButton extends Component {

    private PopupButtonServerRpc rpc = new PopupButtonServerRpc() {

        @Override
        public void onPopupClose() {
            fireClose();
        }

        @Override
        public void onPopupButtonClick() {
            fireOpen();
        }
    };

    private Component child;

    private PopupButtonState state = new PopupButtonState();

    /**
     * Constructs a new PopupButton.
     */
    public PopupButton() {
        registerRpc(rpc);
    }

    private void registerRpc(PopupButtonServerRpc rpc) {
        this.rpc = rpc;
    }

    /**
     * Constructs a new PopupButton with the given content.
     *
     * @param content
     *            Content of the pop-up
     */
    public PopupButton(Component content) {
        this();
        child = content;
    }

    /**
     * Gets the cell reference for the cell that contains this pop-up button.
     *
     * @return Target cell reference
     */
    public CellReference getCellReference() {
        return new CellReference(getState().sheet, getState().row - 1,
                getState().col - 1, true, true);
    }

    void setCellReference(CellReference cellReference) {
        getState().col = cellReference.getCol() + 1;
        getState().row = cellReference.getRow() + 1;
        getState().sheet = cellReference.getSheetName();
    }

    /**
     * Gets the column for this pop-up button.
     *
     * @return Column index, 0-based
     */
    public int getColumn() {
        return getState().col - 1;
    }

    /**
     * Gets the row for this pop-up button.
     *
     * @return Row index, 0-based
     */
    public int getRow() {
        return getState().row - 1;
    }

    /**
     * Opens the pop-up if the button is currently rendered in the visible area
     * of the Spreadsheet.
     */
    public void openPopup() {
        getElement().appendChild(getContent().getElement());
        getParent().ifPresent(parent -> {
            parent.getElement().callJsFunction("onPopupButtonOpen",
                    getRow() + 1, getColumn() + 1,
                    getElement().getNode().getId(),
                    UI.getCurrent().getInternals().getAppId());
        });
        fireOpen();
    }

    /**
     * Closes the pop-up if it is open.
     */
    public void closePopup() {
        getParent().ifPresent(parent -> parent.getElement()
                .callJsFunction("closePopup", getRow() + 1, getColumn() + 1));
        fireClose();
    }

    /**
     * Tells if the pop-up header is currently hidden.
     *
     * @return true if header is hidden, false otherwise
     */
    public boolean isHeaderHidden() {
        return getState().headerHidden;
    }

    /**
     * Sets the pop-up header visible or hidden.
     *
     * @param headerHidden
     *            <code>true</code> for hidden, <code>false</code> for visible.
     */
    public void setHeaderHidden(boolean headerHidden) {
        getState().headerHidden = headerHidden;
    }

    /**
     * Set the width for this pop-up button's pop-up. Can be null or empty for
     * undefined width.
     *
     * @param width
     *            New width for the pop-up
     */
    public void setPopupWidth(String width) {
        getState().popupWidth = width;
    }

    /**
     * Gets the width for this pop-up button's pop-up. Can be null or empty.
     *
     * @return Width of the pop-up
     */
    public String getPopupWidth() {
        return getState().popupWidth;
    }

    /**
     * Set the height for this pop-up button's pop-up. Can be null or empty for
     * undefined height.
     *
     * @param height
     *            New height for the pop-up
     */
    public void setPopupHeight(String height) {
        getState().popupHeight = height;
    }

    /**
     * Gets the height for this pop-up button's pop-up. Can be null or empty.
     *
     * @return Height of the pop-up
     */
    public String getPopupHeight() {
        return getState().popupHeight;
    }

    protected PopupButtonState getState() {
        return state;
    }

    /**
     * Set the contents of the popup.
     *
     */
    public void setContent(Component content) {
        child = content;
    }

    /**
     * Get the contents of the popup.
     */
    public Component getContent() {
        return child;
    }

    /**
     * Mark the button with "active" - style. See PopupButtonWidget for the CSS
     * class name.
     *
     * @param active
     *            true to add "active" style, false to remove it
     */
    public void markActive(boolean active) {
        getState().active = active;
    }

    public boolean isActive() {
        return getState().active;
    }

    /**
     * Adds a {@link PopupOpenListener} to this pop-up button.
     *
     * @param listener
     *            The listener to add
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addPopupOpenListener(PopupOpenListener listener) {
        return addListener(PopupOpenEvent.class, listener::onPopupOpen);
    }

    /**
     * Adds a {@link PopupCloseListener} to this pop-up button.
     *
     * @param listener
     *            The listener to add
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addPopupCloseListener(PopupCloseListener listener) {
        return addListener(PopupCloseEvent.class, listener::onPopupClose);
    }

    private void fireOpen() {
        fireEvent(new PopupOpenEvent(this));
    }

    private void fireClose() {
        fireEvent(new PopupCloseEvent(this));
    }

    /**
     * An event fired after the pop-up button returned by
     * {@link #getPopupButton()} has been clicked and the pop-up has been
     * opened.
     */
    public static class PopupOpenEvent extends ComponentEvent<Component> {

        /**
         * Constructs a new open event for the given PopupButton.
         *
         * @param source
         *            PopupButton component that has been opened.
         */
        public PopupOpenEvent(Component source) {
            super(source, false);
        }

        /**
         * Gets the {@link PopupButton} where the event occurred.
         *
         * @return PopupButton component that has been opened.
         */
        public PopupButton getPopupButton() {
            return (PopupButton) getSource();
        }

    }

    /**
     * Interface for listening for a {@link PopupOpenEvent} fired by a
     * {@link PopupButton}.
     */
    public interface PopupOpenListener extends Serializable {
        public static final Method POPUP_OPEN_METHOD = ReflectTools.findMethod(
                PopupOpenListener.class, "onPopupOpen", PopupOpenEvent.class);

        /**
         * Called when a {@link PopupButton} has been clicked and the pop-up has
         * been opened. A reference to the pop-up button is given by
         * {@link PopupOpenEvent#getPopupButton()}.
         *
         * @param event
         *            An event containing the opened pop-up button
         */
        public void onPopupOpen(PopupOpenEvent event);
    }

    /**
     * An event fired after the pop-up for the {@link PopupButton} returned by
     * {@link #getPopupButton()} has been closed.
     */
    public static class PopupCloseEvent extends ComponentEvent<Component> {
        /**
         * Constructs a new close event for the given PopupButton.
         *
         * @param source
         *            PopupButton component that has been closed.
         */
        public PopupCloseEvent(Component source) {
            super(source, false);
        }

        /**
         * Gets the {@link PopupButton} where the event occurred.
         *
         * @return PopupButton component that has been closed.
         */
        public PopupButton getPopupButton() {
            return (PopupButton) getSource();
        }
    }

    /**
     * Interface for listening for a {@link PopupCloseEvent} fired by a
     * {@link PopupButton}.
     */
    public interface PopupCloseListener extends Serializable {
        public static final Method POPUP_CLOSE_METHOD = ReflectTools.findMethod(
                PopupCloseListener.class, "onPopupClose",
                PopupCloseEvent.class);

        /**
         * Called when the pop-up for the {@link PopupButton} returned by
         * {@link PopupCloseEvent#getPopupButton()} has been closed.
         *
         * @param event
         *            An event containing the closed pop-up button
         */
        public void onPopupClose(PopupCloseEvent event);
    }
}
