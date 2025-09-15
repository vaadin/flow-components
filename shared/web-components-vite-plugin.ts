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
        },
        // The following dependencies are imported both in the external web-components, and in Flow application sources.
        // Vite always resolves dependencies from where they are imported, so these dependencies would then be bundled
        // twice. To avoid that, use dedupe. Dedupe only works for non-optimized dependencies, so they also need to be
        // excluded from optimization / pre-bundling.
        optimizeDeps: {
          exclude: ['lit', 'lit-html', 'ol']
        },
        resolve: {
          dedupe: ['lit', 'lit-html', 'ol'],
        }
      };
    },
    resolveId(id: string) {
      if (id.startsWith('@vaadin')) {
        return this.resolve(path.join(nodeModulesPath, id));
      }
    }
  };
}
