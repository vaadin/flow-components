import './env-setup.js';
import '@vaadin/date-picker/vaadin-date-picker.js';
import '../frontend/generated/jar-resources/datepickerConnector.js';
import type { DatePicker, DatePickerDate } from '@vaadin/date-picker';
import type {} from '@web/test-runner-mocha';

export type { DatePickerDate } from '@vaadin/date-picker';

export type DatePickerI18n = {
  dateFormats: string[];
  firstDayOfWeek?: number;
  referenceDate?: DatePickerDate;
}

export type DatePickerConnector = {
  initLazy: (datePicker: DatePicker) => void;
  updateI18n: (locale: string, i18n: DatePickerI18n) => void;
};

export type FlowDatePicker = DatePicker & {
  $connector: DatePickerConnector;
};

type Vaadin = {
  Flow: {
    datepickerConnector: DatePickerConnector;
  };
};

const Vaadin = window.Vaadin as Vaadin;

export const datepickerConnector = Vaadin.Flow.datepickerConnector;

export function init(datePicker: FlowDatePicker): void {
  datepickerConnector.initLazy(datePicker);
}
