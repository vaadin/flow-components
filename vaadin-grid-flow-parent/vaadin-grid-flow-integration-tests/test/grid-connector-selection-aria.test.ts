import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, initSelectionColumn, setRootItems } from './shared.js';
import type { FlowGrid, FlowGridSelectionColumn } from './shared.js';

type Checkbox = HTMLElement & { accessibleName: string };

describe('grid connector - selection column accessible names', () => {
  let grid: FlowGrid;

  function checkboxes(): Checkbox[] {
    return [...grid.querySelectorAll<Checkbox>('vaadin-checkbox')];
  }

  function rowCheckboxes(): Checkbox[] {
    const [, ...rows] = checkboxes();
    return rows;
  }

  function setup(columnMarkup: string) {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-flow-selection-column></vaadin-grid-flow-selection-column>
        ${columnMarkup}
      </vaadin-grid>
    `);
    init(grid);
    const selectionColumn = grid.querySelector<FlowGridSelectionColumn>('vaadin-grid-flow-selection-column')!;
    initSelectionColumn(grid, selectionColumn);
    setRootItems(grid.$connector, [
      { key: '0', name: 'foo' },
      { key: '1', name: 'bar' }
    ]);
  }

  async function enterMultiSelectMode() {
    grid.$connector.setSelectionMode('MULTI');
    await nextFrame();
  }

  it('should use the row header cell text for the select row placeholder', async () => {
    setup('<vaadin-grid-column path="name" row-header></vaadin-grid-column>');
    grid.i18n = { selectRowCheckboxAriaLabel: 'Select row {0}' };
    await enterMultiSelectMode();
    expect(rowCheckboxes()[0].accessibleName).to.equal('Select row foo');
    expect(rowCheckboxes()[1].accessibleName).to.equal('Select row bar');
  });

  it('should fall back to the row index without a row header column', async () => {
    setup('<vaadin-grid-column path="name"></vaadin-grid-column>');
    grid.i18n = { selectRowCheckboxAriaLabel: 'Select row {0}' };
    await enterMultiSelectMode();
    expect(rowCheckboxes()[0].accessibleName).to.equal('Select row 1');
    expect(rowCheckboxes()[1].accessibleName).to.equal('Select row 2');
  });

  it('should use the select all checkbox accessible name from i18n', async () => {
    setup('<vaadin-grid-column path="name"></vaadin-grid-column>');
    grid.i18n = { selectAllCheckboxAriaLabel: 'Select All Items' };
    await enterMultiSelectMode();
    expect(checkboxes()[0].accessibleName).to.equal('Select All Items');
  });
});
