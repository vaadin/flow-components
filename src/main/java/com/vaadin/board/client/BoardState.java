package com.vaadin.board.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.shared.Connector;

public class BoardState
        extends com.vaadin.shared.ui.AbstractComponentContainerState {

    public static class RowState {
        public List<Connector> components = new ArrayList<>();
        public Map<Connector, Integer> cols = new HashMap<>();
    }

    public List<RowState> rows = new ArrayList<>();

}