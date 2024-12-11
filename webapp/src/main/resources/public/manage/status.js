async function updateSiteActive(siteId) {
  const active = document.getElementById("activeYes").checked;
  try {
    await sendStatusUpdate(siteId, "active", active);
    showConfirmation("Site status set to " + (active ? "active" : "inactive"));
  } catch (error) {
    showError(error, "site 'active' was not updated.")
  }
}

function showConfirmation(text) {
  document.getElementById("updateStatusConfirmation")
      .innerHTML =
      `<span class="green-check" id="update-active-confirm">&#10003;</span> ${text}`;
  document.getElementById("status-update-error").innerHTML = "";
}

function showError(error, messageText) {
  console.log(error);
  document.getElementById("updateStatusConfirmation")
      .innerHTML = "";
  document.getElementById("status-update-error")
      .innerHTML = "An error occurred, " + messageText;
}

async function updateSiteAcceptingDonations(siteId) {
  const accepting = document.getElementById("acceptingYes").checked;
  try {
    await sendStatusUpdate(siteId, "acceptingSupplies", accepting);
    showConfirmation("Site status set to " + (accepting ? "" : "NOT ") + "accepting supplies");
  } catch (error) {
    showError(error, "accepting supplies was not updated.");
  }
}

async function updateSiteDistributingDonations(siteId) {
  const distributing = document.getElementById("distributingYes").checked;
  try {
    await sendStatusUpdate(siteId, "distributingSupplies", distributing);
    showConfirmation("Site status set to " + (distributing ? "" : "NOT ") + "distributing supplies");
  } catch (error) {
    showError(error, "distributing supplies was not updated");
  }
}


async function updateSiteSiteType(siteId) {
  const isDistSite = document.getElementById("distributionCenter").checked;
  try {
    await sendStatusUpdate(siteId, "distSite", isDistSite);
    showConfirmation("Site type set to " + (isDistSite ? "distribution site" : "supply warehouse"));
  } catch (error) {
    showError(error, "site type was not updated");
  }
}


async function updatePubliclyVisible(siteId) {
  const publicVisible = document.getElementById("publicYes").checked;
  try {
    await sendStatusUpdate(siteId, "publiclyVisible", publicVisible);
    showConfirmation("Site set to " + (publicVisible ? "publicly visible" : "visible to logged in users only"));
  } catch (error) {
    showError(error, "publicly visible was not updated");
  }
}

async function updateOnboarded(siteId) {
  const onboarded = document.getElementById("onboardedYes").checked;
  try {
    await sendStatusUpdate(siteId, "onboarded", onboarded);
    showConfirmation("Site set to " + (onboarded ? "onboarded" : "not onboarded"));
  } catch (error) {
    showError(error, "onboarded was not updated");
  }
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
