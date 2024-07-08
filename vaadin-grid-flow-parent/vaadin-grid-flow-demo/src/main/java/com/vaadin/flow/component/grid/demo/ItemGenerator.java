/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.demo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.demo.GridDemo.Item;

/**
 * Helper class used for generating stable random data for demo purposes.
 *
 * @author Vaadin Ltd.
 *
 */
class ItemGenerator extends BeanGenerator {

    public List<Item> generateItems(int amount) {
        LocalDate baseDate = LocalDate.of(2018, 1, 10);
        return IntStream.range(0, amount)
                .mapToObj(index -> createItem(index + 1, baseDate))
                .collect(Collectors.toList());
    }

    private Item createItem(int index, LocalDate baseDate) {
        Item item = new Item();
        item.setName("Item " + index);
        item.setPrice(100 * getRandom("price").nextDouble());
        item.setPurchaseDate(baseDate.atTime(12, 0).minus(
                1 + getRandom("purchaseDate").nextInt(3600),
                ChronoUnit.SECONDS));
        item.setEstimatedDeliveryDate(baseDate.plus(
                1 + getRandom("estimatedDeliveryDate").nextInt(15),
                ChronoUnit.DAYS));
        return item;
    }
}
