window.Vaadin.Flow.timepickerConnector = {
    initLazy: function (timepicker) {
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

        // map from unicode arabic characters to numbers
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
        const parseAnyCharsToInt = function (arabicDigit) {
            return parseInt(arabicDigit.replace(/[\u0660-\u0669]/g, function (char) {
                const unicode = '\\u0' + char.charCodeAt(0).toString(16);
                return arabicDigitMap[unicode];
            }));
        };

        timepicker.$connector.setLocale = function (locale) {
            // capture previous value if any
            let previousValueObject;
            if (timepicker.value && timepicker.value !== '') {
                previousValueObject = timepicker.i18n.parseTime(timepicker.value);
            }

            try {
                // Check whether the locale is supported by the browser or not
                console.log(locale.toUpperCase() + ": " + testPmTime.toLocaleTimeString(locale));
            } catch (e) {
                locale = "en-US";
                // FIXME should do a callback for server to throw an exception ?
                console.error("vaadin-time-picker: The locale " + locale + " is not supported, falling back to default locale setting(en-US).");
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
            //console.info(locale.toUpperCase() + " AM/PM: " + amString + "/" + pmString + ", separator " + separator);

            // 3. regexp that allows to find the numberd and continuing searching after it
            // TODO milliseconds have their own separator, and could be handled separately
            const numbersRegExp = new RegExp('([\\d\\u0660-\\u0669]){1,2}(?:' + separator + ')?', 'g');

            const includeSeconds = function () {
                return timepicker.step && timepicker.step < 60;
            };

            const includeMilliSeconds = function () {
                return timepicker.step && timepicker.step < 1;
            };

            // the web component expects the correct granularity used for the time string,
            // thus need to format the time object in correct granularity by passing the format options
            let cachedStep = timepicker.step;
            let cachedOptions;
            const getTimeFormatOptions = function () {
                if (!cachedOptions || cachedStep && timepicker.step && cachedStep !== timepicker.step) {
                    cachedOptions = {
                        hour: "numeric",
                        minute: "numeric",
                        second: includeSeconds() ? "numeric" : undefined,
                        milliseconds: includeMilliSeconds() ? "numeric" : undefined
                    };
                }
                return cachedOptions;
            };

            timepicker.i18n = {
                formatTime: function (timeObject) {
                    if (timeObject) {
                        let time = new Date();
                        time.setHours(timeObject.hours);
                        time.setMinutes(timeObject.minutes);
                        time.setSeconds(timeObject.seconds !== undefined ? timeObject.seconds : 0);
                        time.setMilliseconds(timeObject.milliseconds !== undefined ? timeObject.milliseconds : 0);
                        return time.toLocaleTimeString(locale, getTimeFormatOptions());
                    }
                },
                parseTime: function (timeString) {
                    if (timeString) {
                        const pm = timeString.search(pmString);
                        const am = timeString.search(amString);
                        const numbersOnlyTimeString = timeString.replace(amString, '').replace(pmString, '').trim();
                        let hours = numbersRegExp.exec(numbersOnlyTimeString);
                        if (hours) {
                            hours = parseAnyCharsToInt(hours[0].replace(separator, ''));
                            // handle 12 am -> 0
                            // do not do anything if am & pm are not used or if those are the same,
                            // as with locale bg-BG there is always  ч. at the end of the time
                            if (pm !== am) {
                                if (hours === 12 && am !== -1) {
                                    hours = 0;
                                } else {
                                    hours += (pm !== -1 && hours !== 12 ? 12 : 0)
                                }
                            }
                            const minutes = numbersRegExp.exec(numbersOnlyTimeString);
                            const seconds = minutes && numbersRegExp.exec(numbersOnlyTimeString);
                            // TODO milliseconds has its own separator
                            const milliseconds = seconds && numbersRegExp.exec(numbersOnlyTimeString);
                            // hours is a number at this point, others are either arrays or null
                            // the string in [0] from the arrays includes the separator too
                            return hours !== undefined && {
                                hours: hours,
                                minutes: minutes ? parseAnyCharsToInt(minutes[0].replace(separator, '')) : 0,
                                seconds: seconds ? parseAnyCharsToInt(seconds[0].replace(separator, '')) : 0,
                                milliseconds: milliseconds ? parseAnyCharsToInt(milliseconds[0].replace(separator, '')) : 0
                            };
                        }
                        // when nothing is returned, the component shows the invalid state for the input
                    }
                }
            };

            if (previousValueObject) {
                const newValue = timepicker.i18n.formatTime(previousValueObject);
                // FIXME works but uses private API, needs fixes in web component
                if (timepicker.__inputElement.value !== newValue) {
                    timepicker.__inputElement.value = newValue;
                    timepicker.__dropdownElement.value = newValue;
                    timepicker.__onInputChange();
                }
            }
        }
    }
};
