import dateFnsFormat from 'date-fns/format';
import dateFnsParse from 'date-fns/parse';
import dateFnsIsValid from 'date-fns/isValid';
import {
  createDate,
  extractDateParts,
  parseDate as _parseDate
} from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';

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

  // STABLE reference — created once per connector, never reassigned. Assigning a new function
  // to `dateMetadataProvider` clears the web component's cache and re-fetches every visible
  // range, so the same function object is reused for every update.
  const dateMetadataProvider = ({ start, end }) => {
    // The range bounds are ISO 8601 dates, which the server parses as they are.
    return datepicker.$server.requestDateMetadata(start, end);
  };

  datepicker.$connector.setDateMetadataConfig = (config) => {
    // Keys are `year-month-day` with a 0-based month, matching what the web component
    // passes to `isDateDisabled`, so nothing has to be parsed or converted per date.
    const disabledDates = new Set((config.disabledDates || []).map(([y, m, d]) => `${y}-${m}-${d}`));
    // ISO weekday numbers, 1..7.
    const disabledWeekdays = new Set(config.disabledWeekdays || []);
    const hasDisabledDates = disabledDates.size > 0;
    const hasDisabledWeekdays = disabledWeekdays.size > 0;

    datepicker.isDateDisabled =
      !hasDisabledDates && !hasDisabledWeekdays
        ? undefined
        : ({ year, month, day }) =>
            (hasDisabledDates && disabledDates.has(`${year}-${month}-${day}`)) ||
            (hasDisabledWeekdays && disabledWeekdays.has(createDate(year, month, day).getDay() || 7));

    datepicker.dateMetadataProvider = config.hasProvider ? dateMetadataProvider : null;
  };

  datepicker.addEventListener('opened-changed', () => (datepicker.$connector._lastParseStatus = undefined));
};
