package com.vaadin.flow.component.gridpro;

/*
 * #%L
 * Vaadin GridPro
 * %%
 * Copyright (C) 2018 - 2019 Vaadin Ltd
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

import com.vaadin.flow.function.SerializableBiConsumer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configurating class with common available properties for different types of edit columns used
 * inside a {@link GridPro}.
 *
 * @author Vaadin Ltd.
 */
public class EditColumnConfigurator {

    private SerializableBiConsumer<Object, String> handler;
    private EditorType type;
    private List<String> options;

    private EditColumnConfigurator(SerializableBiConsumer<Object, String> handler, EditorType type, List<String> options) {
        this.handler = handler;
        this.type = type;
        this.options = options;
    }

    protected EditorType getType() {
        return this.type;
    }

    protected SerializableBiConsumer<Object, String> getHandler() {
        return this.handler;
    }

    protected List<String> getOptions() {
        return this.options;
    }

    /**
     * Constructs a new Column Configurator with text editor preset for column creation.
     *
     * @param handler
     *            the callback function allowing to operate with the data
     */
    public static EditColumnConfigurator text(SerializableBiConsumer handler) {
        return new EditColumnConfigurator(handler, EditorType.TEXT, Collections.emptyList());
    }

    /**
     * Constructs a new Column Configurator with checkbox editor preset for column creation.
     *
     * @param handler
     *            the callback function allowing to operate with the data
     */
    public static EditColumnConfigurator checkbox(SerializableBiConsumer handler) {
        return new EditColumnConfigurator(handler, EditorType.CHECKBOX, Collections.emptyList());
    }

    /**
     * Constructs a new Column Configurator with select editor preset for column creation.
     *
     * @param handler
     *            the callback function allowing to operate with the data
     *
     * @param options
     *            the callback function allowing to operate with the data
     *
     */
    public static EditColumnConfigurator select(SerializableBiConsumer handler, List<String> options) {
        Objects.requireNonNull(options);

        return new EditColumnConfigurator(handler, EditorType.SELECT, options);
    }
}

