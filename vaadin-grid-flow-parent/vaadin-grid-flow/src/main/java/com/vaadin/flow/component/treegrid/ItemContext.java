package com.vaadin.flow.component.treegrid;

record ItemContext<T>(Object id, Cache<T> cache, int index) {}
