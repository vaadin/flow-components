package com.vaadin.flow.component.virtuallist.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.router.Route;

import java.util.stream.IntStream;

@Route("vaadin-virtual-list/scroll-to")
public class VirtualListScrollToPage extends Div {
    public VirtualListScrollToPage() {
        VirtualList<String> virtualList = new VirtualList<>();

        virtualList.setItems(
                IntStream.rangeClosed(0, 1000).mapToObj(String::valueOf));

        NativeButton scrollToStart = new NativeButton("Scroll to start",
                e -> virtualList.scrollToStart());
        scrollToStart.setId("scroll-to-start");

        NativeButton scrollToEnd = new NativeButton("Scroll to end",
                e -> virtualList.scrollToEnd());
        scrollToEnd.setId("scroll-to-end");

        NativeButton scrollToRow500 = new NativeButton("Scroll to row 500",
                e -> virtualList.scrollToIndex(500));
        scrollToRow500.setId("scroll-to-row-500");

        add(virtualList, scrollToStart, scrollToEnd, scrollToRow500);
    }
}
