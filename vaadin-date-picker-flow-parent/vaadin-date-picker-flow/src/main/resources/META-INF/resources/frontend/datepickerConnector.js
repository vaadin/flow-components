(function () {
    const tryCatchWrapper = function (callback) {
        return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Date Picker', 'vaadin-date-picker-flow');
    };

    // Duplicated from the web component's helpers module as it's
    // easier than dealing with imports for both NPM and bower mode
    const vaadinDatePickerHelpers = {
        /**
         * Extracts the basic component parts of a date (day, month and year)
         * to the expected format.
         * @param {!Date} date
         * @return {{day: number, month: number, year: number}}
         */
        extractDateParts(date) {
            return {
                day: date.getDate(),
                month: date.getMonth(),
                year: date.getFullYear(),
            };
        },
        /**
         * Parse date string of one of the following date formats:
         * - ISO 8601 `"YYYY-MM-DD"`
         * - 6-digit extended ISO 8601 `"+YYYYYY-MM-DD"`, `"-YYYYYY-MM-DD"`
         * @param {!string} str Date string to parse
         * @return {Date} Parsed date
         */
        parseDate(str) {
            // Parsing with RegExp to ensure correct format
            const parts = /^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/.exec(str);
            if (!parts) {
                return undefined;
            }

            const date = new Date(0, 0); // Wrong date (1900-01-01), but with midnight in local time
            date.setFullYear(parseInt(parts[1], 10));
            date.setMonth(parseInt(parts[2], 10) - 1);
            date.setDate(parseInt(parts[3], 10));
            return date;
        }
    }

    window.Vaadin.Flow.datepickerConnector = {
        initLazy: (datepicker) =>
            tryCatchWrapper(function (datepicker) {
                // Check whether the connector was already initialized for the datepicker
                if (datepicker.$connector) {
                    return;
                }

                datepicker.$connector = {};

                datepicker.addEventListener(
                    'blur',
                    tryCatchWrapper((e) => {
                        if (!e.target.value && e.target.invalid) {
                            console.warn('Invalid value in the DatePicker.');
                        }
                    })
                );

                const dateFns = window.Vaadin.Flow.datepickerDateFns;
                if (!dateFns) {
                    throw new Error("Custom date-fns bundle for date picker is not registered at window.Vaadin.Flow.datepickerDateFns");
                }

                const createLocaleBasedDateFormat = function (locale) {
                    try {
                        // Check whether the locale is supported or not
                        new Date().toLocaleDateString(locale);
                    } catch (e) {
                        console.warn('The locale is not supported, using default locale setting(en-US).');
                        return 'M/d/yyyy';
                    }

                    // format test date and convert to date-fns pattern
                    const testDate = new Date(Date.UTC(1234, 4, 6));
                    let pattern = testDate.toLocaleDateString(locale, {timeZone: 'UTC'});
                    pattern = pattern
                        // escape date-fns pattern letters by enclosing them in single quotes
                        .replace(/([a-zA-Z]+)/g, "'$1'")
                        // insert date placeholder
                        .replace('06', 'dd')
                        .replace('6', 'd')
                        // insert month placeholder
                        .replace('05', 'MM')
                        .replace('5', 'M')
                        // insert year placeholder
                        .replace('1234', 'yyyy');
                    const isValidPattern = pattern.indexOf('d') >= 0 && pattern.indexOf('M') >= 0 && pattern.indexOf('y') >= 0;
                    if (!isValidPattern) {
                        console.warn('The locale is not supported, using default locale setting(en-US).');
                        return 'M/d/yyyy';
                    }

                    return pattern;
                };

                const createFormatterAndParser = tryCatchWrapper(function (formats) {
                    if (!formats || formats.length === 0) {
                        throw new Error('Array of custom date formats is null or empty');
                    }

                    function getShortYearFormat(format) {
                        if (format.indexOf('yyyy') >= 0 && format.indexOf('yyyyy') < 0) {
                            return format.replace('yyyy', 'yy');
                        }
                        if (format.indexOf('YYYY') >= 0 && format.indexOf('YYYYY') < 0) {
                            return format.replace('YYYY', 'YY');
                        }
                        return undefined;
                    }

                    function isShortYearFormat(format) {
                        if (format.indexOf('y') >= 0) {
                            return format.indexOf('yyy') < 0;
                        }
                        if (format.indexOf('Y') >= 0) {
                            return format.indexOf('YYY') < 0;
                        }
                        return false;
                    }

                    function formatDate(dateParts) {
                        const format = formats[0];
                        const date = vaadinDatePickerHelpers.parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);

                        return dateFns.format(date, format);
                    }

                    function parseDate(dateString) {
                        const referenceDate = _getReferenceDate();
                        for (let format of formats) {
                            // We first try to match the date with the shorter version.
                            const shortYearFormat = getShortYearFormat(format);
                            if (shortYearFormat) {
                                const shortYearFormatDate = dateFns.parse(dateString, shortYearFormat, referenceDate);
                                if (dateFns.isValid(shortYearFormatDate)) {
                                    let yearValue = shortYearFormatDate.getFullYear();
                                    // The last parsed year check handles the case where a four-digit year is parsed, then formatted
                                    // as a two-digit year, and then parsed again. In this case we want to keep the century of the
                                    // originally parsed year, instead of using the century of the reference date.
                                    if (
                                        datepicker.$connector._lastParsedYear &&
                                        yearValue === datepicker.$connector._lastParsedYear % 100
                                    ) {
                                        yearValue = datepicker.$connector._lastParsedYear;
                                    }
                                    return {
                                        day: shortYearFormatDate.getDate(),
                                        month: shortYearFormatDate.getMonth(),
                                        year: yearValue
                                    };
                                }
                            }
                            const date = dateFns.parse(dateString, format, referenceDate);

                            if (dateFns.isValid(date)) {
                                let yearValue = date.getFullYear();
                                if (
                                    datepicker.$connector._lastParsedYear &&
                                    yearValue % 100 === datepicker.$connector._lastParsedYear % 100 &&
                                    isShortYearFormat(format)
                                ) {
                                    yearValue = datepicker.$connector._lastParsedYear;
                                } else {
                                    datepicker.$connector._lastParsedYear = yearValue;
                                }
                                return {
                                    day: date.getDate(),
                                    month: date.getMonth(),
                                    year: yearValue
                                };
                            }
                        }
                        datepicker.$connector._lastParsedYear = undefined;
                        return false;
                    }

                    return {
                        formatDate: formatDate,
                        parseDate: parseDate
                    };
                });

                function _getReferenceDate() {
                    const {referenceDate} = datepicker.i18n;
                    return referenceDate ? new Date(referenceDate.year, referenceDate.month, referenceDate.day) : new Date();
                }

                datepicker.$connector.updateI18n = tryCatchWrapper(function (locale, i18n) {
                    // Either use custom formats specified in I18N, or create format from locale
                    const hasCustomFormats = i18n && i18n.dateFormats && i18n.dateFormats.length > 0;
                    if (i18n && i18n.referenceDate) {
                        i18n.referenceDate = vaadinDatePickerHelpers.extractDateParts(new Date(i18n.referenceDate));
                    }
                    const usedFormats = hasCustomFormats ? i18n.dateFormats : [createLocaleBasedDateFormat(locale)];
                    const formatterAndParser = createFormatterAndParser(usedFormats);

                    // Merge current web component I18N settings with new I18N settings and the formatting and parsing functions
                    datepicker.i18n = Object.assign({}, datepicker.i18n, i18n, formatterAndParser);
                });
            })(datepicker)
    };
})();
