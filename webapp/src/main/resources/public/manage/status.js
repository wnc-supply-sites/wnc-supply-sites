async function updateSiteActive(siteId) {
  const active = document.getElementById("activeYes").checked;
  try {
    await sendStatusUpdate(siteId, "active", active);
    document.getElementById("updateStatusConfirmation")
        .innerHTML =
        `<span class="green-check" id="update-active-confirm">&#10003;</span>` +
        "Site status set to " + (active ? "active" : "inactive");
  } catch (error) {
    console.log(error);
    document.getElementById("status-update-error")
        .innerHTML = "An error occurred, site status was not updated.";
  }
}

async function updateSiteAcceptingDonations(siteId) {
  const accepting = document.getElementById("acceptingYes").checked;
  try {
    await sendStatusUpdate(siteId, "acceptingDonations", accepting);
    document.getElementById("updateStatusConfirmation")
        .innerHTML =
        `<span class="green-check" id="update-active-confirm">&#10003;</span>` +
        "Site status set to " + (accepting ? "" : "NOT ") + "accepting donations";
  } catch (error) {
    console.log(error);
    document.getElementById("status-update-error")
        .innerHTML = "An error occurred, site status was not updated.";
  }
}

async function updateSiteSiteType(siteId) {
  const isDistSite = document.getElementById("distributionCenter").checked;
  await sendStatusUpdate(siteId, "distSite", isDistSite);
  document.getElementById("updateStatusConfirmation")
      .innerHTML =
      `<span class="green-check" id="update-active-confirm">&#10003;</span>` +
      "Site type set to " + (isDistSite ? "distribution site" : "supply hub");
}

async function sendStatusUpdate(siteId, statusFlag, newValue) {
  const url = "/manage/update-status";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      statusFlag: statusFlag,
      newValue: newValue
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response}`);
  }
  return await response.text();
}
