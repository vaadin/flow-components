import { format as dateFnsFormat } from 'date-fns/format';
import { parse as dateFnsParse } from 'date-fns/parse';
import { isValid as dateFnsIsValid } from 'date-fns/isValid';
import {
  TEST_PM_TIME,
  escapeRegExp,
  formatMilliseconds,
  parseMillisecondsIntoInteger,
  parseDigitsIntoInteger,
  getAmString,
  getPmString,
  getSeparator,
  searchAmOrPmToken
} from './helpers.ts';
import type { FlowTimePicker, FlowTimePickerServerI18n, FlowTimePickerTime } from './vaadin-time-picker-types.js';

// The day custom formats format and parse on, fixed to avoid DST shifts
const REFERENCE_DATE = new Date(1970, 0, 1);

// Probe times for the round-trip check of a custom format: a PM time, a
// single-digit hour and midnight. Additional formats are only used for
// parsing, so they need to round-trip the PM time only.
const PRIMARY_FORMAT_PROBES: FlowTimePickerTime[] = [
  { hours: 13, minutes: 5, seconds: 7, milliseconds: 45 },
  { hours: 1, minutes: 5, seconds: 7, milliseconds: 45 },
  { hours: 0, minutes: 5, seconds: 0, milliseconds: 0 }
];
const ADDITIONAL_FORMAT_PROBES = PRIMARY_FORMAT_PROBES.slice(0, 1);

/**
 * timepickerConnector is a communication layer between TimePicker's flow
 * component (server-side) and web component (client-side).
 */
export class TimePickerConnector {
  readonly #timePicker: FlowTimePicker;

  // Locale and the values derived from it, assigned by `updateI18n`
  #locale?: string;
  #amString: string | null = null;
  #pmString: string | null = null;
  #separator: string | null = null;
  #escapedSeparator = '';

  // Validated custom formats, the first one is used for formatting. When
  // unset, the time is formatted and parsed based on the locale.
  #timeFormats?: string[];

  // The result of the last successful parse, reused when the same string is
  // parsed again
  #cachedTimeString?: string;
  #cachedTimeObject?: FlowTimePickerTime;

  constructor(timePicker: FlowTimePicker) {
    this.#timePicker = timePicker;
  }

  updateI18n(locale: string, i18n: FlowTimePickerServerI18n | null): void {
    try {
      // Check whether the locale is supported by the browser or not
      TEST_PM_TIME.toLocaleTimeString(locale);
    } catch (e) {
      // FIXME should do a callback for server to throw an exception ?
      throw new Error(`vaadin-time-picker: The locale ${locale} is not supported.`);
    }

    this.#locale = locale;

    // 1. 24 or 12 hour clock, if latter then what are the am/pm strings ?
    this.#pmString = getPmString(locale);
    this.#amString = getAmString(locale);

    // 2. What is the separator ?
    this.#separator = getSeparator(locale);
    // The separator can be a regexp special character, such as the dot used by fi-FI
    this.#escapedSeparator = escapeRegExp(this.#separator || '');

    this.#timeFormats = validateTimeFormats(i18n?.timeFormats);

    // A cached result was parsed with the previous locale, so it no longer applies
    this.#cachedTimeString = undefined;
    this.#cachedTimeObject = undefined;

    // Assigning a new object makes the web component re-format the current value
    this.#timePicker.i18n = {
      formatTime: (timeObject) => this.#formatTime(timeObject),
      parseTime: (timeString) => this.#parseTime(timeString)
    };
  }

  #includeSeconds(): boolean {
    return !!this.#timePicker.step && this.#timePicker.step < 60;
  }

  #includeMilliseconds(): boolean {
    return !!this.#timePicker.step && this.#timePicker.step < 1;
  }

  #formatTime(timeObject: FlowTimePickerTime | undefined): string | undefined {
    if (!timeObject) return undefined;

    if (this.#timeFormats) {
      return dateFnsFormat(toDate(timeObject), this.#timeFormats[0]);
    }

    const timeToBeFormatted = new Date();
    timeToBeFormatted.setHours(timeObject.hours);
    timeToBeFormatted.setMinutes(timeObject.minutes);
    timeToBeFormatted.setSeconds(timeObject.seconds !== undefined ? timeObject.seconds : 0);

    // the web component expects the correct granularity used for the time string,
    // thus need to format the time object in correct granularity by passing the format options
    let localeTimeString = timeToBeFormatted.toLocaleTimeString(this.#locale, {
      hour: 'numeric',
      minute: 'numeric',
      second: this.#includeSeconds() ? 'numeric' : undefined
    });

    // milliseconds not part of the time format API
    if (this.#includeMilliseconds()) {
      localeTimeString = formatMilliseconds(localeTimeString, timeObject.milliseconds, this.#amString, this.#pmString);
    }

    return localeTimeString;
  }

  #parseTime(timeString: string): FlowTimePickerTime | undefined {
    if (this.#timeFormats) {
      const text = timeString.trim();
      // The first format that matches the text wins
      for (const timeFormat of this.#timeFormats) {
        const time = parseWithFormat(text, timeFormat);
        if (time) return time;
      }
      return undefined;
    }

    if (timeString && timeString === this.#cachedTimeString && this.#cachedTimeObject) {
      return this.#cachedTimeObject;
    }

    if (!timeString) {
      // when nothing is returned, the component shows the invalid state for the input
      return undefined;
    }

    const amToken = searchAmOrPmToken(timeString, this.#amString);
    const pmToken = searchAmOrPmToken(timeString, this.#pmString);

    const numbersOnlyTimeString = timeString
      .replace(amToken || '', '')
      .replace(pmToken || '', '')
      .trim();

    // A regexp that allows to find the numbers with optional separator and continuing searching after it.
    const numbersRegExp = new RegExp('([\\d\\u0660-\\u0669]){1,2}(?:' + this.#escapedSeparator + ')?', 'g');

    const hoursMatch = numbersRegExp.exec(numbersOnlyTimeString);
    if (hoursMatch) {
      let hours = parseDigitsIntoInteger(hoursMatch[0].replace(this.#separator || '', ''));
      // handle 12 am -> 0
      // do not do anything if am & pm are not used or if those are the same,
      // as with locale bg-BG there is always ч. at the end of the time
      if (amToken !== pmToken) {
        if (hours === 12 && amToken) {
          hours = 0;
        }
        if (hours !== 12 && pmToken) {
          hours += 12;
        }
      }
      const minutes = numbersRegExp.exec(numbersOnlyTimeString);
      const seconds = minutes && numbersRegExp.exec(numbersOnlyTimeString);
      // detecting milliseconds from input, expects am/pm removed from end, eg. .0 or .00 or .000
      const millisecondRegExp = /[[\.][\d\u0660-\u0669]{1,3}$/;
      // reset to end or things can explode
      let milliseconds: RegExpExecArray | false | null | undefined =
        seconds && this.#includeMilliseconds() && millisecondRegExp.exec(numbersOnlyTimeString);
      // handle case where last numbers are seconds and . is the separator (invalid regexp match)
      if (milliseconds && seconds && milliseconds.index <= seconds.index) {
        milliseconds = undefined;
      }
      // hours is a number at this point, others are either arrays or null
      // the string in [0] from the arrays includes the separator too
      this.#cachedTimeObject = {
        hours: hours,
        minutes: minutes ? parseDigitsIntoInteger(minutes[0].replace(this.#separator || '', '')) : 0,
        seconds: seconds ? parseDigitsIntoInteger(seconds[0].replace(this.#separator || '', '')) : 0,
        milliseconds:
          minutes && seconds && milliseconds ? parseMillisecondsIntoInteger(milliseconds[0].replace('.', '')) : 0
      };
      this.#cachedTimeString = timeString;
      return this.#cachedTimeObject;
    }
    return undefined;
  }
}

function toDate(time: FlowTimePickerTime): Date {
  const date = new Date(REFERENCE_DATE);
  date.setHours(time.hours, time.minutes, time.seconds ?? 0, time.milliseconds ?? 0);
  return date;
}

function parseWithFormat(text: string, timeFormat: string): FlowTimePickerTime | undefined {
  const date = dateFnsParse(text, timeFormat, REFERENCE_DATE);
  if (!dateFnsIsValid(date)) return undefined;
  return {
    hours: date.getHours(),
    minutes: date.getMinutes(),
    seconds: date.getSeconds(),
    milliseconds: date.getMilliseconds()
  };
}

/**
 * Returns whether each probe time survives formatting and parsing back with
 * the format. Only the fields that the format contains are compared, with
 * milliseconds compared at the precision of the format.
 */
function isValidTimeFormat(timeFormat: string, probes: FlowTimePickerTime[]): boolean {
  // Remove escaped quotes and quoted literals to keep only the tokens
  const tokens = timeFormat.replace(/''/g, '').replace(/'[^']*'/g, '');
  // Reject date tokens and letters that date-fns does not know
  if (/[a-z]/i.test(tokens.replace(/[HhKkmsSa]/g, ''))) {
    return false;
  }

  const millisecondDigits = tokens.split('S').length - 1;
  const millisecondDivisor = 10 ** (3 - millisecondDigits);
  const isSameTime = (a: FlowTimePickerTime, b: FlowTimePickerTime) =>
    a.hours === b.hours &&
    (!tokens.includes('m') || a.minutes === b.minutes) &&
    (!tokens.includes('s') || a.seconds === b.seconds) &&
    (!millisecondDigits ||
      Math.floor(a.milliseconds! / millisecondDivisor) === Math.floor(b.milliseconds! / millisecondDivisor));

  try {
    return probes.every((probe) => {
      const parsed = parseWithFormat(dateFnsFormat(toDate(probe), timeFormat), timeFormat);
      return !!parsed && isSameTime(probe, parsed);
    });
  } catch (e) {
    // date-fns throws for invalid formats
    return false;
  }
}

/**
 * Returns the formats that pass validation, or `undefined` to use the locale.
 * An invalid primary format disables all custom formats, while an invalid
 * additional format is dropped alone.
 */
function validateTimeFormats(timeFormats: string[] | undefined): string[] | undefined {
  if (!timeFormats?.length) return undefined;

  const [primaryFormat, ...additionalFormats] = timeFormats;
  if (!isValidTimeFormat(primaryFormat, PRIMARY_FORMAT_PROBES)) {
    console.warn(`vaadin-time-picker: The time format "${primaryFormat}" is not supported, using the locale instead.`);
    return undefined;
  }

  return [
    primaryFormat,
    ...additionalFormats.filter((timeFormat) => {
      const isValid = isValidTimeFormat(timeFormat, ADDITIONAL_FORMAT_PROBES);
      if (!isValid) {
        console.warn(`vaadin-time-picker: The time format "${timeFormat}" is not supported, ignoring it.`);
      }
      return isValid;
    })
  ];
}

function initLazy(timePicker: FlowTimePicker): void {
  // Init the connector only once for the time picker
  timePicker.$connector ??= new TimePickerConnector(timePicker);
}

window.Vaadin.Flow.timepickerConnector = { initLazy };
