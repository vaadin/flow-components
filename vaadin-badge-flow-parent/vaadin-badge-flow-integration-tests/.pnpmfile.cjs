// Exclude Vaadin's own packages from pnpm's minimum release age check
// (minimum-release-age, set by Flow's build), so that freshly released
// @vaadin/* web component versions can be installed right away. Kept in
// .pnpmfile.cjs instead of pnpm-workspace.yaml because pnpm rewrites the
// latter on install.
module.exports = {
  hooks: {
    updateConfig(config) {
      config.minimumReleaseAgeExclude ??= [];
      config.minimumReleaseAgeExclude.push('@vaadin/*');
      return config;
    },
  },
};
