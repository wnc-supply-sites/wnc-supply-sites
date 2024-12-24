
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
