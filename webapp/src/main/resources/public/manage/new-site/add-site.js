async function saveNewSite() {
  const contactNumber = document.getElementById("Contact Number");
  const website = document.getElementById("Website");
  const siteType = document.getElementById("distributionCenterRadio")
      .checked ? "Distribution Center" : "Supply Hub";

  const siteName = document.getElementById("Site Name");
  const streetAddress = document.getElementById("Street Address");
  const cityField = document.getElementById("City");
  const countyField = document.getElementById("County");

  let validData = true;
  validData = checkField(siteName) && validData;
  validData = checkField(streetAddress) && validData;
  validData = checkField(cityField) && validData;
  validData = checkField(countyField) && validData;

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
      contactNumber: contactNumber.value,
      website: website.value,
      siteType: siteType,
      siteName: siteName.value,
      streetAddress: streetAddress.value,
      city: cityField.value,
      county: countyField.value
    })
  })
  .then(
      async function(response) {
        if(response.ok) {
          const editNewSiteInventoryUrl = response.json().editSiteInventoryUrl;
          showSuccess(editNewSiteInventoryUrl);

          // TODO: handle user error 400 (duplicate site name or something)
        } else if (response.status === 500) {
          const responseJson = await response.json();
          showError("Failed to save: " + response.status + ", " + responseJson.error);
        }
      },
      function(error) {
        showError("Failed to save, server not available.");
      });
}

function showError(text) {
  const confirmation = document.getElementById("save-site-confirm");
  const redX = document.getElementById("red-x");
  const greenCheck = document.getElementById("green-check")

  greenCheck.classList.add("hidden");
  redX.classList.remove("hidden");
  confirmation.innerHTML = text;
}

function showSuccess(editSiteUrl) {
  const confirmation = document.getElementById("save-site-confirm");
  const redX = document.getElementById("red-x");
  const greenCheck = document.getElementById("green-check")

  greenCheck.classList.remove("hidden");
  redX.classList.add("hidden");
  confirmation.innerHTML = `Site saved. <a href=${editSiteUrl}>Click to add items to site</a>`
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

