package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between a localized formula and a non-localized formula.
 * <p>
 * This is needed because internally POI only handles formulas with '.' as the
 * decimal separator, and ',' as the argument separator.
 */
public class FormulaFormatter implements Serializable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FormulaFormatter.class);

    /*
     * Classes for the intermediary token format
     */
    private class FormulaToken implements Serializable {
        private final String content;

        public FormulaToken(char charContent) {
            this(Character.toString(charContent));
        }

        public FormulaToken(String content) {
            if (content == null) {
                throw new IllegalArgumentException();
            }
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }

    protected class NumberToken extends FormulaToken {
        public NumberToken(String content) {
            super(content);
        }
    }

    protected class SeparatorToken extends FormulaToken {
        public SeparatorToken(char content) {
            super(content);
        }
    }

    /**
     * Convert from a localized format to a non-localized.
     *
     * @param formulaValue
     *            the value that should be converted
     * @param locale
     *            the locale of the given value
     * @return the non-localized formula
     */
    public String unFormatFormulaValue(String formulaValue, Locale locale) {
        if (formulaValue != null && getCurrentDecimalSeparator(locale) == ',') {
            List<FormulaToken> tokens = tokenizeFormula(formulaValue, locale);
            List<FormulaToken> unLocalizedTokens = unLocalizeTokens(tokens,
                    locale);
            return tokensToString(unLocalizedTokens);
        }

        return formulaValue;
    }

    /**
     * Convert from a non-localized format to a localized.
     *
     * @param formulaValue
     *            the value that should be converted
     * @param locale
     *            the target locale
     * @return the localized formula
     */
    public String reFormatFormulaValue(String formulaValue, Locale locale) {
        if (formulaValue != null && getCurrentDecimalSeparator(locale) == ',') {
            List<FormulaToken> tokens = tokenizeFormula(formulaValue, null);
            List<FormulaToken> localizedTokens = localizeTokens(tokens, locale);
            return tokensToString(localizedTokens);
        }

        return formulaValue;
    }

    public boolean isFormulaFormat(String value) {
        return value.startsWith("=") || value.startsWith("+");
    }

    /**
     * Rudimentary checks if the given string could be a valid formula
     *
     * @param value
     *            whole formula as a string, must start with '=' or '+'
     * @param locale
     *            the current locale
     * @return true if the formula could be valid
     */
    public boolean isValidFormulaFormat(String value, Locale locale) {
        if (isFormulaFormat(value)) {
            String formulaValue = value.substring(1);

            final List<FormulaToken> formulaTokens = tokenizeFormula(
                    formulaValue, locale);

            // check for unparsed '.' characters after numbers
            // e.g. "1.1" with decimal separator ',' should fail
            for (int i = 0; i < formulaTokens.size(); i++) {
                FormulaToken token = formulaTokens.get(i);
                if (token instanceof NumberToken
                        && i + 1 < formulaTokens.size()) {
                    FormulaToken nextToken = formulaTokens.get(i + 1);
                    if (".".equals(nextToken.toString())) {
                        return false;
                    }
                }
            }

            // no problems found in formula, pass
            return true;

        } else {
            // this isn't a valid formula to start with
            return false;
        }
    }

    /*
     * Loop through tokens and localize them as needed.
     */
    private List<FormulaToken> localizeTokens(List<FormulaToken> tokens,
            Locale locale) {
        List<FormulaToken> localizedTokens = new LinkedList<FormulaToken>();

        for (FormulaToken token : tokens) {
            if (token instanceof NumberToken) {
                try {
                    localizedTokens.add(new NumberToken(getDecimalFormat(locale)
                            .format(Double.parseDouble(token.toString()))));
                } catch (NumberFormatException e) {
                    LOGGER.info("ERROR parsing token " + token, e);
                    localizedTokens.add(token);
                }

            } else if (token instanceof SeparatorToken) {
                localizedTokens
                        .add(new SeparatorToken(getParameterSeparator(locale)));

            } else {
                localizedTokens.add(token);
            }
        }

        return localizedTokens;
    }

    protected String tokensToString(List<FormulaToken> tokens) {
        StringBuilder tokenString = new StringBuilder();
        for (FormulaToken token : tokens) {
            tokenString.append(token.toString());
        }

        return tokenString.toString();
    }

    /*
     * Loop through tokens and un-localize them as needed.
     */
    protected List<FormulaToken> unLocalizeTokens(List<FormulaToken> tokens,
            Locale locale) {
        List<FormulaToken> unlocalizedTokens = new LinkedList<FormulaToken>();

        for (FormulaToken token : tokens) {
            if (token instanceof NumberToken) {
                try {
                    unlocalizedTokens
                            .add(new NumberToken(getDecimalFormat(locale)
                                    .parse(token.toString()).toString()));
                } catch (ParseException e) {
                    LOGGER.info("ERROR parsing token: " + token, e);
                    unlocalizedTokens.add(token);
                }

            } else if (token instanceof SeparatorToken) {
                unlocalizedTokens
                        .add(new SeparatorToken(getParameterSeparator(null)));

            } else {
                unlocalizedTokens.add(token);
            }
        }

        return unlocalizedTokens;
    }

    /*
     * Go through the formula String and transform it to a list of tokens of the
     * correct type. Main goal is to figure out what are numbers and what are
     * argument separators.
     */
    protected List<FormulaToken> tokenizeFormula(String formulaValue,
            Locale from) {
        boolean inString = false;
        StringBuilder numberBuilder = new StringBuilder();
        List<FormulaToken> tokens = new LinkedList<FormulaToken>();
        for (int i = 0; i < formulaValue.length() + 1; i++) {
            Character current = i == formulaValue.length() ? null
                    : formulaValue.charAt(i);

            if (!isNumberChar(current, from) && numberBuilder.length() > 0) {
                tokens.add(new NumberToken(numberBuilder.toString()));
                numberBuilder = new StringBuilder();
            }

            if (current != null) {
                if (!inString && isNumberChar(current, from)) {
                    numberBuilder.append(current);

                } else if (current == getParameterSeparator(from)
                        && !inString) {
                    tokens.add(new SeparatorToken(current));

                } else if (current == '"') {
                    tokens.add(new FormulaToken('"'));
                    if (!inString) {
                        inString = true;

                    } else {
                        inString = false;
                    }
                } else {
                    tokens.add(new FormulaToken(current));
                }

            }
        }

        return tokens;
    }

    private char getParameterSeparator(Locale locale) {
        if (locale != null) {
            return ';';
        } else {
            return ',';
        }
    }

    /*
     * Check if the character is a number character in the given locale. Note
     * that grouping separators (e.g. for thousands) are not considered.
     */
    protected boolean isNumberChar(Character current, Locale locale) {
        return current != null && (Character.isDigit(current)
                || getCurrentDecimalSeparator(locale) == current);
    }

    protected DecimalFormat getDecimalFormat(Locale locale) {
        DecimalFormat instance = (DecimalFormat) DecimalFormat
                .getInstance(locale);
        instance.setGroupingUsed(false);
        return instance;
    }

    protected char getCurrentDecimalSeparator(Locale locale) {
        if (locale == null) {
            return '.';
        } else {
            DecimalFormat format = getDecimalFormat(locale);

            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
            return symbols.getDecimalSeparator();
        }
    }
}
