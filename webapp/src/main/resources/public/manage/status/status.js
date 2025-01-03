async function updateSiteActive(siteId) {
  const active = document.getElementById("activeYes").checked;

  const confirmationDiv = event.target.parentNode.parentNode.querySelector("div[class=confirm-message]")
  const errorDiv = event.target.parentNode.parentNode.querySelector("div[class=errorMessage]")

  try {
    await sendStatusUpdate(siteId, "active", active);
    if(active) {
      await sendStatusUpdate(siteId, "inactiveReason", "");
      showConfirmation(confirmationDiv, errorDiv,"Site status set to active");
    } else {
      const inactiveReason = document.getElementById("inactive-reason").value.trim();
      await sendStatusUpdate(siteId, "inactiveReason", inactiveReason);
      showConfirmation(confirmationDiv, errorDiv, "Site status set to inactive" + (inactiveReason !== "" ? " with reason " + inactiveReason : ""));
    }
  } catch (error) {
    showStatusError(confirmationDiv, errorDiv, error, "site 'active' was not updated.")
  }
}

function showConfirmation(confirmationDiv, errorDiv, text) {
  confirmationDiv
      .innerHTML =
      `<span class="green-check" id="update-active-confirm">&#10003;</span> ${text}`;
  errorDiv.innerHTML = "";
}

function showStatusError(confirmationDiv, errorDiv, error, messageText) {
  console.log(error);
  confirmationDiv.innerHTML = "";
  errorDiv.innerHTML = "An error occurred, " + messageText;
}

async function updateSiteAcceptingDonations(siteId) {
  const accepting = document.getElementById("acceptingYes").checked;
  const confirmationDiv = event.target.parentNode.parentNode.querySelector("div[class=confirm-message]")
  const errorDiv = event.target.parentNode.parentNode.querySelector("div[class=errorMessage]")

  try {
    await sendStatusUpdate(siteId, "acceptingSupplies", accepting);
    showConfirmation(confirmationDiv, errorDiv, "Site status set to " + (accepting ? "" : "NOT ") + "accepting supplies");
  } catch (error) {
    showStatusError(confirmationDiv, errorDiv, error, "accepting supplies was not updated.");
  }
}

async function updateSiteDistributingDonations(siteId) {
  const distributing = document.getElementById("distributingYes").checked;
  const confirmationDiv = event.target.parentNode.parentNode.querySelector("div[class=confirm-message]")
  const errorDiv = event.target.parentNode.parentNode.querySelector("div[class=errorMessage]")

  try {
    await sendStatusUpdate(siteId, "distributingSupplies", distributing);
    showConfirmation(confirmationDiv, errorDiv, "Site status set to " + (distributing ? "" : "NOT ") + "distributing supplies");
  } catch (error) {
    showStatusError(confirmationDiv, errorDiv, error, "distributing supplies was not updated");
  }
}


async function updateSiteSiteType(siteId) {
  const isDistSite = document.getElementById("distributionCenter").checked;
  const confirmationDiv = event.target.parentNode.parentNode.querySelector("div[class=confirm-message]")
  const errorDiv = event.target.parentNode.parentNode.querySelector("div[class=errorMessage]")
  try {
    await sendStatusUpdate(siteId, "distSite", isDistSite);
    showConfirmation(confirmationDiv, errorDiv, "Site type set to " + (isDistSite ? "distribution site" : "supply warehouse"));
  } catch (error) {
    showStatusError(confirmationDiv, errorDiv, error, "site type was not updated");
  }
}


async function updatePubliclyVisible(siteId) {
  const publicVisible = document.getElementById("publicYes").checked;

  const confirmationDiv = event.target.parentNode.parentNode.querySelector("div[class=confirm-message]")
  const errorDiv = event.target.parentNode.parentNode.querySelector("div[class=errorMessage]")

  try {
    await sendStatusUpdate(siteId, "publiclyVisible", publicVisible);
    showConfirmation(confirmationDiv, errorDiv, "Site set to " + (publicVisible ? "publicly visible" : "visible to logged in users only"));
  } catch (error) {
    showStatusError(confirmationDiv, errorDiv, error, "publicly visible was not updated");
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


function hideInactiveReason() {
  document.getElementById('inactive-div').classList.add("hidden");
}
function showInactiveReason() {
  document.getElementById('inactive-div').classList.remove("hidden");
  window.scrollTo(0, document.body.scrollHeight);
}
