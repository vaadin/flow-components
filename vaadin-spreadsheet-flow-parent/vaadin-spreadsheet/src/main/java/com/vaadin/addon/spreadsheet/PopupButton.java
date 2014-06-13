package com.vaadin.addon.spreadsheet;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.client.PopupButtonClientRpc;
import com.vaadin.addon.spreadsheet.client.PopupButtonServerRpc;
import com.vaadin.addon.spreadsheet.client.PopupButtonState;
import com.vaadin.addon.spreadsheet.client.PopupButtonWidget;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.util.ReflectTools;

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
 * {@link Spreadsheet#addPopupButton(PopupButton)}. Remove with
 * {@link Spreadsheet#removePopup(PopupButton)}.
 */
public class PopupButton extends AbstractComponentContainer {

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

    private List<Component> children = new ArrayList<Component>();

    public PopupButton() {
        registerRpc(rpc);
    }

    /**
     * Create a new pop-up button to the specific cell.
     * 
     * @param cellReference
     */
    public PopupButton(CellReference cellReference) {
        this();
        getState().col = cellReference.getCol() + 1;
        getState().row = cellReference.getRow() + 1;
    }

    /**
     * Gets the cell reference for the cell that contains this popup button.
     * 
     * @return cell reference
     */
    public CellReference getCellReference() {
        return new CellReference(getState(false).row - 1,
                getState(false).col - 1);
    }

    /**
     * Gets the column for this pop-up button.
     * 
     * @return 0-based
     */
    public int getColumn() {
        return getState(false).col - 1;
    }

    /**
     * Set the column for this pop-up button.
     * 
     * @param column
     *            0-based
     */
    public void setColumn(int column) {
        getState().col = column + 1;
    }

    /**
     * Gets the row for this pop-up button.
     * 
     * @return 0-based
     */
    public int getRow() {
        return getState(false).row - 1;
    }

    /**
     * Set the row for this pop-up button.
     * 
     * @param row
     *            0-based
     */
    public void setRow(int row) {
        getState().row = row + 1;
    }

    /**
     * Opens the popup if the button is rendered inside the spreadsheet.
     */
    public void openPopup() {
        getRpcProxy(PopupButtonClientRpc.class).openPopup();
    }

    /**
     * Closes the popup if it is open.
     */
    public void closePopup() {
        getRpcProxy(PopupButtonClientRpc.class).closePopup();
    }

    /**
     * Is the pop-up header currently hidden.
     * <p>
     * 
     * @return
     */
    public boolean isHeaderHidden() {
        return getState().headerHidden;
    }

    /**
     * Set the pop-up header visible or hidden.
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
     */
    public void setPopupWidth(String width) {
        getState().popupWidth = width;
    }

    /**
     * Gets the width for this pop-up button's pop-up. Can be null or empty.
     * 
     * @return the pop-up width
     */
    public String getPopupWidth() {
        return getState().popupWidth;
    }

    /**
     * Set the height for this pop-up button's pop-up. Can be null or empty for
     * undefined height.
     * 
     * @param height
     */
    public void setPopupHeight(String height) {
        getState().popupHeight = height;
    }

    /**
     * Gets the height for this pop-up button's pop-up. Can be null or empty.
     * 
     * @return the pop-up height
     */
    public String getPopupHeight() {
        return getState().popupHeight;
    }

    @Override
    protected PopupButtonState getState() {
        return (PopupButtonState) super.getState();
    }

    @Override
    protected PopupButtonState getState(boolean markAsDirty) {
        return (PopupButtonState) super.getState(markAsDirty);
    }

    /**
     * Adds a component to the end of this {@link PopupButton}'s pop-up.
     */
    @Override
    public void addComponent(Component c) {
        children.add(c);
        super.addComponent(c);
        markAsDirty();
    }

    /**
     * Removes a component from this {@link PopupButton}'s pop-up.
     */
    @Override
    public void removeComponent(Component c) {
        children.remove(c);
        super.removeComponent(c);
        markAsDirty();
    }

    /**
     * Not supported!
     */
    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        // TODO
        throw new UnsupportedOperationException(
                "replaceComponent not implemented");
    }

    /**
     * Returns the number of components inside this {@link PopupButton}'s
     * pop-up.
     */
    @Override
    public int getComponentCount() {
        return children.size();
    }

    @Override
    public Iterator<Component> iterator() {
        return children.iterator();
    }

    /**
     * Mark the button with "active" - style. See {@link PopupButtonWidget} for
     * the class name.
     * 
     * @param active
     */
    public void markActive(boolean active) {
        getState().active = active;
    }

    /**
     * Adds a {@link PopupOpenListener} to this pop-up button.
     * 
     * @param listener
     */
    public void addPopupOpenListener(PopupOpenListener listener) {
        addListener(PopupOpenEvent.class, listener,
                PopupOpenListener.POPUP_OPEN_METHOD);
    }

    /**
     * Removes the given {@link PopupOpenListener} from this pop-up button.
     * 
     * @param listener
     */
    public void removePopupOpenListener(PopupOpenListener listener) {
        removeListener(PopupOpenEvent.class, listener,
                PopupOpenListener.POPUP_OPEN_METHOD);
    }

    /**
     * Adds a {@link PopupCloseListener} to this pop-up button.
     * 
     * @param listener
     */
    public void addPopupCloseListener(PopupCloseListener listener) {
        addListener(PopupCloseEvent.class, listener,
                PopupCloseListener.POPUP_CLOSE_METHOD);
    }

    /**
     * Removes the given {@link PopupCloseListener} from this pop-up button.
     * 
     * @param listener
     */
    public void removePopupCloseListener(PopupCloseListener listener) {
        removeListener(PopupCloseEvent.class, listener,
                PopupCloseListener.POPUP_CLOSE_METHOD);
    }

    private void fireOpen() {
        fireEvent(new PopupOpenEvent(this));
    }

    private void fireClose() {
        fireEvent(new PopupCloseEvent(this));
    }

    /**
     * An event fired after the pop-up button returned by
     * {@link #getPopupButton()} has been clicked and the popup has been opened.
     * 
     */
    public static class PopupOpenEvent extends Component.Event {

        public PopupOpenEvent(Component source) {
            super(source);
        }

        /**
         * Gets the {@link PopupButton} where the event occurred.
         * 
         * @return the source of the event
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
     * 
     */
    public static class PopupCloseEvent extends Component.Event {
        public PopupCloseEvent(Component source) {
            super(source);
        }

        /**
         * Gets the {@link PopupButton} where the event occurred.
         * 
         * @return the source of the event
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
        public static final Method POPUP_CLOSE_METHOD = ReflectTools
                .findMethod(PopupCloseListener.class, "onPopupClose",
                        PopupCloseEvent.class);

        /**
         * Called when the pop-up for the {@link PopupButton} returned by
         * {@link PopupCloseEvent#getPopupButton()} has been closed.
         * 
         * @param event
         */
        public void onPopupClose(PopupCloseEvent event);
    }

}
