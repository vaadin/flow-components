package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud
 * %%
 * Copyright (C) 2018 Vaadin Ltd
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

import com.vaadin.flow.component.ComponentEvent;

/**
 * Event fired to a crud grid when the internationalization object is changed.
 * This allows the grid to localize its content, most especially the
 * accessibility of the edit column.
 *
 * <br>
 * <code>
 *     ComponentUtil.addListener(myGrid, CrudI18nUpdatedEvent.class, event -&gt; {<br>
 *       CrudI18n newI18n = event.getI18n();<br>
 *       // Localize an item in myGrid.<br>
 *     });<br>
 * </code>
 */
public class CrudI18nUpdatedEvent extends ComponentEvent<Crud<?>> {

    private final CrudI18n i18n;

    /**
     * Creates a new event using the given source and indicator whether the
     * event originated from the client side or the server side.
     *
     * @param source
     *            the source component
     * @param fromClient
     *            <code>true</code> if the event originated from the client
     * @param i18n
     *            the new i18n object
     */
    public CrudI18nUpdatedEvent(Crud<?> source, boolean fromClient,
            CrudI18n i18n) {
        super(source, fromClient);
        this.i18n = i18n;
    }

    /**
     * Gets the new i18n
     *
     * @return the new i18n
     */
    public CrudI18n getI18n() {
        return i18n;
    }
}
