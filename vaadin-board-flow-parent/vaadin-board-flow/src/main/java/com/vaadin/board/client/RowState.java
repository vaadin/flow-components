package com.vaadin.board.client;

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
