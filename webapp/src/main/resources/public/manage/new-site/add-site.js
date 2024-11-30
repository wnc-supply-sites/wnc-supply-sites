async function saveNewSite() {
  const contactNumber = document.getElementById("Contact Number");
  const website = document.getElementById("Website");
  const siteType = document.getElementById("distributionCenterRadio")
      .checked ? "Distribution Center" : "Supply Hub";

  const siteName = document.getElementById("Site Name");
  const streetAddress = document.getElementById("Street Address");
  const cityField = document.getElementById("City");
  const countyField = document.getElementById("County");
  const stateField = document.getElementById("State");

  let validData = true;
  validData = checkField(siteName) && validData;
  validData = checkField(streetAddress) && validData;
  validData = checkField(cityField) && validData;
  validData = checkField(countyField) && validData;
  validData = checkField(stateField) && validData;

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
      contactNumber: contactNumber.value.trim(),
      website: website.value.trim(),
      siteType: siteType.trim(),
      siteName: siteName.value.trim(),
      streetAddress: streetAddress.value.trim(),
      city: cityField.value.trim(),
      county: countyField.value.trim(),
      state: stateField.value.trim()
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
      },
      function (error) {
        showError("Failed to save, server not available.");
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
  document.getElementById("Contact Number").value = "";
  document.getElementById("Website").value = "";
  document.getElementById("Site Name").value = "";
  document.getElementById("Street Address").value = "";
  document.getElementById("City").value = "";
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

