import { expect } from 'chai';
import { fixtureSync, nextUpdate } from '@vaadin/testing-helpers';
import { getInputValue, init, timepickerConnector, type FlowTimePicker } from './shared.js';

describe('time-picker connector', () => {
  let timePicker: FlowTimePicker;

  beforeEach(async () => {
    timePicker = fixtureSync('<vaadin-time-picker></vaadin-time-picker>');
    init(timePicker);
    await nextUpdate(timePicker);
  });

  it('should not reinitialize the connector', () => {
    const connector = timePicker.$connector;
    timepickerConnector.initLazy(timePicker);
    expect(timePicker.$connector).to.equal(connector);
  });

  describe('12 hour clock locale', () => {
    beforeEach(() => {
      timePicker.$connector.setLocale('en-US');
    });

    it('should format time with the AM/PM token', () => {
      expect(timePicker.i18n.formatTime!({ hours: 13, minutes: 30, seconds: 0, milliseconds: 0 })).to.equal(
        '1:30 PM'
      );
    });

    it('should parse time with the AM/PM token', () => {
      expect(timePicker.i18n.parseTime!('1:30 PM')).to.eql({
        hours: 13,
        minutes: 30,
        seconds: 0,
        milliseconds: 0
      });
    });

    it('should parse time without the AM/PM token', () => {
      expect(timePicker.i18n.parseTime!('1:30')).to.eql({
        hours: 1,
        minutes: 30,
        seconds: 0,
        milliseconds: 0
      });
    });

    it('should not parse an empty string', () => {
      expect(timePicker.i18n.parseTime!('')).to.be.undefined;
    });

    it('should format seconds when the step is below a minute', () => {
      timePicker.step = 1;
      expect(timePicker.i18n.formatTime!({ hours: 13, minutes: 30, seconds: 15, milliseconds: 0 })).to.equal(
        '1:30:15 PM'
      );
    });

    it('should format milliseconds when the step is below a second', () => {
      timePicker.step = 0.5;
      expect(timePicker.i18n.formatTime!({ hours: 13, minutes: 30, seconds: 15, milliseconds: 250 })).to.equal(
        '1:30:15.250 PM'
      );
    });

    it('should parse milliseconds when the step is below a second', () => {
      timePicker.step = 0.5;
      expect(timePicker.i18n.parseTime!('1:30:15.250 PM')).to.eql({
        hours: 13,
        minutes: 30,
        seconds: 15,
        milliseconds: 250
      });
    });
  });

  describe('24 hour clock locale', () => {
    beforeEach(() => {
      timePicker.$connector.setLocale('de-DE');
    });

    it('should format time without the AM/PM token', () => {
      expect(timePicker.i18n.formatTime!({ hours: 13, minutes: 30, seconds: 0, milliseconds: 0 })).to.equal('13:30');
    });

    it('should parse time without the AM/PM token', () => {
      expect(timePicker.i18n.parseTime!('13:30')).to.eql({
        hours: 13,
        minutes: 30,
        seconds: 0,
        milliseconds: 0
      });
    });
  });

  describe('unsupported locale', () => {
    it('should name the unsupported locale in the error', () => {
      expect(() => timePicker.$connector.setLocale('en_US')).to.throw('en_US');
    });
  });

  describe('dot separator locale', () => {
    beforeEach(() => {
      timePicker.$connector.setLocale('fi-FI');
    });

    it('should format time using the dot separator', () => {
      expect(timePicker.i18n.formatTime!({ hours: 13, minutes: 30, seconds: 0, milliseconds: 0 })).to.equal('13.30');
    });

    it('should parse time using the dot separator', () => {
      expect(timePicker.i18n.parseTime!('13.30')).to.eql({
        hours: 13,
        minutes: 30,
        seconds: 0,
        milliseconds: 0
      });
    });

    [
      { text: '1234', hours: 12, minutes: 34 },
      { text: '2359', hours: 23, minutes: 59 },
      { text: '130', hours: 13, minutes: 0 }
    ].forEach(({ text, hours, minutes }) => {
      it(`should parse ${text} typed without a separator`, () => {
        expect(timePicker.i18n.parseTime!(text)).to.eql({
          hours,
          minutes,
          seconds: 0,
          milliseconds: 0
        });
      });
    });

    it('should parse milliseconds using the dot separator', () => {
      timePicker.step = 0.5;
      expect(timePicker.i18n.parseTime!('2.03.04.555')).to.eql({
        hours: 2,
        minutes: 3,
        seconds: 4,
        milliseconds: 555
      });
    });
  });

  describe('locale change', () => {
    beforeEach(async () => {
      timePicker.$connector.setLocale('de-DE');
      timePicker.value = '13:00';
      await nextUpdate(timePicker);
    });

    it('should reformat the value when the locale changes', async () => {
      timePicker.$connector.setLocale('en-US');
      await nextUpdate(timePicker);
      expect(getInputValue(timePicker)).to.equal('1:00 PM');
    });

    it('should keep the value when the locale changes', async () => {
      timePicker.$connector.setLocale('en-US');
      await nextUpdate(timePicker);
      expect(timePicker.value).to.equal('13:00');
    });

    it('should not reformat an empty value when the locale changes', async () => {
      timePicker.value = '';
      await nextUpdate(timePicker);
      timePicker.$connector.setLocale('en-US');
      await nextUpdate(timePicker);
      expect(getInputValue(timePicker)).to.equal('');
    });
  });
});
