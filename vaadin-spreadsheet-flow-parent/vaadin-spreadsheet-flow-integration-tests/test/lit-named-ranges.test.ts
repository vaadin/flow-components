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

/**
 * The named-range picker in the formula bar.
 *
 * The rendered option text is asserted here, not just the values. Maven
 * resource filtering once rewrote the `${name}` binding in the template to
 * the literal project name, so every option read "Vaadin Spreadsheet" while
 * its `value` stayed correct — invisible to any test that only checks
 * values. See ConnectorResourceFilteringTest for the build-side guard.
 */

let hostSeq = 0;

function attachSheet(): {
  sheet: Spreadsheet;
  cleanup: () => void;
} {
  const host = document.createElement('div');
  host.id = `named-range-host-${hostSeq++}`;
  host.style.cssText = 'width:400px;height:300px;display:flex;flex-direction:column;';
  document.body.appendChild(host);
  const renderTarget = host.attachShadow({ mode: 'open' });
  const sheet = new Spreadsheet(host, renderTarget);
  sheet.setCols(10);
  sheet.setRows(10);
  sheet.setColW(new Array(10).fill(50));
  sheet.setRowH(new Array(10).fill(15));
  sheet.setHeaderRowHeight(20);
  sheet.setHeaderColumnWidth(40);
  return {
    sheet,
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

function namedRangeBox(sheet: Spreadsheet): HTMLSelectElement | null {
  return sheet.getFormulaBar().shadowRoot!.querySelector('select.namedrangebox');
}

async function options(sheet: Spreadsheet): Promise<HTMLOptionElement[]> {
  await sheet.getFormulaBar().updateComplete;
  const box = namedRangeBox(sheet);
  return box ? Array.from(box.options) : [];
}

describe('Spreadsheet — named-range picker', () => {
  let ctx: ReturnType<typeof attachSheet>;

  beforeEach(() => {
    ctx = attachSheet();
  });

  afterEach(() => {
    ctx.cleanup();
  });

  it('renders no picker while the workbook defines no named range', async () => {
    ctx.sheet.setNamedRanges([]);
    expect(await options(ctx.sheet)).to.be.empty;
    expect(namedRangeBox(ctx.sheet)).to.be.null;
  });

  it('lists every named range, behind a blank first entry', async () => {
    ctx.sheet.setNamedRanges(['john', 'local', 'numbers', 'sheet2']);
    const rendered = await options(ctx.sheet);
    expect(rendered.map((o) => o.value)).to.eql(['', 'john', 'local', 'numbers', 'sheet2']);
    expect(rendered.map((o) => o.textContent!.trim())).to.eql([
      '',
      'john',
      'local',
      'numbers',
      'sheet2'
    ]);
  });

  it('starts with nothing selected', async () => {
    ctx.sheet.setNamedRanges(['john', 'local']);
    await ctx.sheet.getFormulaBar().updateComplete;
    expect(namedRangeBox(ctx.sheet)!.value).to.equal('');
  });

  it('accepts the list as the JSON string Flow sends on the property lane', async () => {
    ctx.sheet.setNamedRanges('["john","local"]' as unknown as string[]);
    const rendered = await options(ctx.sheet);
    expect(rendered.map((o) => o.value)).to.eql(['', 'john', 'local']);
  });

  it('drops the picker again when the ranges are cleared', async () => {
    ctx.sheet.setNamedRanges(['john']);
    expect(await options(ctx.sheet)).to.have.length(2);
    ctx.sheet.setNamedRanges([]);
    expect(await options(ctx.sheet)).to.be.empty;
    expect(namedRangeBox(ctx.sheet)).to.be.null;
  });

  it('reports the picked range as a sheet address change', async () => {
    ctx.sheet.setNamedRanges(['john', 'local']);
    await ctx.sheet.getFormulaBar().updateComplete;

    const seen: string[] = [];
    ctx.sheet.getFormulaBar().addEventListener('sheet-address-changed', (e: Event) => {
      seen.push((e as CustomEvent).detail.value);
    });

    const box = namedRangeBox(ctx.sheet)!;
    box.value = 'local';
    box.dispatchEvent(new Event('change', { bubbles: true, composed: true }));

    expect(seen).to.eql(['local']);
  });

  it('reports a change dispatched straight at the select, without bubbling', async () => {
    // How TestBench drives the picker: setProperty + a bare Event. A
    // listener delegated from the shadow root never sees this one.
    ctx.sheet.setNamedRanges(['john', 'local']);
    await ctx.sheet.getFormulaBar().updateComplete;

    const seen: string[] = [];
    ctx.sheet.getFormulaBar().addEventListener('sheet-address-changed', (e: Event) => {
      seen.push((e as CustomEvent).detail.value);
    });

    const box = namedRangeBox(ctx.sheet)!;
    box.value = 'john';
    box.dispatchEvent(new Event('change'));

    expect(seen).to.eql(['john']);
  });

  it('ignores a change back to the blank entry', async () => {
    ctx.sheet.setNamedRanges(['john']);
    await ctx.sheet.getFormulaBar().updateComplete;

    const seen: string[] = [];
    ctx.sheet.getFormulaBar().addEventListener('sheet-address-changed', (e: Event) => {
      seen.push((e as CustomEvent).detail.value);
    });

    const box = namedRangeBox(ctx.sheet)!;
    box.value = '';
    box.dispatchEvent(new Event('change', { bubbles: true, composed: true }));

    expect(seen).to.be.empty;
  });

  it('shows the range the selection is on, and the blank entry anywhere else', async () => {
    // As the GWT client did: the picker follows the address field.
    ctx.sheet.setNamedRanges(['john', 'local']);
    ctx.sheet.updateFormulaBar('john', 1, 1);
    await ctx.sheet.getFormulaBar().updateComplete;
    expect(namedRangeBox(ctx.sheet)!.value).to.equal('john');

    ctx.sheet.updateFormulaBar(null, 8, 12);
    await ctx.sheet.getFormulaBar().updateComplete;
    expect(namedRangeBox(ctx.sheet)!.value).to.equal('');
  });

  it('lets a range be picked again once the selection has moved away', async () => {
    // A select only reports a change of option. Left on the range picked
    // before, picking it again after selecting elsewhere did nothing.
    ctx.sheet.setNamedRanges(['john', 'local']);
    await ctx.sheet.getFormulaBar().updateComplete;
    const box = namedRangeBox(ctx.sheet)!;
    box.value = 'john';
    box.dispatchEvent(new Event('change'));

    ctx.sheet.updateFormulaBar(null, 8, 12);
    await ctx.sheet.getFormulaBar().updateComplete;

    expect(box.value).to.equal('');
  });

  it('keeps the address and formula fields untouched by a range list update', async () => {
    ctx.sheet.getFormulaBar().renderState = {
      address: 'B2',
      formula: '=SUM(A1:A3)',
      namedRanges: []
    };
    ctx.sheet.setNamedRanges(['john']);
    await ctx.sheet.getFormulaBar().updateComplete;

    const root = ctx.sheet.getFormulaBar().shadowRoot!;
    expect(root.querySelector<HTMLInputElement>('.address')!.value).to.equal('B2');
    expect(root.querySelector<HTMLInputElement>('.formula')!.value).to.equal('=SUM(A1:A3)');
  });
});
