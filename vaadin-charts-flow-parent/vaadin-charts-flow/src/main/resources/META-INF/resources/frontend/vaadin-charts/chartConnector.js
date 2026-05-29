/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

// Resolves a Highcharts series by its internal, Vaadin-controlled identity
// (top-level vaadinReactiveId series option, preserved by Highcharts across
// updates), which is independent of the user-facing public id.
function findSeries(chart, vid) {
  const hc = chart.configuration;
  if (!hc) {
    return undefined;
  }
  return hc.series.find((s) => s.options && s.options.vaadinReactiveId === vid);
}

function init(chartElement) {
  chartElement.$connector = {
    /**
     * Applies a coalesced, ordered batch of id-keyed series/point operations,
     * then redraws once. Operations are addressed by the internal reactive id
     * (not positional index), so a batch mixing add/remove/update reconciles
     * correctly regardless of intervening structural changes.
     *
     * @param ops array of { op, vid, ... } operations
     */
    syncSeries(ops) {
      const hc = chartElement.configuration;
      if (!hc || !ops) {
        return;
      }
      ops.forEach((op) => {
        switch (op.op) {
          case 'addSeries':
            // op.config already carries custom.vaadinReactiveId from the server
            hc.addSeries(op.config, false, false);
            break;
          case 'updateSeries': {
            const s = findSeries(chartElement, op.vid);
            if (s) {
              s.update(op.config, false);
            }
            break;
          }
          case 'removeSeries': {
            const s = findSeries(chartElement, op.vid);
            if (s) {
              s.remove(false);
            }
            break;
          }
          case 'setSeriesVisible': {
            const s = findSeries(chartElement, op.vid);
            if (s) {
              s.setVisible(op.visible, false);
            }
            break;
          }
          case 'addPoint': {
            const s = findSeries(chartElement, op.vid);
            if (s) {
              s.addPoint(op.point, false, op.shift);
            }
            break;
          }
          case 'removePoint': {
            const s = findSeries(chartElement, op.vid);
            if (s && s.data[op.index]) {
              s.data[op.index].remove(false);
            }
            break;
          }
          case 'updatePoint': {
            const s = findSeries(chartElement, op.vid);
            if (s && s.data[op.index]) {
              s.data[op.index].update(op.value, false);
            }
            break;
          }
          case 'slicePoint': {
            const s = findSeries(chartElement, op.vid);
            if (s && s.data[op.index]) {
              s.data[op.index].slice(op.sliced, false, op.animation);
            }
            break;
          }
          default:
            break;
        }
      });
      hc.redraw();
    }
  };
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.chartConnector = { init };
