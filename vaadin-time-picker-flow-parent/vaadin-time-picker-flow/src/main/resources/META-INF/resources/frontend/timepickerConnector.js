(function () {
    const tryCatchWrapper = function (callback) {
        return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Time Picker', 'vaadin-time-picker');
    };

    // Execute callback when predicate returns true.
    // Try again later if predicate returns false.
    function when(predicate, callback, timeout = 0) {
        if (predicate()) {
            callback();
        } else {
            setTimeout(() => when(predicate, callback, 200), timeout);
        }
    }

    window.Vaadin.Flow.timepickerConnector = {
        initLazy: timepicker => tryCatchWrapper(function (timepicker) {
            // Check whether the connector was already initialized for the timepicker
            if (timepicker.$connector) {
                return;
            }

            timepicker.$connector = {};

            const getAmPmString = function (locale, testTime) {
                const testTimeString = testTime.toLocaleTimeString(locale);
                // AM/PM string is anything from one letter in eastern arabic to standard two letters,
                // to having space in between, dots ...
                // cannot disqualify whitespace since some locales use a. m. / p. m.
                // TODO when more scripts support is added (than Arabic), need to exclude those numbers too
                const endWithAmPmRegex = /[^\d\u0660-\u0669]+$/g;
                let amPmString = testTimeString.match(endWithAmPmRegex);
                if (!amPmString) {
                    // eg. chinese (and some else too) starts with am/pm
                    amPmString = testTimeString.match(/^[^\d\u0660-\u0669]+/g);
                }
                if (amPmString) {
                    amPmString = amPmString[0].trim();
                }
                return amPmString;
            };
            const testPmTime = new Date('August 19, 1975 23:15:30');
            const testAmTime = new Date('August 19, 1975 05:15:30');

            const getPmString = function (locale) {
                return getAmPmString(locale, testPmTime);

            };
            const getAmString = function (locale) {
                return getAmPmString(locale, testAmTime);
            };

            // map from unicode eastern arabic number characters to arabic numbers
            const arabicDigitMap = {
                '\\u0660': '0',
                '\\u0661': '1',
                '\\u0662': '2',
                '\\u0663': '3',
                '\\u0664': '4',
                '\\u0665': '5',
                '\\u0666': '6',
                '\\u0667': '7',
                '\\u0668': '8',
                '\\u0669': '9'
            };

            // parses eastern arabic number characters to arabic numbers (0-9)
            const anyNumberCharToArabicNumberReplacer = function (charsToReplace) {
                return charsToReplace.replace(/[\u0660-\u0669]/g, function (char) {
                    const unicode = '\\u0' + char.charCodeAt(0).toString(16);
                    return arabicDigitMap[unicode];
                });
            };

            const parseAnyCharsToInt = function (anyNumberChars) {
                return parseInt(anyNumberCharToArabicNumberReplacer(anyNumberChars));
            };

            const parseMillisecondCharsToInt =function (millisecondChars) {
                millisecondChars = anyNumberCharToArabicNumberReplacer(millisecondChars);
                // digits are either .1 .01 or .001 so need to "shift"
                if (millisecondChars.length === 1) {
                    millisecondChars += "00";
                } else if (millisecondChars.length === 2) {
                    millisecondChars += "0";
                }
                return parseInt(millisecondChars);
            };

            // detecting milliseconds from input, expects am/pm removed from end, eg. .0 or .00 or .000
            const millisecondRegExp = /[[\.][\d\u0660-\u0669]{1,3}$/;

            timepicker.$connector.setLocale = tryCatchWrapper(function (locale) {
                // capture previous value if any
                let previousValueObject;
                if (timepicker.value && timepicker.value !== '') {
                    previousValueObject = timepicker.i18n.parseTime(timepicker.value);
                }

                try {
                    // Check whether the locale is supported by the browser or not
                    testPmTime.toLocaleTimeString(locale);
                } catch (e) {
                    locale = "en-US";
                    // FIXME should do a callback for server to throw an exception ?
                    throw new Error("vaadin-time-picker: The locale " + locale + " is not supported, falling back to default locale setting(en-US).");
                }

                // 1. 24 or 12 hour clock, if latter then what are the am/pm strings ?
                const pmString = getPmString(locale);
                const amString = getAmString(locale);

                // 2. What is the separator ?
                let localeTimeString = testPmTime.toLocaleTimeString(locale);
                // since the next regex picks first non-number-whitespace, need to discard possible PM from beginning (eg. chinese locale)
                if (pmString && localeTimeString.startsWith(pmString)) {
                    localeTimeString = localeTimeString.replace(pmString, '');
                }
                const separator = localeTimeString.match(/[^\u0660-\u0669\s\d]/);

                // 3. regexp that allows to find the numbers with optional separator and continuing searching after it
                const numbersRegExp = new RegExp('([\\d\\u0660-\\u0669]){1,2}(?:' + separator + ')?', 'g');

                const includeSeconds = function () {
                    return timepicker.step && timepicker.step < 60;
                };

                const includeMilliSeconds = function () {
                    return timepicker.step && timepicker.step < 1;
                };

                // the web component expects the correct granularity used for the time string,
                // thus need to format the time object in correct granularity by passing the format options
                let cachedStep;
                let cachedOptions;
                const getTimeFormatOptions = function () {
                    // calculate the format options if none done cached or step has changed
                    if (!cachedOptions || cachedStep !== timepicker.step) {
                        cachedOptions = {
                            hour: "numeric",
                            minute: "numeric",
                            second: includeSeconds() ? "numeric" : undefined,
                        };
                        cachedStep = timepicker.step;
                    }
                    return cachedOptions;
                };

                const formatMilliseconds = function (localeTimeString, milliseconds) {
                    if (includeMilliSeconds()) {
                        // might need to inject milliseconds between seconds and AM/PM
                        let cleanedTimeString = localeTimeString;
                        if (localeTimeString.endsWith(amString)) {
                            cleanedTimeString = localeTimeString.replace(" " + amString, '');
                        } else if (localeTimeString.endsWith(pmString)) {
                            cleanedTimeString = localeTimeString.replace(" " + pmString, '');
                        }
                        if (milliseconds) {
                            let millisecondsString = milliseconds < 10 ? "0" : "";
                            millisecondsString += milliseconds < 100 ? "0" : "";
                            millisecondsString += milliseconds;
                            cleanedTimeString += "." + millisecondsString;
                        } else {
                            cleanedTimeString += ".000";
                        }
                        if (localeTimeString.endsWith(amString)) {
                            cleanedTimeString = cleanedTimeString + " " + amString;
                        } else if (localeTimeString.endsWith(pmString)) {
                            cleanedTimeString = cleanedTimeString + " " + pmString;
                        }
                        return cleanedTimeString;
                    }
                    return localeTimeString;
                };

                let cachedTimeString;
                let cachedTimeObject;

                timepicker.i18n = {
                    formatTime: tryCatchWrapper(function (timeObject) {
                        if (timeObject) {
                            let timeToBeFormatted = new Date();
                            timeToBeFormatted.setHours(timeObject.hours);
                            timeToBeFormatted.setMinutes(timeObject.minutes);
                            timeToBeFormatted.setSeconds(timeObject.seconds !== undefined ? timeObject.seconds : 0);
                            let localeTimeString = timeToBeFormatted.toLocaleTimeString(locale, getTimeFormatOptions());
                            // milliseconds not part of the time format API
                            localeTimeString = formatMilliseconds(localeTimeString, timeObject.milliseconds);
                            return localeTimeString;
                        }
                    }),
                    parseTime: tryCatchWrapper(function (timeString) {
                        if (timeString && timeString === cachedTimeString && cachedTimeObject) {
                            return cachedTimeObject;
                        }
                        if (timeString) {
                            const pm = timeString.search(pmString);
                            const am = timeString.search(amString);
                            let numbersOnlyTimeString = timeString.replace(amString, '').replace(pmString, '').trim();
                            // reset regex to beginning or things can explode when the length of the input changes
                            numbersRegExp.lastIndex = 0;
                            let hours = numbersRegExp.exec(numbersOnlyTimeString);
                            if (hours) {
                                hours = parseAnyCharsToInt(hours[0].replace(separator, ''));
                                // handle 12 am -> 0
                                // do not do anything if am & pm are not used or if those are the same,
                                // as with locale bg-BG there is always ч. at the end of the time
                                if (pm !== am) {
                                    if (hours === 12 && am !== -1) {
                                        hours = 0;
                                    } else {
                                        hours += (pm !== -1 && hours !== 12 ? 12 : 0)
                                    }
                                }
                                const minutes = numbersRegExp.exec(numbersOnlyTimeString);
                                const seconds = minutes && numbersRegExp.exec(numbersOnlyTimeString);
                                // reset to end or things can explode
                                let milliseconds = seconds && includeMilliSeconds() && millisecondRegExp.exec(numbersOnlyTimeString);
                                // handle case where last numbers are seconds and . is the separator (invalid regexp match)
                                if (milliseconds && milliseconds['index'] <= seconds['index']) {
                                    milliseconds = undefined;
                                }
                                // hours is a number at this point, others are either arrays or null
                                // the string in [0] from the arrays includes the separator too
                                cachedTimeObject = hours !== undefined && {
                                    hours: hours,
                                    minutes: minutes ? parseAnyCharsToInt(minutes[0].replace(separator, '')) : 0,
                                    seconds: seconds ? parseAnyCharsToInt(seconds[0].replace(separator, '')) : 0,
                                    milliseconds: minutes && seconds && milliseconds ? parseMillisecondCharsToInt(milliseconds[0].replace('.', '')) : 0
                                };
                                cachedTimeString = timeString;
                                return cachedTimeObject;
                            }
                            // when nothing is returned, the component shows the invalid state for the input
                        }
                    })
                };

                if (previousValueObject) {
                    when(() => timepicker.$, () => {
                        const newValue = timepicker.i18n.formatTime(previousValueObject);
                        // FIXME works but uses private API, needs fixes in web component
                        if (timepicker.inputElement.value !== newValue) {
                            timepicker.inputElement.value = newValue;
                            timepicker.$.comboBox.value = newValue;
                        }
                    });
                }
            });
        })(timepicker)
    };
})();
