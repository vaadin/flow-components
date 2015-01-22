package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

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
 * {@link Spreadsheet#setPopup(CellReference, PopupButton)},
 * {@link Spreadsheet#setPopup(String, PopupButton)} or
 * {@link Spreadsheet#setPopup(int, int, PopupButton)}. The button can be
 * removed from the target cell by giving <code>null</code> as the PopupButton
 * parameter to one of the previously mentioned methods.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
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

    /**
     * Constructs a new PopupButton.
     */
    public PopupButton() {
        registerRpc(rpc);
    }

    /**
     * Constructs a new PopupButton with the given content.
     * 
     * @param content
     *            Content of the pop-up
     */
    public PopupButton(Component content) {
        this();
        addComponent(content);
    }

    /**
     * Gets the cell reference for the cell that contains this pop-up button.
     * 
     * @return Target cell reference
     */
    public CellReference getCellReference() {
        return new CellReference(getState(false).row - 1,
                getState(false).col - 1);
    }

    void setCellReference(CellReference cellReference) {
        getState().col = cellReference.getCol() + 1;
        getState().row = cellReference.getRow() + 1;
    }

    /**
     * Gets the column for this pop-up button.
     * 
     * @return Column index, 0-based
     */
    public int getColumn() {
        return getState(false).col - 1;
    }

    /**
     * Gets the row for this pop-up button.
     * 
     * @return Row index, 0-based
     */
    public int getRow() {
        return getState(false).row - 1;
    }

    /**
     * Opens the pop-up if the button is currently rendered in the visible area
     * of the Spreadsheet.
     */
    public void openPopup() {
        getRpcProxy(PopupButtonClientRpc.class).openPopup();
    }

    /**
     * Closes the pop-up if it is open.
     */
    public void closePopup() {
        getRpcProxy(PopupButtonClientRpc.class).closePopup();
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

    @Override
    protected PopupButtonState getState() {
        return (PopupButtonState) super.getState();
    }

    @Override
    protected PopupButtonState getState(boolean markAsDirty) {
        return (PopupButtonState) super.getState(markAsDirty);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.ui.AbstractComponentContainer#addComponent(com.vaadin.ui.Component
     * )
     */
    @Override
    public void addComponent(Component c) {
        children.add(c);
        super.addComponent(c);
        markAsDirty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.ui.AbstractComponentContainer#removeComponent(com.vaadin.ui
     * .Component)
     */
    @Override
    public void removeComponent(Component c) {
        children.remove(c);
        super.removeComponent(c);
        markAsDirty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.ui.ComponentContainer#replaceComponent(com.vaadin.ui.Component
     * , com.vaadin.ui.Component)
     */
    @Override
    public void replaceComponent(Component oldComponent, Component newComponent) {
        int oldIndex = children.indexOf(oldComponent);
        removeComponent(oldComponent);
        children.add(oldIndex, newComponent);
        super.addComponent(newComponent);
        markAsDirty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.ComponentContainer#getComponentCount()
     */
    @Override
    public int getComponentCount() {
        return children.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.HasComponents#iterator()
     */
    @Override
    public Iterator<Component> iterator() {
        return children.iterator();
    }

    /**
     * Mark the button with "active" - style. See {@link PopupButtonWidget} for
     * the CSS class name.
     * 
     * @param active
     *            true to add "active" style, false to remove it
     */
    public void markActive(boolean active) {
        getState().active = active;
    }

    /**
     * Adds a {@link PopupOpenListener} to this pop-up button.
     * 
     * @param listener
     *            The listener to add
     */
    public void addPopupOpenListener(PopupOpenListener listener) {
        addListener(PopupOpenEvent.class, listener,
                PopupOpenListener.POPUP_OPEN_METHOD);
    }

    /**
     * Removes the given {@link PopupOpenListener} from this pop-up button.
     * 
     * @param listener
     *            The listener to remove
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
     * {@link #getPopupButton()} has been clicked and the pop-up has been
     * opened.
     */
    public static class PopupOpenEvent extends Component.Event {

        /**
         * Constructs a new open event for the given PopupButton.
         * 
         * @param source
         *            PopupButton component that has been opened.
         */
        public PopupOpenEvent(Component source) {
            super(source);
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
    public static class PopupCloseEvent extends Component.Event {
        /**
         * Constructs a new close event for the given PopupButton.
         * 
         * @param source
         *            PopupButton component that has been closed.
         */
        public PopupCloseEvent(Component source) {
            super(source);
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
        public static final Method POPUP_CLOSE_METHOD = ReflectTools
                .findMethod(PopupCloseListener.class, "onPopupClose",
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