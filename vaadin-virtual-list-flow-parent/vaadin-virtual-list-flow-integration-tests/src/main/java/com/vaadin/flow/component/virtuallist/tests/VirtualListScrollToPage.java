package com.vaadin.flow.component.virtuallist.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("vaadin-virtual-list/scroll-to")
public class VirtualListScrollToPage extends Div implements BeforeEnterObserver {
    VirtualList<String> virtualList;

    public VirtualListScrollToPage() {
        List<String> items = IntStream.rangeClosed(1, 1000).mapToObj(String::valueOf).collect(Collectors.toList());

        virtualList = new VirtualList<>();
        virtualList.setItems(items);

        NativeButton scrollToStart = new NativeButton("Scroll to start",
                e -> virtualList.scrollToStart());
        scrollToStart.setId("scroll-to-start");

        NativeButton scrollToEnd = new NativeButton("Scroll to end",
                e -> virtualList.scrollToEnd());
        scrollToEnd.setId("scroll-to-end");

        NativeButton scrollToRow500 = new NativeButton("Scroll to row 500",
                e -> virtualList.scrollToIndex(500));
        scrollToRow500.setId("scroll-to-row-500");

        NativeButton addItemsAndScrollToItem = new NativeButton("Add 1000 items and scroll to new item",
                e -> {
                    IntStream.rangeClosed(items.size() + 1, items.size() + 1000).mapToObj(String::valueOf).forEach(items::add);
                    virtualList.getDataProvider().refreshAll();
                    virtualList.scrollToIndex(1500);
                });
        addItemsAndScrollToItem.setId("add-items-and-scroll-to-item");

        NativeButton addItemsAndScrollToEnd = new NativeButton("Add 1000 items and scroll to end",
                e -> {
                    IntStream.rangeClosed(items.size() + 1, items.size() + 1000).mapToObj(String::valueOf).forEach(items::add);
                    virtualList.getDataProvider().refreshAll();
                    virtualList.scrollToEnd();
                });
        addItemsAndScrollToEnd.setId("add-items-and-scroll-to-end");

        add(virtualList, scrollToStart, scrollToEnd, scrollToRow500, addItemsAndScrollToItem, addItemsAndScrollToEnd);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> scrollTo = event.getLocation().getQueryParameters().getSingleParameter("initialPosition");

        if (scrollTo.isPresent() && scrollTo.get().equals("middle")) {
            virtualList.scrollToIndex(500);
        }

        if (scrollTo.isPresent() && scrollTo.get().equals("end")) {
            virtualList.scrollToEnd();
        }
    }
}
