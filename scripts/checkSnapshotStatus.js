const benderBaseUrl = 'https://bender.vaadin.com';
const benderBuildType = {
  snapshot: 'FlowComponents_Snapshot'
};
const ghRepo = 'vaadin/flow-components';
const ghWorkflow = 'validation.yml';
const branches = process.env.CHECK_SNAPSHOT_STATUS_BRANCHES;
const benderToken = process.env.CHECK_SNAPSHOT_STATUS_BENDER_TOKEN;
const slackWebhookUrl = process.env.CHECK_SNAPSHOT_STATUS_SLACK_WEBHOOK_URL;

async function getBuildStatus() {
  const baseBuildUrl = `${benderBaseUrl}/app/rest/builds?locator=buildType:`;
  const branchNames = branches.split(',').map(branch => branch.trim()).filter(Boolean);

  const branchUrls = branchNames.map((branch) =>
    `${baseBuildUrl}${benderBuildType.snapshot},branch:${branch}`
  );

  const buildResults = await Promise.all(branchUrls.map((url) => getLatestBuild(url)));
  const status = buildResults.filter(build => !!build).map((latestBuild) => getBuildDetails(latestBuild));

  const ghaStatus = await getGhaScheduleStatus();
  if (ghaStatus) status.push(ghaStatus);

  return status;
}

async function getGhaScheduleStatus() {
  const url = `https://api.github.com/repos/${ghRepo}/actions/workflows/${ghWorkflow}/runs?event=schedule&per_page=1`;
  const response = await fetch(url, { headers: { Accept: 'application/vnd.github+json' } });
  const data = await response.json();
  const run = data.workflow_runs?.[0];
  if (!run) return null;
  return {
    branch: 'Nightly validation (GHA)',
    branchUrl: run.html_url,
    status: run.conclusion === 'success' ? 'SUCCESS' : 'FAILURE'
  };
}

async function getLatestBuild(url) {
  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${benderToken}`,
      Accept: 'application/json'
    }
  });
  const latestBuilds = await response.json();
  const latestBuild = latestBuilds.build[0];

  return latestBuild;
}

function getBuildDetails(latestBuild) {
  const branch = latestBuild.branchName;
  const branchUrl = `${benderBaseUrl}/buildConfiguration/${latestBuild.buildTypeId}${
    branch ? `?branch=${branch === 'main' ? '%3Cdefault%3E' : branch}` : ''
  }`;

  return {
    branch: branch ? branch : 'Latest WC main',
    branchUrl,
    status: latestBuild.status
  };
}

async function postToSlack(status) {
  const statusMessage = status
    .map((result) => {
      const statusIcon = result.status === 'SUCCESS' ? '🟢' : '🔴';
      const link = `<${result.branchUrl}|${result.branch}>`;
      return `${statusIcon} ${link}`;
    })
    .join(' | ');

  console.log('Status message', statusMessage);

  const payload = {
    text: `${statusMessage}`,
    icon_emoji: ':teamcity:'
  };

  await fetch(slackWebhookUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  console.log('💌 Status sent to Slack');
}

async function run() {
  if (!branches) {
    console.error('`CHECK_SNAPSHOT_STATUS_BRANCHES` is not configured in environment variables.');
    return;
  }
  if (!benderToken) {
    console.error('`CHECK_SNAPSHOT_STATUS_BENDER_TOKEN` is not configured in environment variables.');
    return;
  }
  if (!slackWebhookUrl) {
    console.error('`CHECK_SNAPSHOT_STATUS_SLACK_WEBHOOK_URL` is not configured in environment variables.');
    return;
  }

  const status = await getBuildStatus();

  await postToSlack(status);
}

run();
