import dateFnsFormat from 'date-fns/format';
import dateFnsParse from 'date-fns/parse';
import dateFnsIsValid from 'date-fns/isValid';
import {
  createDate,
  extractDateParts,
  parseDate as _parseDate
} from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';

function createLocaleBasedDateFormat(locale) {
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

/**
 * datepickerConnector is a communication layer between DatePicker's flow
 * component (server-side) and web component (client-side).
 */
class DatePickerConnector {
  #datePicker;

  /** `'successful'`, `'error'`, or `undefined` when nothing was parsed since the overlay opened */
  #lastParseStatus;
  #lastParsedDate;

  // STABLE reference — created once per connector, never reassigned. Assigning a new function
  // to `dateMetadataProvider` clears the web component's cache and re-fetches every visible
  // range, so the same function object is reused for every update.
  #dateMetadataProvider = ({ start, end }) => {
    // The range bounds are ISO 8601 dates, which the server parses as they are.
    return this.#datePicker.$server.requestDateMetadata(start, end);
  };

  constructor(datePicker) {
    this.#datePicker = datePicker;

    datePicker.addEventListener('opened-changed', () => (this.#lastParseStatus = undefined));
  }

  updateI18n(locale, i18n) {
    // Either use custom formats specified in I18N, or create format from locale
    const hasCustomFormats = i18n && i18n.dateFormats && i18n.dateFormats.length > 0;
    if (i18n && i18n.referenceDate) {
      i18n.referenceDate = extractDateParts(new Date(i18n.referenceDate));
    }
    const usedFormats = hasCustomFormats ? i18n.dateFormats : [createLocaleBasedDateFormat(locale)];

    // Merge new I18N settings with formatting and parsing functions
    this.#datePicker.i18n = Object.assign({}, i18n, this.#createFormatterAndParser(usedFormats));
  }

  setDateMetadataConfig(config) {
    // Keys are `year-month-day` with a 0-based month, matching what the web component
    // passes to `isDateDisabled`, so nothing has to be parsed or converted per date.
    const disabledDates = new Set((config.disabledDates || []).map(([y, m, d]) => `${y}-${m}-${d}`));
    // ISO weekday numbers, 1..7.
    const disabledWeekdays = new Set(config.disabledWeekdays || []);
    const hasDisabledDates = disabledDates.size > 0;
    const hasDisabledWeekdays = disabledWeekdays.size > 0;

    this.#datePicker.isDateDisabled =
      !hasDisabledDates && !hasDisabledWeekdays
        ? undefined
        : ({ year, month, day }) =>
            (hasDisabledDates && disabledDates.has(`${year}-${month}-${day}`)) ||
            (hasDisabledWeekdays && disabledWeekdays.has(createDate(year, month, day).getDay() || 7));

    this.#datePicker.dateMetadataProvider = config.hasProvider ? this.#dateMetadataProvider : null;
  }

  /**
   * Creates the `formatDate` and `parseDate` functions the web component's i18n
   * object expects, bound to the given date formats.
   */
  #createFormatterAndParser(formats) {
    if (!formats || formats.length === 0) {
      throw new Error('Array of custom date formats is null or empty');
    }

    return {
      formatDate: (dateParts) => this.#formatDate(dateParts, formats[0]),
      parseDate: (dateString) => this.#parseDate(dateString, formats)
    };
  }

  #formatDate(dateParts, format) {
    const date = _parseDate(`${dateParts.year}-${dateParts.month + 1}-${dateParts.day}`);

    return dateFnsFormat(date, format);
  }

  #parseDate(dateString, formats) {
    const referenceDate = this.#getReferenceDate();
    for (const format of getExtendedFormats(formats)) {
      const parsedDate = this.#doParseDate(dateString, format, referenceDate);
      if (parsedDate) {
        this.#lastParseStatus = 'successful';
        this.#lastParsedDate = parsedDate;
        return parsedDate;
      }
    }
    this.#lastParseStatus = 'error';
    return false;
  }

  #doParseDate(dateString, format, referenceDate) {
    // When format does not contain a year, then current year should be used.
    const refDate = isFormatWithYear(format) ? referenceDate : new Date();
    const date = dateFnsParse(dateString, format, refDate);
    if (dateFnsIsValid(date)) {
      if (isFormatWithYear(format) && isShortYearFormat(format)) {
        this.#correctFullYear(date);
      }
      return {
        day: date.getDate(),
        month: date.getMonth(),
        year: date.getFullYear()
      };
    }
  }

  #correctFullYear(date) {
    // The last parsed date check handles the case where a four-digit year is parsed, then formatted
    // as a two-digit year, and then parsed again. In this case we want to keep the century of the
    // originally parsed year, instead of using the century of the reference date.

    // Do not apply any correction if the previous parse attempt was failed.
    if (this.#lastParseStatus === 'error') {
      return;
    }

    // Update century if the last parsed date is the same except the century.
    if (this.#lastParseStatus === 'successful') {
      if (
        this.#lastParsedDate.day === date.getDate() &&
        this.#lastParsedDate.month === date.getMonth() &&
        this.#lastParsedDate.year % 100 === date.getFullYear() % 100
      ) {
        date.setFullYear(this.#lastParsedDate.year);
      }
      return;
    }

    // Update century if this is the first parse after overlay open.
    const currentValue = _parseDate(this.#datePicker.value);
    if (
      dateFnsIsValid(currentValue) &&
      currentValue.getDate() === date.getDate() &&
      currentValue.getMonth() === date.getMonth() &&
      currentValue.getFullYear() % 100 === date.getFullYear() % 100
    ) {
      date.setFullYear(currentValue.getFullYear());
    }
  }

  #getReferenceDate() {
    const { referenceDate } = this.#datePicker.i18n;
    return referenceDate ? new Date(referenceDate.year, referenceDate.month, referenceDate.day) : new Date();
  }
}

function initLazy(datePicker) {
  // Init the connector only once for the date picker
  datePicker.$connector ??= new DatePickerConnector(datePicker);
}

window.Vaadin.Flow.datepickerConnector = { initLazy };
