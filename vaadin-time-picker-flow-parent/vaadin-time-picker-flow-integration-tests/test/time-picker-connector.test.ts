import { expect } from 'chai';
import * as sinon from 'sinon';
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
      timePicker.$connector.updateI18n('en-US', null);
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
      timePicker.$connector.updateI18n('de-DE', null);
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
      expect(() => timePicker.$connector.updateI18n('en_US', null)).to.throw('en_US');
    });
  });

  describe('dot separator locale', () => {
    beforeEach(() => {
      timePicker.$connector.updateI18n('fi-FI', null);
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
      timePicker.$connector.updateI18n('de-DE', null);
      timePicker.value = '13:00';
      await nextUpdate(timePicker);
    });

    it('should reformat the value when the locale changes', async () => {
      timePicker.$connector.updateI18n('en-US', null);
      await nextUpdate(timePicker);
      expect(getInputValue(timePicker)).to.equal('1:00 PM');
    });

    it('should keep the value when the locale changes', async () => {
      timePicker.$connector.updateI18n('en-US', null);
      await nextUpdate(timePicker);
      expect(timePicker.value).to.equal('13:00');
    });

    it('should not reformat an empty value when the locale changes', async () => {
      timePicker.value = '';
      await nextUpdate(timePicker);
      timePicker.$connector.updateI18n('en-US', null);
      await nextUpdate(timePicker);
      expect(getInputValue(timePicker)).to.equal('');
    });
  });

  describe('custom time formats', () => {
    function setTimeFormats(...timeFormats: string[]) {
      timePicker.$connector.updateI18n('en-US', { timeFormats });
    }

    it('should format time with a 24 hour format in a 12 hour clock locale', () => {
      setTimeFormats('HH:mm');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal('13:05');
    });

    it('should format and parse time with a custom separator', () => {
      setTimeFormats('HH.mm');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal('13.05');
      expect(timePicker.i18n.parseTime('14.30')).to.eql({ hours: 14, minutes: 30, seconds: 0, milliseconds: 0 });
    });

    it('should parse time with an additional format and format with the primary format', () => {
      setTimeFormats('HH.mm', 'Hmm');
      expect(timePicker.i18n.parseTime('930')).to.eql({ hours: 9, minutes: 30, seconds: 0, milliseconds: 0 });
      expect(timePicker.i18n.formatTime({ hours: 9, minutes: 30 })).to.equal('09.30');
    });

    [
      { text: '130', hours: 13, minutes: 0 },
      { text: '245', hours: 2, minutes: 45 }
    ].forEach(({ text, hours, minutes }) => {
      it(`should parse ambiguous ${text} with an additional Hmm format`, () => {
        setTimeFormats('HH:mm', 'Hmm');
        expect(timePicker.i18n.parseTime(text)).to.eql({ hours, minutes, seconds: 0, milliseconds: 0 });
      });
    });

    it('should parse single digit hours and minutes with H:mm', () => {
      setTimeFormats('H:mm');
      expect(timePicker.i18n.parseTime('9:5')).to.eql({ hours: 9, minutes: 5, seconds: 0, milliseconds: 0 });
    });

    it('should format and parse time with the AM/PM token', () => {
      setTimeFormats('h:mm a');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal('1:05 PM');
      expect(timePicker.i18n.parseTime('1:30 pm')).to.eql({ hours: 13, minutes: 30, seconds: 0, milliseconds: 0 });
    });

    it('should not parse time without the AM/PM token when the format requires it', () => {
      setTimeFormats('h:mm a');
      expect(timePicker.i18n.parseTime('1:30')).to.be.undefined;
    });

    it('should format and parse milliseconds', () => {
      timePicker.step = 0.001;
      setTimeFormats('HH:mm:ss.SSS');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5, seconds: 7, milliseconds: 45 })).to.equal(
        '13:05:07.045'
      );
      expect(timePicker.i18n.parseTime('13:05:07.045')).to.eql({ hours: 13, minutes: 5, seconds: 7, milliseconds: 45 });
    });

    it('should format tenths of a second with a single fraction digit', () => {
      setTimeFormats('HH:mm:ss.S');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5, seconds: 7, milliseconds: 400 })).to.equal(
        '13:05:07.4'
      );
    });

    it('should accept a format with two fraction digits', () => {
      setTimeFormats('HH:mm:ss.SS');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5, seconds: 7, milliseconds: 450 })).to.equal(
        '13:05:07.45'
      );
    });

    it('should parse time with a quoted literal', () => {
      setTimeFormats("HH'h'mm");
      expect(timePicker.i18n.parseTime('14h30')).to.eql({ hours: 14, minutes: 30, seconds: 0, milliseconds: 0 });
    });

    it('should format time with an escaped quote', () => {
      setTimeFormats("HH''mm");
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal("13'05");
    });

    it('should parse hours only with an additional H format', () => {
      setTimeFormats('HH:mm', 'H');
      expect(timePicker.i18n.parseTime('9')).to.eql({ hours: 9, minutes: 0, seconds: 0, milliseconds: 0 });
    });

    it('should format missing seconds as zero', () => {
      setTimeFormats('HH:mm:ss');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal('13:05:00');
    });

    it('should not format an undefined time', () => {
      setTimeFormats('HH:mm');
      expect(timePicker.i18n.formatTime(undefined)).to.be.undefined;
    });

    ['abc', '25:00', ''].forEach((text) => {
      it(`should not parse invalid input "${text}"`, () => {
        setTimeFormats('HH:mm');
        expect(timePicker.i18n.parseTime(text)).to.be.undefined;
      });
    });

    it('should trim a leading space before parsing', () => {
      setTimeFormats('HH:mm');
      expect(timePicker.i18n.parseTime(' 14:30')).to.eql({ hours: 14, minutes: 30, seconds: 0, milliseconds: 0 });
    });

    it('should not parse with a previous format after the formats change', () => {
      setTimeFormats('HH.mm');
      expect(timePicker.i18n.parseTime('14.30')).to.eql({ hours: 14, minutes: 30, seconds: 0, milliseconds: 0 });
      setTimeFormats('HH:mm');
      expect(timePicker.i18n.parseTime('14.30')).to.be.undefined;
    });

    it('should revert to the locale format without custom formats', () => {
      setTimeFormats('HH:mm');
      timePicker.$connector.updateI18n('en-US', {});
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal('1:05 PM');
    });

    it('should use the custom format for the dropdown items', async () => {
      setTimeFormats('HH:mm');
      await nextUpdate(timePicker);
      const items = (timePicker as unknown as { _dropdownItems: Array<{ label: string }> })._dropdownItems;
      expect(items[13].label).to.equal('13:00');
    });
  });

  describe('pattern validation', () => {
    let warn: sinon.SinonStub;

    beforeEach(() => {
      warn = sinon.stub(console, 'warn');
    });

    afterEach(() => {
      warn.restore();
    });

    function setTimeFormats(...timeFormats: string[]) {
      timePicker.$connector.updateI18n('en-US', { timeFormats });
    }

    ['h:mm', 'Hmm', 'K:mm', 'HH:mm yyyy', 'foo'].forEach((timeFormat) => {
      it(`should reject the primary format ${timeFormat} and use the locale`, () => {
        expect(() => setTimeFormats(timeFormat)).not.to.throw();
        expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal('1:05 PM');
        expect(warn.calledOnce).to.be.true;
      });
    });

    it('should accept an additional Hmm format with a valid primary format', () => {
      setTimeFormats('HH:mm', 'Hmm');
      expect(timePicker.i18n.parseTime('930')).to.eql({ hours: 9, minutes: 30, seconds: 0, milliseconds: 0 });
      expect(warn.called).to.be.false;
    });

    it('should drop only an invalid additional format', () => {
      setTimeFormats('HH.mm', 'foo');
      expect(timePicker.i18n.parseTime('14.30')).to.eql({ hours: 14, minutes: 30, seconds: 0, milliseconds: 0 });
      expect(warn.calledOnce).to.be.true;
    });

    it('should not apply a valid additional format when the primary format is invalid', () => {
      setTimeFormats('h:mm', 'Hmm');
      expect(timePicker.i18n.formatTime({ hours: 13, minutes: 5 })).to.equal('1:05 PM');
      // The locale parser reads 930 as hours 93, which the web component rejects
      expect(timePicker.i18n.parseTime('930')).not.to.eql({ hours: 9, minutes: 30, seconds: 0, milliseconds: 0 });
      expect(warn.calledOnce).to.be.true;
    });
  });
});
