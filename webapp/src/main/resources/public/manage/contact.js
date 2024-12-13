/**
 * Sends an update of one field to webserver.
 * On success, shows a green checkbox next to the update
 * button, and below the update button shows a confirmation
 * message. On error, the messaging indicates an error happened.
 */
async function showUpdateConfirmation(siteId, field) {
  let newValue;

  if (field === 'County' || field === 'State') {
    newValue =
        document.getElementById('County').value + ',' + document.getElementById('State').value;
  } else {
    newValue = document.getElementById(field).value;
  }

  try {
    await sendSiteUpdate(siteId, field, newValue)
    showConfirmations(field, newValue);
  } catch (error) {
    showError(field, error);
  }
}

/** Invoke this when we have done the update to webserver and need
 * to show confirmation messaging to the user.
 */
function showConfirmations(field, newValue) {
  document.getElementById(field + "-update-confirm-checkmark").style.display = 'block';

  setTimeout(function () {
        document.getElementById(field + "-update-confirm-checkmark").style.display = 'none';
      },
      1500);

  if (newValue === "") {
    document.getElementById(field + "-update-confirm").innerHTML =
        `${field} was deleted`;
  } else if (newValue === "Updated") {
    document.getElementById(field + "-update-confirm").innerHTML =
        `${field} was successfully updated`;
  } else {
    document.getElementById(field + "-update-confirm").innerHTML =
        `${field} updated to: ${newValue}`;
  }
}

function showError(field, error) {
  console.log(error);
  document.getElementById(field + "-update-confirm").innerHTML =
      `An error occurred, ${field} was not updated`;
}

/** Does webservice call to send a single updated value to the webserver */
async function sendSiteUpdate(siteId, field, newValue) {
  const url = "/manage/update-site";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      field: field,
      newValue: newValue
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response}`);
  }
  return await response.text();
}

/** Invoked when we update the receiving capabilities checkboxes.
 * All other fields go through 'showUpdateConfirmation'.
 * This method special cases the checkboxes and handles those
 * in a hardcoded manner. The other form submit buttons
 * send one updated field value at a time. This method
 * sends an update for all three checkboxes at once.
 */
async function updateReceivingCapabilities(siteId) {

  try {
    await sendSiteReceivingUpdates(siteId);
    showConfirmations('receiving capabilities', 'Updated')
  } catch (error) {
    showError('receiving capabilities', error);
  }
}

async function sendSiteReceivingUpdates(siteId) {
  const hasForklift = document.getElementById("hasForklift").checked;
  const hasLoadingDock = document.getElementById("hasLoadingDock").checked;
  const hasIndoorStorage = document.getElementById("hasIndoorStorage").checked;

  const url = "/manage/update-site-receiving";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      hasForkLift: hasForklift,
      hasLoadingDock: hasLoadingDock,
      hasIndoorStorage: hasIndoorStorage
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response}`);
  }
  return await response.text();
}
