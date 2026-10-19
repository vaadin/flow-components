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
import { touchCommandPosition } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/algorithms/Placement.js';
import { FormulaPointer } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/FormulaPointer.js';
import { PointerSelection } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/PointerSelection.js';
import { SelectionReporter } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/SelectionReporter.js';
import { TouchSelection } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/controllers/TouchSelection.js';
import { SheetStore } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/state/SheetStore.js';
import type { SpreadsheetEvent } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/types/rpc-client-to-server.js';
import { spreadsheetStyles } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet-styles.js';

describe('touchCommandPosition — where the command of a selection by touch goes', () => {
  const selection = { left: 200, top: 300, right: 300, bottom: 340 };
  const size = { width: 60, height: 30 };

  it('is centred over the selection, 5px above it', () => {
    expect(touchCommandPosition(selection, size, 100, 1000)).to.eql({ left: 220, top: 265 });
  });

  it('goes 5px below the selection when above would take it past the top of the cells', () => {
    expect(touchCommandPosition(selection, size, 280, 1000)).to.eql({ left: 220, top: 345 });
  });

  it('stays inside the window across', () => {
    const atLeft = { left: 0, top: 300, right: 20, bottom: 340 };
    const atRight = { left: 980, top: 300, right: 1000, bottom: 340 };

    expect(touchCommandPosition(atLeft, size, 100, 1000).left).to.equal(0);
    expect(touchCommandPosition(atRight, size, 100, 1000).left).to.equal(940);
  });
});

/**
 * Selecting by touch, on its own: a finger's press makes the sheet used by
 * touch, the corner handle stretches the selection, a "Fill" command is
 * offered after, and once it is chosen the fill handles fill outward.
 */
describe('TouchSelection', () => {
  let store: SheetStore;
  let sent: SpreadsheetEvent[];
  let selecting: PointerSelection;
  let scrolling: boolean;
  let movedTo: Array<[number, number]>;
  let selectionBox: { left: number; top: number; right: number; bottom: number } | null;
  let drawings: Array<() => void>;
  let touch: TouchSelection;
  let holder: HTMLElement;

  const types = (): string[] => sent.map((e) => e.type);
  const range = () => store.selection.activeRange;
  const command = () => touch.element;
  /** Runs what waits for the sheet to be drawn. */
  const draw = () => drawings.splice(0).forEach((work) => work());

  beforeEach(() => {
    store = new SheetStore(() => {});
    store.write('rows', 20);
    store.write('cols', 10);
    sent = [];
    scrolling = false;
    movedTo = [];
    selectionBox = { left: 100, top: 200, right: 200, bottom: 240 };
    drawings = [];
    const report = new SelectionReporter(store, { send: (e) => sent.push(e), selectedCellReported: () => {} });
    const pointing = new FormulaPointer(store, {
      fieldHasFocus: () => false,
      sheetName: () => 'Sheet1',
      scrollIntoView: () => {}
    });
    selecting = new PointerSelection(store, report, pointing, {
      scrollTop: () => 0,
      scrollLeft: () => 0,
      finishHeaderResize: () => false,
      cellOfEditor: () => null,
      send: (e) => sent.push(e)
    });
    touch = new TouchSelection(store, selecting, {
      // Cells of 50 by 20 pixels from the top left of the window.
      cellAt: (x, y) => ({ col: Math.floor(x / 50) + 1, row: Math.floor(y / 20) + 1 }),
      scrolling: () => scrolling,
      movedTo: (x, y) => movedTo.push([x, y]),
      selectionBox: () => selectionBox,
      cellsTop: () => 0,
      afterDrawn: (work) => drawings.push(work)
    });
    // In a root with the sheet's stylesheet, as the sheet's own parts are.
    holder = document.createElement('div');
    document.body.append(holder);
    const shadow = holder.attachShadow({ mode: 'open' });
    shadow.adoptedStyleSheets = [spreadsheetStyles.styleSheet!];
    shadow.append(command());
  });

  afterEach(() => {
    holder.remove();
  });

  /** A finger moved to a point. */
  function fingerAt(x: number, y: number): void {
    touch.moved({ pointerType: 'touch', clientX: x, clientY: y });
  }

  describe('what the sheet is used by', () => {
    it('is touch from a press with a finger, and not from one with a mouse or a pen', () => {
      touch.pressed('touch');
      expect(store.interaction.touch).to.equal('select');

      touch.pressed('mouse');
      expect(store.interaction.touch).to.equal('off');

      touch.pressed('touch');
      touch.pressed('pen');
      expect(store.interaction.touch).to.equal('off');
    });

    it('takes the command away on a press with a mouse', () => {
      touch.pressed('touch');
      touch.tapped();
      draw();
      expect(command().hidden).to.be.false;

      touch.pressed('mouse');

      expect(command().hidden).to.be.true;
    });
  });

  describe('the corner handle', () => {
    beforeEach(() => {
      store.selectCellOrMerge(2, 2);
      touch.pressed('touch');
    });

    it('stretches the selection from its active cell to the cell under the finger, reported on the lift', () => {
      touch.handlePressed(false);
      fingerAt(170, 90);

      expect(range()).to.eql({ c1: 2, c2: 4, r1: 2, r2: 5 });
      expect(movedTo).to.eql([[170, 90]]);

      touch.lifted();
      expect(types()).to.eql(['cellRangePainted']);
      expect(store.interaction.press).to.equal(null);
    });

    it('leaves the drag to the cells while they scroll past their edge', () => {
      touch.handlePressed(false);
      scrolling = true;

      fingerAt(170, 90);

      expect(range()).to.eql({ c1: 2, c2: 2, r1: 2, r2: 2 });
      expect(movedTo, 'the cells are told where the finger is').to.eql([[170, 90]]);
    });

    it('takes no mouse, and no finger while no handle is dragged', () => {
      touch.handlePressed(false);
      touch.moved({ pointerType: 'mouse', clientX: 170, clientY: 90 });
      expect(range(), 'a mouse').to.eql({ c1: 2, c2: 2, r1: 2, r2: 2 });

      touch.lifted();
      fingerAt(170, 90);
      expect(range(), 'after the lift').to.eql({ c1: 2, c2: 2, r1: 2, r2: 2 });
      expect(movedTo).to.eql([]);
    });

    it('offers the command once the stretched selection is drawn, and not before', () => {
      touch.handlePressed(false);
      fingerAt(170, 90);
      touch.lifted();
      expect(command().hidden, 'before').to.be.true;

      draw();

      expect(command().hidden).to.be.false;
      expect(command().textContent!.trim()).to.equal('Fill');
    });

    it('takes the command away when the handle is pressed again', () => {
      touch.tapped();
      draw();

      touch.handlePressed(false);

      expect(command().hidden).to.be.true;
    });
  });

  describe('the command', () => {
    beforeEach(() => {
      store.selectCellOrMerge(2, 2);
      touch.pressed('touch');
    });

    it('is offered after a tap by touch, over the selection', () => {
      touch.tapped();
      draw();

      expect(command().hidden).to.be.false;
      const box = command().getBoundingClientRect();
      // Centred on the selection, 150px across, and ending 5px above its top at 200.
      expect(box.left + box.width / 2).to.be.closeTo(150, 1);
      expect(box.bottom).to.be.closeTo(195, 1);
    });

    it('is not offered after a click with a mouse', () => {
      touch.pressed('mouse');
      touch.tapped();
      expect(drawings, 'nothing waits for the drawing').to.have.length(0);
      draw();

      expect(command().hidden).to.be.true;
    });

    it('is not offered while none of the selection shows', () => {
      selectionBox = null;

      touch.tapped();
      draw();

      expect(command().hidden).to.be.true;
    });

    it('goes away on a press elsewhere, and stays for a press on itself', () => {
      touch.tapped();
      draw();

      touch.dismiss([command().firstElementChild!, command(), document.body]);
      expect(command().hidden, 'on itself').to.be.false;

      touch.dismiss([document.body]);
      expect(command().hidden, 'elsewhere').to.be.true;
    });

    it('goes away for good: a dismissal with no press', () => {
      touch.tapped();
      draw();

      touch.dismiss();

      expect(command().hidden).to.be.true;
    });

    it('turns the handles into fill handles when Fill is chosen', () => {
      touch.tapped();
      draw();

      command().querySelector('button')!.click();

      expect(store.interaction.touch).to.equal('fill');
      expect(command().hidden).to.be.true;
    });

    it('is not offered once Fill is chosen meanwhile', () => {
      touch.tapped();
      store.useTouch('fill');

      draw();

      expect(command().hidden).to.be.true;
    });
  });

  describe('the fill handles', () => {
    beforeEach(() => {
      store.select({ activeCell: { col: 2, row: 2 }, activeRange: { c1: 2, c2: 2, r1: 2, r2: 3 }, auxRanges: [] });
      touch.pressed('touch');
      store.useTouch('fill');
    });

    it('fill outward from the selection, and the sheet goes back to selecting after', () => {
      touch.handlePressed(true);
      // A press on a fill handle keeps the fill going.
      touch.pressed('touch');
      expect(store.interaction.touch).to.equal('fill');

      fingerAt(70, 110);
      touch.lifted();

      expect(sent).to.eql([{ type: 'selectionIncreasePainted', data: { r1: 2, c1: 2, r2: 6, c2: 2 } }]);
      expect(store.interaction.touch).to.equal('select');
      draw();
      expect(command().hidden, 'no command after a fill').to.be.true;
    });

    it('never clear: a drag back inside the selection fills nothing', () => {
      touch.handlePressed(true);

      fingerAt(70, 30);
      touch.lifted();

      expect(sent).to.eql([]);
    });

    it('go away on a press by touch anywhere else', () => {
      touch.pressed('touch');

      expect(store.interaction.touch).to.equal('select');
    });
  });
});

describe('Spreadsheet — selecting by touch', () => {
  function attach(): { host: HTMLElement; sheet: Spreadsheet; cleanup: () => void } {
    const host = document.createElement('div');
    host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
    document.body.appendChild(host);
    // A root with the sheet's stylesheet, as the element has.
    const shadow = host.attachShadow({ mode: 'open' });
    shadow.adoptedStyleSheets = [spreadsheetStyles.styleSheet!];
    const sheet = new Spreadsheet(host, shadow);
    sheet.setRows(100);
    sheet.setCols(30);
    sheet.setColW(new Array(30).fill(50));
    sheet.setRowH(new Array(100).fill(15));
    sheet.recomputeViewport();
    return {
      host,
      sheet,
      cleanup: () => {
        sheet.dispose();
        host.remove();
      }
    };
  }

  const frame = () => new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));

  /** A finger's press, moves and lift, from an element to points on the page. */
  function touchDrag(element: Element, to: Array<{ x: number; y: number }>): PointerEvent {
    const box = element.getBoundingClientRect();
    const start = { clientX: box.left + box.width / 2, clientY: box.top + box.height / 2 };
    const common = { pointerType: 'touch', pointerId: 7, isPrimary: true, bubbles: true, composed: true };
    const down = new PointerEvent('pointerdown', { ...common, ...start, cancelable: true });
    element.dispatchEvent(down);
    for (const point of to) {
      document.dispatchEvent(new PointerEvent('pointermove', { ...common, clientX: point.x, clientY: point.y }));
    }
    const end = to[to.length - 1] ?? { x: start.clientX, y: start.clientY };
    document.dispatchEvent(new PointerEvent('pointerup', { ...common, clientX: end.x, clientY: end.y }));
    return down;
  }

  function cellBox(sheet: Spreadsheet, col: number, row: number): DOMRect {
    return sheet.getCellGrid().cellClientRect(col, row)!;
  }

  function outline(sheet: Spreadsheet): HTMLElement {
    return sheet.getCellGrid().shadowRoot!.querySelector<HTMLElement>('.sheet-selection')!;
  }

  /** The command, in the top layer beside the sheet's parts. */
  function command(host: HTMLElement): HTMLElement {
    return host.shadowRoot!.querySelector<HTMLElement>('.touch-actions')!;
  }

  it('draws the selection for a finger after a press with one, and for a mouse after a press with that', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const cell = sheet.getCellGrid().shadowRoot!.querySelector('.cell.col2.row2')!;

      cell.dispatchEvent(new PointerEvent('pointerdown', { pointerType: 'touch', bubbles: true, composed: true }));
      await sheet.getCellGrid().updateComplete;
      expect(outline(sheet).classList.contains('touch')).to.be.true;

      cell.dispatchEvent(new PointerEvent('pointerdown', { pointerType: 'mouse', bubbles: true, composed: true }));
      await sheet.getCellGrid().updateComplete;
      expect(outline(sheet).classList.contains('touch')).to.be.false;
    } finally {
      cleanup();
    }
  });

  it('stretches the selection with the corner handle, keeps the mouse out of it, and offers Fill', async () => {
    const { host, sheet, cleanup } = attach();
    const events: string[] = [];
    host.addEventListener('spreadsheet-event', (e: Event) => {
      const { type } = (e as CustomEvent).detail;
      // The sheet tells the server where it is scrolled as it is first drawn.
      if (type !== 'onSheetScroll') {
        events.push(type);
      }
    });
    try {
      await sheet.getCellGrid().updateComplete;
      const cell = sheet.getCellGrid().shadowRoot!.querySelector('.cell.col2.row2')!;
      cell.dispatchEvent(new PointerEvent('pointerdown', { pointerType: 'touch', bubbles: true, composed: true }));
      sheet.getCellGrid().dispatchEvent(
        new CustomEvent('cell-click', {
          detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false },
          bubbles: true,
          composed: true
        })
      );
      await sheet.getCellGrid().updateComplete;
      const corner = sheet.getCellGrid().shadowRoot!.querySelector('.selection-corner')!;
      const target = cellBox(sheet, 4, 6);

      const down = touchDrag(corner, [{ x: target.left + 5, y: target.top + 5 }]);

      expect(down.defaultPrevented, 'no mouse press made of the touch').to.be.true;
      expect(sheet.getSelection().activeRange).to.eql({ c1: 2, c2: 4, r1: 2, r2: 6 });
      expect(events).to.eql(['cellSelected', 'cellRangePainted']);
      await frame();
      const fill = command(host);
      expect(fill.hidden).to.be.false;
      expect(fill.textContent!.trim()).to.equal('Fill');
    } finally {
      cleanup();
    }
  });

  it('leaves a press of the corner with a mouse to the mouse: a fill', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const corner = sheet.getCellGrid().shadowRoot!.querySelector('.selection-corner')!;

      const down = new PointerEvent('pointerdown', { pointerType: 'mouse', bubbles: true, composed: true, cancelable: true });
      corner.dispatchEvent(down);

      expect(down.defaultPrevented).to.be.false;
    } finally {
      cleanup();
    }
  });

  it('offers Fill over the part of a stretched selection that shows', async () => {
    const { host, sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const cell = sheet.getCellGrid().shadowRoot!.querySelector('.cell.col2.row2')!;
      cell.dispatchEvent(new PointerEvent('pointerdown', { pointerType: 'touch', bubbles: true, composed: true }));
      sheet.getCellGrid().dispatchEvent(
        new CustomEvent('cell-click', {
          detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false },
          bubbles: true,
          composed: true
        })
      );
      await sheet.getCellGrid().updateComplete;
      const corner = sheet.getCellGrid().shadowRoot!.querySelector('.selection-corner')!;
      const container = sheet.getScrollContainer();
      const below = container.getBoundingClientRect().bottom + 30;
      const box = corner.getBoundingClientRect();
      const common = { pointerType: 'touch', pointerId: 8, bubbles: true, composed: true };
      corner.dispatchEvent(
        new PointerEvent('pointerdown', { ...common, cancelable: true, clientX: box.left + 2, clientY: box.top + 2 })
      );
      document.dispatchEvent(new PointerEvent('pointermove', { ...common, clientX: box.left + 2, clientY: below }));
      // Held below the sheet: the rows scroll on, row 2 out of view above.
      await new Promise((resolve) => setTimeout(resolve, 400));
      document.dispatchEvent(new PointerEvent('pointerup', { ...common, clientX: box.left + 2, clientY: below }));
      await frame();

      expect(container.scrollTop).to.be.above(30);
      const cellsBottom = container.getBoundingClientRect().top + container.clientTop + container.clientHeight;
      const fill = command(host);
      expect(fill.hidden).to.be.false;
      // Under the part that shows, which ends with the cells in view - not under the row cut off there.
      expect(fill.getBoundingClientRect().top).to.be.at.most(cellsBottom + 5 + 1);
    } finally {
      cleanup();
    }
  });

  /** Taps a cell with a finger; the command offered after. */
  async function tap(sheet: Spreadsheet, host: HTMLElement, col: number, row: number): Promise<HTMLElement> {
    await sheet.getCellGrid().updateComplete;
    const cell = sheet.getCellGrid().shadowRoot!.querySelector(`.cell.col${col}.row${row}`)!;
    cell.dispatchEvent(new PointerEvent('pointerdown', { pointerType: 'touch', bubbles: true, composed: true }));
    sheet.getCellGrid().dispatchEvent(
      new CustomEvent('cell-click', {
        detail: { col, row, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false },
        bubbles: true,
        composed: true
      })
    );
    await frame();
    const offered = command(host);
    expect(offered.hidden, 'offered').to.be.false;
    return offered;
  }

  it('takes the command away when the cells scroll, on a press elsewhere, a key, or the sheet leaving the page', async () => {
    const { host, sheet, cleanup } = attach();
    try {
      let offered = await tap(sheet, host, 2, 2);
      sheet.getScrollContainer().scrollTop = 40;
      await frame();
      expect(offered.hidden, 'a scroll').to.be.true;

      offered = await tap(sheet, host, 2, 5);
      document.body.dispatchEvent(new PointerEvent('pointerdown', { pointerType: 'touch', bubbles: true }));
      expect(offered.hidden, 'a press elsewhere').to.be.true;

      offered = await tap(sheet, host, 2, 5);
      document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'a', bubbles: true }));
      expect(offered.hidden, 'a key').to.be.true;

      offered = await tap(sheet, host, 2, 5);
      sheet.disconnect();
      expect(offered.hidden, 'leaving the page').to.be.true;
    } finally {
      cleanup();
    }
  });

  it('offers Fill below a selection at the top of the cells', async () => {
    const { host, sheet, cleanup } = attach();
    try {
      const offered = await tap(sheet, host, 2, 1);

      expect(offered.getBoundingClientRect().top).to.be.at.least(cellBox(sheet, 2, 1).bottom);
    } finally {
      cleanup();
    }
  });

  it('ends a stretch the browser takes the touch over from', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const corner = sheet.getCellGrid().shadowRoot!.querySelector('.selection-corner')!;
      const common = { pointerType: 'touch', pointerId: 9, bubbles: true, composed: true };
      const to = (col: number, row: number) => {
        const box = cellBox(sheet, col, row);
        return { clientX: box.left + 5, clientY: box.top + 5 };
      };
      corner.dispatchEvent(new PointerEvent('pointerdown', { ...common, cancelable: true, ...to(1, 1) }));
      document.dispatchEvent(new PointerEvent('pointermove', { ...common, ...to(3, 3) }));

      document.dispatchEvent(new PointerEvent('pointercancel', { ...common, ...to(3, 3) }));
      document.dispatchEvent(new PointerEvent('pointermove', { ...common, ...to(5, 5) }));

      expect(sheet.getSelection().activeRange).to.eql({ c1: 1, c2: 3, r1: 1, r2: 3 });
    } finally {
      cleanup();
    }
  });

  it('leaves a stretch past the edge of the cells to their scrolling, not the cell under the finger', async () => {
    const { sheet, cleanup } = attach();
    try {
      await sheet.getCellGrid().updateComplete;
      const corner = sheet.getCellGrid().shadowRoot!.querySelector('.selection-corner')!;
      const box = corner.getBoundingClientRect();
      const common = { pointerType: 'touch', pointerId: 10, bubbles: true, composed: true };
      const below = { clientX: box.left + 2, clientY: sheet.getScrollContainer().getBoundingClientRect().bottom + 40 };
      corner.dispatchEvent(
        new PointerEvent('pointerdown', { ...common, cancelable: true, clientX: box.left + 2, clientY: box.top + 2 })
      );

      document.dispatchEvent(new PointerEvent('pointermove', { ...common, ...below }));

      // Nothing yet: the first step of the scrolling takes the drag to the edge.
      expect(sheet.getSelection().activeRange).to.eql({ c1: 1, c2: 1, r1: 1, r2: 1 });
      document.dispatchEvent(new PointerEvent('pointerup', { ...common, ...below }));
    } finally {
      cleanup();
    }
  });

  it('fills from a fill handle once Fill is chosen', async () => {
    const { host, sheet, cleanup } = attach();
    const events: Array<{ type: string; data: unknown }> = [];
    host.addEventListener('spreadsheet-event', (e: Event) => events.push((e as CustomEvent).detail));
    try {
      await sheet.getCellGrid().updateComplete;
      const cell = sheet.getCellGrid().shadowRoot!.querySelector('.cell.col2.row2')!;
      cell.dispatchEvent(new PointerEvent('pointerdown', { pointerType: 'touch', bubbles: true, composed: true }));
      sheet.getCellGrid().dispatchEvent(
        new CustomEvent('cell-click', {
          detail: { col: 2, row: 2, shiftKey: false, ctrlKey: false, metaKey: false, altKey: false },
          bubbles: true,
          composed: true
        })
      );
      await frame();
      expect(command(host).hidden, 'offered after the tap').to.be.false;
      command(host).querySelector('button')!.click();
      await frame();
      expect(outline(sheet).className).to.equal('sheet-selection touch fill');
      const bottom = sheet.getCellGrid().shadowRoot!.querySelector('.fill-touch-square.bottom')!;
      const target = cellBox(sheet, 2, 5);

      touchDrag(bottom, [{ x: target.left + 5, y: target.top + 5 }]);

      expect(events.at(-1)).to.eql({ type: 'selectionIncreasePainted', data: { r1: 2, c1: 2, r2: 5, c2: 2 } });
      await frame();
      expect(outline(sheet).className, 'back to selecting').to.equal('sheet-selection touch');
    } finally {
      cleanup();
    }
  });
});
