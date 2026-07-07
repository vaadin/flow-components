import dateFnsFormat from 'date-fns/format';
import dateFnsParse from 'date-fns/parse';
import dateFnsIsValid from 'date-fns/isValid';
import { extractDateParts, parseDate as _parseDate } from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';

window.Vaadin.Flow.datepickerConnector = {};
window.Vaadin.Flow.datepickerConnector.initLazy = (datepicker) => {
  // Check whether the connector was already initialized for the datepicker
  if (datepicker.$connector) {
    return;
  }

  datepicker.$connector = {};

  const createLocaleBasedDateFormat = function (locale) {
    try {
      // Check whether the locale is supported or not
      new Date().toLocaleDateString(locale);
    } catch (e) {
      console.warn('The locale is not supported, using default format setting (ISO 8601).');
      return 'yyyy-MM-dd';
    }

    // format test date and convert to date-fns pattern
    const testDate = new Date(Date.UTC(1234, 4, 6));
    let pattern = testDate.toLocaleDateString(locale, { timeZone: 'UTC' });
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
    const isValidPattern = pattern.includes('d') && pattern.includes('M') && pattern.includes('y');
    if (!isValidPattern) {
      console.warn('The locale is not supported, using default format setting (ISO 8601).');
      return 'yyyy-MM-dd';
    }

    return pattern;
  };

  function createFormatterAndParser(formats) {
    if (!formats || formats.length === 0) {
      throw new Error('Array of custom date formats is null or empty');
    }

    function getShortYearFormat(format) {
      if (format.includes('yyyy') && !format.includes('yyyyy')) {
        return format.replace('yyyy', 'yy');
      }
      if (format.includes('YYYY') && !format.includes('YYYYY')) {
        return format.replace('YYYY', 'YY');
      }
      return undefined;
    }

    function isFormatWithYear(format) {
      return format.includes('y') || format.includes('Y');
    }

    function isShortYearFormat(format) {
      // Format is long if it includes a four-digit year.
      return !format.includes('yyyy') && !format.includes('YYYY');
    }

    function getExtendedFormats(formats) {
      return formats.reduce((acc, format) => {
        // We first try to match the date with the shorter version,
        // as short years are supported with the long date format.
        if (isFormatWithYear(format) && !isShortYearFormat(format)) {
          acc.push(getShortYearFormat(format));
        }
        acc.push(format);
        return acc;
      }, []);
    }

    function correctFullYear(date) {
      // The last parsed date check handles the case where a four-digit year is parsed, then formatted
      // as a two-digit year, and then parsed again. In this case we want to keep the century of the
      // originally parsed year, instead of using the century of the reference date.

      // Do not apply any correction if the previous parse attempt was failed.
      if (datepicker.$connector._lastParseStatus === 'error') {
        return;
      }

      // Update century if the last parsed date is the same except the century.
      if (datepicker.$connector._lastParseStatus === 'successful') {
        if (
          datepicker.$connector._lastParsedDate.day === date.getDate() &&
          datepicker.$connector._lastParsedDate.month === date.getMonth() &&
          datepicker.$connector._lastParsedDate.year % 100 === date.getFullYear() % 100
        ) {
          date.setFullYear(datepicker.$connector._lastParsedDate.year);
        }
        return;
      }

      // Update century if this is the first parse after overlay open.
      const currentValue = _parseDate(datepicker.value);
      if (
        dateFnsIsValid(currentValue) &&
        currentValue.getDate() === date.getDate() &&
        currentValue.getMonth() === date.getMonth() &&
        currentValue.getFullYear() % 100 === date.getFullYear() % 100
      ) {
        date.setFullYear(currentValue.getFullYear());
      }
    }

    function formatDate(dateParts) {
      const format = formats[0];
      const date = _parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);

      return dateFnsFormat(date, format);
    }

    function doParseDate(dateString, format, referenceDate) {
      // When format does not contain a year, then current year should be used.
      const refDate = isFormatWithYear(format) ? referenceDate : new Date();
      const date = dateFnsParse(dateString, format, refDate);
      if (dateFnsIsValid(date)) {
        if (isFormatWithYear(format) && isShortYearFormat(format)) {
          correctFullYear(date);
        }
        return {
          day: date.getDate(),
          month: date.getMonth(),
          year: date.getFullYear()
        };
      }
    }

    function parseDate(dateString) {
      const referenceDate = _getReferenceDate();
      for (let format of getExtendedFormats(formats)) {
        const parsedDate = doParseDate(dateString, format, referenceDate);
        if (parsedDate) {
          datepicker.$connector._lastParseStatus = 'successful';
          datepicker.$connector._lastParsedDate = parsedDate;
          return parsedDate;
        }
      }
      datepicker.$connector._lastParseStatus = 'error';
      return false;
    }

    return {
      formatDate: formatDate,
      parseDate: parseDate
    };
  }

  function _getReferenceDate() {
    const { referenceDate } = datepicker.i18n;
    return referenceDate ? new Date(referenceDate.year, referenceDate.month, referenceDate.day) : new Date();
  }

  // Disabled dates state, populated from the server through
  // `setDisabledDatesConfig`. Keys use the `year-month-day` (0-based month)
  // format also produced while iterating a range below.
  let disabledDatesSet = new Set();
  let disabledWeekdaysSet = new Set();
  let hasServerProvider = false;
  let disabledDatesRequestId = 0;
  const pendingDisabledDatesRequests = new Map();

  function dateKey(year, month, day) {
    return `${year}-${month}-${day}`;
  }

  function isoToDateParts(iso) {
    return extractDateParts(_parseDate(iso));
  }

  // Returns the statically disabled dates (fixed list + weekdays) within the
  // given range as an array of `DatePickerDate` objects.
  function computeStaticDisabledDates({ start, end }) {
    const result = [];
    const date = new Date(start.year, start.month, start.day);
    const last = new Date(end.year, end.month, end.day);
    while (date <= last) {
      const isoWeekday = date.getDay() === 0 ? 7 : date.getDay();
      if (
        disabledDatesSet.has(dateKey(date.getFullYear(), date.getMonth(), date.getDate())) ||
        disabledWeekdaysSet.has(isoWeekday)
      ) {
        result.push({ year: date.getFullYear(), month: date.getMonth(), day: date.getDate() });
      }
      date.setDate(date.getDate() + 1);
    }
    return result;
  }

  function updateDisabledDatesProvider() {
    if (disabledDatesSet.size === 0 && disabledWeekdaysSet.size === 0 && !hasServerProvider) {
      datepicker.disabledDatesProvider = undefined;
      return;
    }

    datepicker.disabledDatesProvider = (range) => {
      const staticDisabled = computeStaticDisabledDates(range);
      if (!hasServerProvider) {
        return staticDisabled;
      }

      // Ask the server for the range and block rendering until it responds.
      const requestId = ++disabledDatesRequestId;
      return new Promise((resolve) => {
        pendingDisabledDatesRequests.set(requestId, (serverDates) => {
          resolve(staticDisabled.concat(serverDates.map(isoToDateParts)));
        });
        const pad = (value, length) => String(value).padStart(length, '0');
        const toIso = (parts) => `${pad(parts.year, 4)}-${pad(parts.month + 1, 2)}-${pad(parts.day, 2)}`;
        datepicker.$server.requestDisabledDates(toIso(range.start), toIso(range.end), requestId);
      });
    };
  }

  datepicker.$connector.setDisabledDatesConfig = (config) => {
    disabledDatesSet = new Set(
      (config.dates || []).map((iso) => {
        const parts = isoToDateParts(iso);
        return dateKey(parts.year, parts.month, parts.day);
      })
    );
    disabledWeekdaysSet = new Set(config.weekdays || []);
    hasServerProvider = !!config.hasProvider;
    updateDisabledDatesProvider();
  };

  datepicker.$connector.resolveDisabledDates = (requestId, dates) => {
    const resolve = pendingDisabledDatesRequests.get(requestId);
    if (resolve) {
      pendingDisabledDatesRequests.delete(requestId);
      resolve(dates);
    }
  };

  datepicker.$connector.updateI18n = (locale, i18n) => {
    // Either use custom formats specified in I18N, or create format from locale
    const hasCustomFormats = i18n && i18n.dateFormats && i18n.dateFormats.length > 0;
    if (i18n && i18n.referenceDate) {
      i18n.referenceDate = extractDateParts(new Date(i18n.referenceDate));
    }
    const usedFormats = hasCustomFormats ? i18n.dateFormats : [createLocaleBasedDateFormat(locale)];
    const formatterAndParser = createFormatterAndParser(usedFormats);

    // Merge new I18N settings with formatting and parsing functions
    datepicker.i18n = Object.assign({}, i18n, formatterAndParser);
  };

  datepicker.addEventListener('opened-changed', () => (datepicker.$connector._lastParseStatus = undefined));
};
