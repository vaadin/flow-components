package com.vaadin.flow.component.shared;

import com.vaadin.flow.data.provider.DataChangeEvent;

import java.io.Serializable;
import java.util.Objects;

public abstract class DataChangeHandler<T> implements Serializable {

    private SelectionOnDataChange selectionOnDataChange;

    public DataChangeHandler(
            SelectionOnDataChange initialSelectionOnDataChange) {
        setSelectionOnDataChange(initialSelectionOnDataChange);
    }

    public final void setSelectionOnDataChange(
            SelectionOnDataChange selectionOnDataChange) {
        Objects.requireNonNull(selectionOnDataChange, "");
        this.selectionOnDataChange = selectionOnDataChange;
    }

    public final SelectionOnDataChange getSelectionOnDataChange() {
        return selectionOnDataChange;
    }

    public final void handleDataChange(DataChangeEvent<T> dataChangeEvent) {
        switch (selectionOnDataChange) {
        case PRESERVE_ALL -> onPreserveAll(dataChangeEvent);
        case PRESERVE_EXISTENT -> onPreserveExisting(dataChangeEvent);
        case DISCARD -> onDiscard(dataChangeEvent);
        }
    }

    public abstract void onPreserveAll(DataChangeEvent<T> dataChangeEvent);

    public abstract void onPreserveExisting(DataChangeEvent<T> dataChangeEvent);

    public abstract void onDiscard(DataChangeEvent<T> dataChangeEvent);
}
