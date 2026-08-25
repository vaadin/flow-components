// Adds the createDate helper that is missing from the @vaadin/date-picker
// helper module's type declarations
import type {} from '@vaadin/date-picker/src/vaadin-date-picker-helper.js';

declare module '@vaadin/date-picker/src/vaadin-date-picker-helper.js' {
  /**
   * Creates a Date from the given parts. Unlike the Date constructor, years
   * below 100 are kept as they are instead of being mapped to the 1900s.
   *
   * @param year full year
   * @param month zero-based month, may be out of range to shift the year
   * @param day may be `0` to select the last day of the previous month
   */
  export function createDate(year: number, month: number, day: number): Date;
}
