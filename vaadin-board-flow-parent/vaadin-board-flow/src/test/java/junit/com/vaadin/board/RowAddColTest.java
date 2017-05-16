package junit.com.vaadin.board;

import static junit.com.vaadin.board.RowTestHelperFunctions.createButtonRow;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.IntStream;

import org.junit.Test;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.board.client.RowState;
import com.vaadin.ui.Button;

/**
 *
 */
public class RowAddColTest {

    @Test
    public void testFrom1To4()
        throws Exception {
        IntStream
            .range(1, 5)
            .forEachOrdered(i -> {
                Row apply = createButtonRow().apply(i);
                int usedColAmount = -1;
                try {
                    Method getState = Row.class.getDeclaredMethod("getState");
                    getState.setAccessible(true);
                    RowState rowState = (RowState) getState.invoke(apply);
                    usedColAmount = rowState.usedColAmount();
                } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException e) {
                    e.printStackTrace();
                }
                assertEquals(i, usedColAmount);});
    }

    @Test(expected = IllegalStateException.class)
    public void testWithColValue5_IllegalStateException()
        throws Exception {
        new Board().addRow().addComponent(new Button(), 5);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithColValue0_IllegalStateException()
        throws Exception {
        new Board().addRow().addComponent(new Button(), 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithColValueMinusOne_IllegalStateException()
        throws Exception {
        new Board().addRow().addComponent(new Button(), -1);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithOnePlus4_IllegalStateException()
        throws Exception {
        createButtonRow().apply(1).addComponent(new Button(), 4);
    }

    @Test()
    public void testWithOnePlus3()
        throws Exception {
        createButtonRow().apply(1).addComponent(new Button(), 3);
    }

}
