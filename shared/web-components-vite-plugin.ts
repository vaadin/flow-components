import path from 'path';
import { PluginOption } from 'vite';

/**
 * Vite plugin that resolves Vaadin component JS modules to a
 * local checkout of the web-components repository
 *
 * @param webComponentsRepoPath
 */
export function useLocalWebComponents(webComponentsRepoPath: string): PluginOption {
  const nodeModulesPath = path.resolve(__dirname, '../', `${webComponentsRepoPath}/node_modules`);

  return {
    name: 'use-local-web-components',
    enforce: 'pre',
    config() {
      return {
        server: {
          fs: {
            allow: [nodeModulesPath]
          },
          watch: {
            ignored: [`!${nodeModulesPath}/**`]
          }
        }
      };
    },
    resolveId(id) {
      if (/^(@polymer|@vaadin)/.test(id)) {
        return this.resolve(path.join(nodeModulesPath, id));
      }
    }
  };
}

/**
 * Vite plugin that resolves Vaadin web components to Lit versions
 */
export function useLitWebComponents(): PluginOption {
  return {
    name: 'use-lit-web-components',
    config() {
      return {
        resolve: {
          alias: [
            'accordion',
            'accordion-panel',
            'app-layout',
            'drawer-toggle',
            'avatar',
            'avatar-group',
            'board',
            'board-row',
            'button',
            'chart',
            'chart-series',
            'checkbox',
            'checkbox-group',
            'combo-box',
            'confirm-dialog',
            'context-menu',
            'cookie-consent',
            'crud',
            'crud-edit',
            'crud-edit-column',
            'custom-field',
            'date-picker',
            'date-time-picker',
            'details',
            'dialog',
            'email-field',
            'form-layout',
            'form-item',
            'form-row',
            'grid',
            'grid-column',
            'grid-column-group',
            'grid-filter',
            'grid-filter-column',
            'grid-selection-column',
            'grid-sort-column',
            'grid-sorter',
            'grid-tree-column',
            'grid-tree-toggle',
            'grid-pro',
            'grid-pro-edit-column',
            'horizontal-layout',
            'icon',
            'integer-field',
            'item',
            'list-box',
            'login-form',
            'login-overlay',
            'map',
            'menu-bar',
            'menu-bar-item',
            'message-list',
            'message-input',
            'multi-select-combo-box',
            'notification',
            'number-field',
            'password-field',
            'progress-bar',
            'radio-button',
            'radio-group',
            'rich-text-editor',
            'scroller',
            'select',
            'split-layout',
            'tabs',
            'tab',
            'tabsheet',
            'text-area',
            'text-field',
            'time-picker',
            'upload',
            'vertical-layout',
            'virtual-list'
          ].flatMap((component) => {
            return [
              {
                find: new RegExp(`^@vaadin/([^\/]+)\/vaadin-${component}.js`),
                replacement: `@vaadin/$1/vaadin-lit-${component}.js`
              },
              {
                find: new RegExp(`^@vaadin/([^\/]+)\/src/vaadin-${component}.js`),
                replacement: `@vaadin/$1/src/vaadin-lit-${component}.js`
              },
              {
                find: new RegExp(`^@vaadin/([^\/]+)\/theme/lumo/vaadin-${component}.js`),
                replacement: `@vaadin/$1/theme/lumo/vaadin-lit-${component}.js`
              },
              {
                find: new RegExp(`^@vaadin/([^\/]+)\/theme/material/vaadin-${component}.js`),
                replacement: `@vaadin/$1/theme/material/vaadin-lit-${component}.js`
              }
            ];
          })
        }
      };
    }
  };
}
