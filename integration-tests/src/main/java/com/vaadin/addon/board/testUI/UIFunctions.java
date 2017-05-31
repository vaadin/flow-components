package com.vaadin.addon.board.testUI;

import static com.vaadin.server.Sizeable.Unit.PIXELS;
import static java.lang.String.valueOf;
import static java.lang.System.setProperty;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;


import com.vaadin.addon.frp.CheckedBiFunction;
import com.vaadin.addon.frp.CheckedExecutor;
import com.vaadin.addon.frp.CheckedFunction;
import com.vaadin.addon.frp.Result;
import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 *
 */
public interface UIFunctions {

    //    String PX1024 = (4 * 128) + "px";
    String PX1024 = "setSizeFull";
    String PX0512 = (3 * 128) + "px";
    String SWITCH = "switch";
    String ID_PREFIX = "test-component-";

    static <T extends Component> Function<Stream<T>, AbstractOrderedLayout> testLayout() {
        return (stream) -> {

            final Board board = new Board();
            board.setSizeFull();

            Component[] components = stream
                .peek(Sizeable::setSizeFull)
                .map(e -> (Component) e)
                .toArray(Component[]::new);


            int i = 0;
            for (final Component component : components) {
                component.setId(ID_PREFIX + i++);
            }

            final Row row = board.addRow(components);

            final AbstractOrderedLayout baseLayout = new VerticalLayout();

            final Button button1024 = new Button(PX1024,
                                                 (ClickListener) clickEvent
                                                     -> UI.getCurrent().setSizeFull());
            button1024.setSizeFull();

            final Button button0512 = new Button(PX0512,
                                                 (ClickListener) clickEvent
                                                     -> UI.getCurrent().setWidth((2 * 128), PIXELS));
            button0512.setSizeFull();

            final Button buttonSwitch = new Button(SWITCH,
                                                   (ClickListener) clickEvent -> row.setCols(components[1],
                                                                                             (row.getCols(components[1]) > 1) ? 1 : 2));
            buttonSwitch.setSizeFull();
            baseLayout.addComponents(board, button1024, button0512, buttonSwitch);

            return baseLayout;
        };
    }

    static CheckedBiFunction<Class<? extends Component>, String, Component> newInstance() {
        return (aClass, caption) -> {
            Constructor<? extends Component> constructor = aClass.getConstructor(String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(caption);
        };
    }

    static CheckedBiFunction<Class<? extends Component>, Integer, Component[]> newInstanceArray() {
        return (clazz, amount) -> IntStream
            .range(0, amount)
            .mapToObj(i -> newInstance().apply(clazz, valueOf(i)))
            .filter(Result::isPresent)
            .map(Result::get)
            .toArray(Component[]::new);
    }

    static CheckedBiFunction<Class<? extends Component>, Integer, Stream<Component>> newInstanceStream() {
        return (clazz, amount) -> IntStream
            .range(0, amount)
            .mapToObj(i -> newInstance().apply(clazz, valueOf(i)))
            .filter(Result::isPresent)
            .map(Result::get);
    }


    CheckedFunction<String, Properties> readProperties = (filename) -> {
        try (
            final FileInputStream fis = new FileInputStream(new File(filename));
            final BufferedInputStream bis = new BufferedInputStream(fis)) {
            final Properties properties = new Properties();
            properties.load(bis);

            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    };

    CheckedExecutor readTestbenchProperties = () -> readProperties
        .apply("config/testbench.properties")
        .ifPresent(p -> p.forEach((key, value) -> setProperty((String) key, (String) value))
        );
}
