import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, initSelectionColumn, setRootItems } from './shared.js';
import type { FlowGrid, FlowGridSelectionColumn } from './shared.js';

type Checkbox = HTMLElement & { accessibleName: string };

describe('grid connector - selection column accessible names', () => {
  let grid: FlowGrid;
  let selectionColumn: FlowGridSelectionColumn;

  function allCheckboxes(): Checkbox[] {
    return [...grid.querySelectorAll<Checkbox>('vaadin-checkbox')];
  }

  function selectAllCheckbox(): Checkbox {
    return allCheckboxes()[0];
  }

  function rowCheckboxes(): Checkbox[] {
    const [, ...rows] = allCheckboxes();
    return rows;
  }

  async function enterMultiSelectMode() {
    grid.$connector.setSelectionMode('MULTI');
    await nextFrame();
  }

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-flow-selection-column></vaadin-grid-flow-selection-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);
    init(grid);

    selectionColumn = grid.querySelector('vaadin-grid-flow-selection-column')!;
    initSelectionColumn(grid, selectionColumn);

    // First item carries a server-generated full aria-label, the second one
    // does not (no generator value for it).
    setRootItems(grid.$connector, [
      { key: '0', name: 'foo', selectRowCheckboxAriaLabel: 'Select Row Foo' },
      { key: '1', name: 'bar' }
    ]);
    await nextFrame();
  });

  it('should use the per-item selectRowCheckboxAriaLabel as the row checkbox name', async () => {
    await enterMultiSelectMode();
    expect(rowCheckboxes()[0].accessibleName).to.equal('Select Row Foo');
  });

  it('should fall back to the default name for items without a selectRowCheckboxAriaLabel', async () => {
    await enterMultiSelectMode();
    expect(rowCheckboxes()[1].accessibleName).to.equal('Select Row');
  });

  it('should use the select all checkbox accessible name', async () => {
    selectionColumn.selectAllAccessibleName = 'Select All Items';
    await enterMultiSelectMode();
    expect(selectAllCheckbox().accessibleName).to.equal('Select All Items');
  });
});
