const benderBaseUrl = 'https://bender.vaadin.com';
const benderBuildType = {
  snapshot: 'FlowComponents_Snapshot',
  latestWc: 'VaadinFlowComponents_WcFcNightlyValidation'
};
const branches = process.env.CHECK_SNAPSHOT_STATUS_BRANCHES;
const benderToken = process.env.CHECK_SNAPSHOT_STATUS_BENDER_TOKEN;
const slackWebhookUrl = process.env.CHECK_SNAPSHOT_STATUS_SLACK_WEBHOOK_URL;

async function getBuildStatus() {
  const baseBuildUrl = `${benderBaseUrl}/app/rest/builds?locator=buildType:`;
  const branchNames = branches.split(',').map(branch => branch.trim()).filter(Boolean);

  const branchUrls = branchNames.map((branch) =>
    `${baseBuildUrl}${benderBuildType.snapshot},branch:${branch}`
  );
  branchUrls.push(`${baseBuildUrl}${benderBuildType.latestWc}`);

  const buildPromises = branchUrls.map((url) => getLatestBuild(url));
  const buildResults = await Promise.all(buildPromises);

  const status = buildResults.filter(build => !!build).map((latestBuild) => getBuildDetails(latestBuild));

  return status;
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
