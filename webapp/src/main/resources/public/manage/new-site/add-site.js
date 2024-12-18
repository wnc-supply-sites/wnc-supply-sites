async function saveNewSite() {

  /* Required Fields */
  const siteName = document.getElementById("Site Name");
  const streetAddress = document.getElementById("Street Address");
  const cityField = document.getElementById("City");
  const contactNumber = document.getElementById("Contact Number");

  let validData = true;
  validData = checkField(siteName) && validData;
  validData = checkField(streetAddress) && validData;
  validData = checkField(cityField) && validData;
  validData = checkField(contactNumber) && validData;

  /* Drop Down Selection Fields */
  const stateField = document.getElementById("State");
  const countyField = document.getElementById("County");

  const website = document.getElementById("Website");
  const facebook = document.getElementById("Facebook");
  const siteType = document.getElementById("distributionCenterRadio")
      .checked ? "Distribution Center" : "Supply Hub";
  const siteHours = document.getElementById("Site Hours");

  const hasForkLift = document.getElementById('hasForklift');
  const hasLoadingDock = document.getElementById('hasLoadingDock');
  const hasIndoorStorage = document.getElementById('hasIndoorStorage');
  const maxSupplyLoad = document.getElementById('maxSupplyLoad');
  const receivingNotes = document.getElementById('receivingNotes');

  const contactName = document.getElementById("Contact Name");
  const contactEmail = document.getElementById("Contact Email");
  const additionalContacts = document.getElementById("Additional Contacts");


  if (!validData) {
    showError("Missing required fields")
    return;
  }

  const url = "/manage/add-site";
  fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteName: siteName.value.trim(),
      streetAddress: streetAddress.value.trim(),
      city: cityField.value.trim(),
      state: stateField.value.trim(),
      county: countyField.value.trim(),
      website: website.value.trim(),
      facebook: facebook.value.trim(),
      siteType: siteType.trim(),
      siteHours: siteHours.value.trim(),

      hasForklift: hasForkLift.checked,
      hasLoadingDock: hasLoadingDock.checked,
      hasIndoorStorage: hasIndoorStorage.checked,
      maxSupplyLoad: maxSupplyLoad.value,
      receivingNotes: receivingNotes.value.trim(),

      contactName: contactName.value.trim(),
      contactNumber: contactNumber.value.trim(),
      contactEmail: contactEmail.value.trim(),
      additionalContacts: additionalContacts.value.trim()
    })
  })
  .then(
      async function (response) {
        if (response.ok) {
          const responseJson = await response.json();
          const editNewSiteInventoryUrl = responseJson.editSiteInventoryUrl;
          showSuccess(editNewSiteInventoryUrl, siteName.value);
        } else {
          const responseJson = await response.json();
          showError("Failed to save: " + responseJson.error);
        }
        window.scrollTo(0, document.body.scrollHeight);
      },
      function (error) {
        showError("Failed to save, server not available.");
        window.scrollTo(0, document.body.scrollHeight);
      });
}

function showError(text) {
  const confirmation = document.getElementById("save-site-confirm");
  const redX = document.getElementById("red-x");
  const greenCheck = document.getElementById("green-check")
  const confirmMessage = document.getElementById("save-site-confirm");

  greenCheck.classList.add("hidden");
  redX.classList.remove("hidden");
  confirmMessage.classList.add("errorMessage");
  confirmation.innerHTML = text;
}

function showSuccess(editSiteUrl, siteName) {
  const confirmation = document.getElementById("save-site-confirm");
  const redX = document.getElementById("red-x");
  const greenCheck = document.getElementById("green-check")
  const confirmMessage = document.getElementById("save-site-confirm");

  greenCheck.classList.remove("hidden");
  redX.classList.add("hidden");
  confirmMessage.classList.remove("errorMessage");
  confirmation.innerHTML = `${siteName} saved. <a href=${editSiteUrl}>Click to set up inventory</a>`

  /** Clear data entry fields to facilitate adding more sites. */
  document.getElementById("Site Name").value = "";
  document.getElementById("Street Address").value = "";
  document.getElementById("City").value = "";
  document.getElementById("Website").value = "";
  document.getElementById("Facebook").value = "";
  document.getElementById("Site Hours").value = "";

  document.getElementById('hasForklift').checked = false;
  document.getElementById('hasLoadingDock').checked = false;
  document.getElementById('hasIndoorStorage').checked = false;
  document.getElementById('receivingNotes').value = "";

  document.getElementById("Contact Name").value = "";
  document.getElementById("Contact Number").value = "";
  document.getElementById("Contact Email").value = "";
  document.getElementById("Additional Contacts").value = "";
}

/** Returns false if field is not set, true if field is set */
function checkField(fieldElement) {
  if (fieldElement.value.trim() === "") {
    fieldElement.classList.add("missingData");
    return false;
  } else {
    fieldElement.classList.remove("missingData");
    return true;
  }
}

