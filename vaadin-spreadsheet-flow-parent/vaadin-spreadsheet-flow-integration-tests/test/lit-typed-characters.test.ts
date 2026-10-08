/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import { expect } from 'chai';
import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';
import { typesCharacter } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/KeyboardMoves.js';

const plain = { ctrlKey: false, metaKey: false, altKey: false };

describe('typesCharacter', () => {
  it('takes a plain character, on any platform', () => {
    expect(typesCharacter({ key: 'a', ...plain }, false)).to.be.true;
    expect(typesCharacter({ key: 'a', ...plain }, true)).to.be.true;
  });

  it('takes a character typed with AltGr, which Windows reports as Ctrl and Alt', () => {
    expect(typesCharacter({ key: '€', ...plain, ctrlKey: true, altKey: true }, false)).to.be.true;
  });

  it('takes a character typed with AltGr where the browser reports AltGraph', () => {
    expect(
      typesCharacter({ key: '@', ...plain, getModifierState: (k: string) => k === 'AltGraph' }, false)
    ).to.be.true;
  });

  it('takes a character typed with Option on a Mac', () => {
    expect(typesCharacter({ key: '@', ...plain, altKey: true }, true)).to.be.true;
  });

  it('leaves Ctrl and Cmd shortcuts alone', () => {
    expect(typesCharacter({ key: 'b', ...plain, ctrlKey: true }, false)).to.be.false;
    expect(typesCharacter({ key: 'b', ...plain, metaKey: true }, true)).to.be.false;
    expect(typesCharacter({ key: 'b', ...plain, ctrlKey: true }, true)).to.be.false;
  });

  it('leaves Alt alone off a Mac, where it types nothing', () => {
    expect(typesCharacter({ key: 'f', ...plain, altKey: true }, false)).to.be.false;
  });

  it('leaves named keys and text still being composed alone', () => {
    expect(typesCharacter({ key: 'Enter', ...plain }, false)).to.be.false;
    expect(typesCharacter({ key: 'F2', ...plain }, false)).to.be.false;
    expect(typesCharacter({ key: 'a', ...plain, isComposing: true }, false)).to.be.false;
  });
});

describe('Spreadsheet — typing into a cell with AltGr or Option', () => {
  function onPlatform<T>(platform: string, body: () => T): T {
    Object.defineProperty(navigator, 'platform', { value: platform, configurable: true });
    try {
      return body();
    } finally {
      delete (navigator as { platform?: string }).platform;
    }
  }

  let host: HTMLElement;
  let sheet: Spreadsheet;

  beforeEach(() => {
    host = document.createElement('div');
    host.style.cssText = 'width:300px;height:200px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    sheet = new Spreadsheet(host);
    sheet.setRows(5);
    sheet.setCols(5);
    sheet.setColW([50, 50, 50, 50, 50]);
    sheet.setRowH([15, 15, 15, 15, 15]);
    sheet.recomputeViewport();
  });

  afterEach(() => {
    sheet.dispose();
    host.remove();
  });

  function press(init: KeyboardEventInit): void {
    sheet
      .getScrollContainer()
      .dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, cancelable: true, ...init }));
  }

  function editorValue(): string | null {
    return sheet.getCellGrid().renderState.editor?.initialValue ?? null;
  }

  it('starts editing with an @ typed with Option on a Mac', () => {
    onPlatform('MacIntel', () => press({ key: '@', altKey: true }));

    expect(editorValue()).to.equal('@');
  });

  it('starts editing with a € typed with AltGr on Windows', () => {
    onPlatform('Win32', () => press({ key: '€', ctrlKey: true, altKey: true }));

    expect(editorValue()).to.equal('€');
  });

  it('does not start editing on a Ctrl shortcut', () => {
    onPlatform('Win32', () => press({ key: 'b', ctrlKey: true }));

    expect(editorValue()).to.be.null;
  });
});
