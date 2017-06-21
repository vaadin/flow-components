package com.vaadin.board.client;

/*
 * #%L
 * Vaadin Board
 * %%
 * Copyright (C) 2017 Vaadin Ltd
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.AbstractComponentContainerState;

public class RowState extends AbstractComponentContainerState {
    public Map<Connector, Integer> cols = new HashMap<>();

    public int usedColAmount(){
        Collection<Integer> values = cols.values();
        Integer sum = 0;
        for (Integer value : values) {
            sum = sum + value;
        }
        return sum;
    }







}
