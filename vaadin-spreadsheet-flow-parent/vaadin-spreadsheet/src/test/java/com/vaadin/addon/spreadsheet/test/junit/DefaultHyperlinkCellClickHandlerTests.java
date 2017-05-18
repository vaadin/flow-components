package com.vaadin.addon.spreadsheet.test.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.DefaultHyperlinkCellClickHandler;

/**
 * Created by mtzukanov on 24.4.2017.
 */
public class DefaultHyperlinkCellClickHandlerTests {

    private Map<String, String> testStrings = new HashMap<String, String>() {
        {
            put("=HYPERLINK(\"[spreadsheet_hyperlinks.xlsx]Sheet1!B6\", \"explicit link to next cell\")",
                "\"[spreadsheet_hyperlinks.xlsx]Sheet1!B6\"");
            put("G( A2 , A3)", "A2");
            put("G( \"hello\" ,A3)", "\"hello\"");
            put("G(\"comma,comma\",A3)", "\"comma,comma\"");
        }
    };

    @Test
    public void hyperlinkParser_validStrings_correctParsed()
        throws NoSuchMethodException, InvocationTargetException,
        IllegalAccessException {
        final Method getFirstArgumentFromFormula = DefaultHyperlinkCellClickHandler.class
            .getDeclaredMethod("getFirstArgumentFromFormula", String.class);

        getFirstArgumentFromFormula.setAccessible(true);

        for (String testString : testStrings.keySet()) {
            final Object result = getFirstArgumentFromFormula
                .invoke(null, testString);
            
            Assert.assertEquals(testStrings.get(testString), result);
        }
    }
}
