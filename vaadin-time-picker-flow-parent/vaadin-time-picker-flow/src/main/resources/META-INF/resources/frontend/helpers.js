// map from unicode eastern arabic number characters to arabic numbers
const ARABIC_DIGIT_MAP = {
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

/**
 * Escapes the given string so it can be safely used in a regexp.
 *
 * @param {string} string
 * @return {string}
 */
 function escapeRegExp(string) {
  return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Parses eastern arabic number characters to arabic numbers (0-9)
 */
function anyNumberCharToArabicNumberReplacer(charsToReplace) {
  return charsToReplace.replace(/[\u0660-\u0669]/g, function (char) {
    const unicode = '\\u0' + char.charCodeAt(0).toString(16);
    return ARABIC_DIGIT_MAP[unicode];
  });
}

function getAmPmString(locale, testTime) {
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
}

/**
 * Searches for either an AM or PM token in the given time string
 * depending on what is provided in `amPmString`.
 *
 * The search is case and space insensitive.
 *
 * @example
 * `searchAmPmToken('1 P M', 'PM')` => `'P M'`
 *
 * @example
 * `searchAmPmToken('1 a.m.', 'A. M.')` => `a.m.`
 *
 * @param {string} timeString
 * @param {string} amPmString
 * @return {string | null}
 */
export function searchAmPmToken(timeString, amPmString) {
  if (!amPmString) return null;

  // Turn `amPmString` into a space-insensitive regexp representation.
  const tokenRegExpString = amPmString.split(/\s*/).map(escapeRegExp).join('\\s*');

  // Create an actual regexp with the enabled case-insensitivity.
  const tokenRegExp = new RegExp(tokenRegExpString, 'i');

  // Match the regexp against the time string.
  const tokenMatches = timeString.match(tokenRegExp);
  if (tokenMatches) {
    return tokenMatches[0];
  }
}

export const TEST_PM_TIME = new Date('August 19, 1975 23:15:30');

export const TEST_AM_TIME = new Date('August 19, 1975 05:15:30');

export function getPmString(locale) {
  return getAmPmString(locale, TEST_PM_TIME);
}

export function getAmString(locale) {
  return getAmPmString(locale, TEST_AM_TIME);
}

export function parseAnyCharsToInt(anyNumberChars) {
  return parseInt(anyNumberCharToArabicNumberReplacer(anyNumberChars));
}

export function parseMillisecondCharsToInt(millisecondChars) {
  millisecondChars = anyNumberCharToArabicNumberReplacer(millisecondChars);
  // digits are either .1 .01 or .001 so need to "shift"
  if (millisecondChars.length === 1) {
    millisecondChars += '00';
  } else if (millisecondChars.length === 2) {
    millisecondChars += '0';
  }
  return parseInt(millisecondChars);
}

export function formatMilliseconds(localeTimeString, milliseconds, amString, pmString) {
  // might need to inject milliseconds between seconds and AM/PM
  let cleanedTimeString = localeTimeString;
  if (localeTimeString.endsWith(amString)) {
    cleanedTimeString = localeTimeString.replace(' ' + amString, '');
  } else if (localeTimeString.endsWith(pmString)) {
    cleanedTimeString = localeTimeString.replace(' ' + pmString, '');
  }
  if (milliseconds) {
    let millisecondsString = milliseconds < 10 ? '0' : '';
    millisecondsString += milliseconds < 100 ? '0' : '';
    millisecondsString += milliseconds;
    cleanedTimeString += '.' + millisecondsString;
  } else {
    cleanedTimeString += '.000';
  }
  if (localeTimeString.endsWith(amString)) {
    cleanedTimeString = cleanedTimeString + ' ' + amString;
  } else if (localeTimeString.endsWith(pmString)) {
    cleanedTimeString = cleanedTimeString + ' ' + pmString;
  }
  return cleanedTimeString;
}
