package com.vaadin.flow.component.crud;

/*
 * #%L
 * Vaadin Crud for Vaadin 10
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

import com.helger.commons.annotation.VisibleForTesting;
import com.vaadin.flow.component.crud.annotation.Hidden;
import com.vaadin.flow.component.crud.annotation.Order;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

public class Util {

    public static String capitalize(String name) {
        return Character.toString(name.charAt(0))
                .toUpperCase() + name.substring(1);
    }

    public static Field[] visiblePropertiesIn(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(e -> !Modifier.isStatic(e.getModifiers()))
                .filter(e -> !e.isAnnotationPresent(Hidden.class))
                .filter(e -> {
                    Method getter = getterFor(e, clazz);
                    return getter == null || !getter.isAnnotationPresent(Hidden.class);
                })
                .sorted(Comparator.comparingInt(e -> orderFor(e, clazz)))
                .toArray(Field[]::new);
    }

    @VisibleForTesting
    public static Method getterFor(Field field, Class<?> clazz) {
        try {
            return clazz.getMethod("get" + capitalize(field.getName()));
        } catch (NoSuchMethodException e1) {
            if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                try {
                    return clazz.getMethod("is" + capitalize(field.getName()));
                } catch (NoSuchMethodException ignored) { }
            }
        }

        return null;
    }

    @VisibleForTesting
    static int orderFor(Field field, Class<?> clazz) {
        Order annotation = field.getAnnotation(Order.class);
        if (annotation == null) {
            Method getter = getterFor(field, clazz);
            if (getter != null) {
                annotation = getter.getAnnotation(Order.class);
            }
        }

        return annotation != null ? annotation.value() : Integer.MAX_VALUE;
    }
}
