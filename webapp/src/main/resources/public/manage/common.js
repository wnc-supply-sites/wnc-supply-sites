async function changeCounty() {
  const state = document.getElementById('State').value;
  const counties = countyMapByState[state];
  document.getElementById('County')
      .innerHTML =
      counties.map(c =>
          `<option value="${c}">${c}</option>`
      ).sort()
}

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


  if (newValue.trim() === "" &&
      ((field === "Contact Number")
          || (field === "Site Name")
          || (field === "Street Address")
          || (field === "City"))
  ) {
    document.getElementById(field).classList.add("missingData");
    document.getElementById(field + "-update-confirm").innerHTML =
        "Field is required"
    return false;
  } else {
    document.getElementById(field).classList.remove("missingData");
  }

  try {
    await sendSiteUpdate(siteId, field, newValue)
    showConfirmations(field, newValue);
  } catch (error) {
    showSiteUpdateError(field, error);
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

function showSiteUpdateError(field, error) {
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
