/** Fires when a user clicks the table cell containing an
 * inventory checkbox. This is a convenience to make the select
 * area very large, easy to click, without having to actually
 * click the checkbox or label precisely.
 */

async function checkAndToggleInventory(siteId, itemName) {
  // first toggle the checkbox
  const checkbox = document.getElementById(`${itemName}Checkbox`);
  checkbox.checked = !checkbox.checked;

  // now handle the toggle inventory event as normal
  await toggleInventory(siteId, itemName);
}

/**
 * Fires when inventory checkbox is checked or unchecked.
 */
async function toggleInventory(siteId, itemName) {
  const checked = document.getElementById(`${itemName}Checkbox`).checked;

  const requestedChecked = document.getElementById(`${itemName}Requested`)
      .checked;
  const urgentChecked = document.getElementById(`${itemName}Urgent`)
      .checked;
  const oversupplyChecked = document.getElementById(`${itemName}Oversupply`)
      .checked;

  if (checked) {
    try {
      let labelClass = "requested";
      let itemStatus = "Requested";
      if (urgentChecked) {
        labelClass = "urgent";
        itemStatus = "Urgent Need";
      } else if (oversupplyChecked) {
        labelClass = "oversupply";
        itemStatus = "Oversupply";
      }
      await sendActivateItem(siteId, itemName, itemStatus);

      document.getElementById(`${itemName}Label`).classList.value = "larger " + labelClass;

      document.getElementById(`${itemName}RequestedLabel`)
      .classList.remove("disabled");
      document.getElementById(`${itemName}UrgentLabel`)
      .classList.remove("disabled");
      document.getElementById(`${itemName}OversupplyLabel`)
      .classList.remove("disabled");
      showUpdateConfirmation(itemName);

      document.getElementById(`${itemName}Requested`).disabled = !checked;
      document.getElementById(`${itemName}Urgent`).disabled = !checked;
      document.getElementById(`${itemName}Oversupply`).disabled = !checked;
    } catch (error) {
      showError(error);
      // revert checkbox update
      document.getElementById(`${itemName}Checkbox`).checked = false;
    }
  } else {
    try {
      await sendDeactivateItem(siteId, itemName);

      document.getElementById(`${itemName}Label`).classList.value = "larger disabled";

      document.getElementById(`${itemName}RequestedLabel`)
      .classList.add("disabled");
      document.getElementById(`${itemName}UrgentLabel`)
      .classList.add("disabled");
      document.getElementById(`${itemName}OversupplyLabel`)
      .classList.add("disabled");
      showUpdateConfirmation(itemName);

      document.getElementById(`${itemName}Requested`).disabled = !checked;
      document.getElementById(`${itemName}Urgent`).disabled = !checked;
      document.getElementById(`${itemName}Oversupply`).disabled = !checked;
    } catch (error) {
      showError(error);
      // revert checkbox update
      document.getElementById(`${itemName}Checkbox`).checked = true;
    }
  }
}

async function sendActivateItem(siteId, itemName, itemStatus) {
  const url = "/manage/activate-site-item";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      itemName: itemName,
      itemStatus: itemStatus
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response.body}`);
  }
  return await response.text();
}

async function sendDeactivateItem(siteId, itemName) {
  const url = "/manage/deactivate-site-item";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      itemName: itemName
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response.body}`);
  }
  return await response.text();
}


/**
 * Fires when the item status radio buttons are toggled.
 */
async function changeItemStatus(siteId, itemName) {
  const requestedChecked = document.getElementById(`${itemName}Requested`)
      .checked ? "checked" : "";
  const urgentChecked = document.getElementById(`${itemName}Urgent`)
      .checked ? "checked" : "";
  const oversupplyChecked = document.getElementById(`${itemName}Oversupply`)
      .checked ? "checked" : "";

  document.getElementById(`${itemName}Label`)
  .classList.remove("requested", "urgent", "oversupply");

  let newStatus = "";
  if (urgentChecked) {
    newStatus = "Urgent Need";
    document.getElementById(`${itemName}Label`)
    .classList.add("urgent");
  } else if (oversupplyChecked) {
    newStatus = "Oversupply";
    document.getElementById(`${itemName}Label`)
    .classList.add("oversupply");
  } else {
    newStatus = "Requested";
    document.getElementById(`${itemName}Label`)
    .classList.add("requested");
  }

  try {
    await sendItemStatusChange(siteId, itemName, newStatus);
    showUpdateConfirmation(itemName);
  } catch (error) {
    showError(error);
  }
}

async function sendItemStatusChange(siteId, itemName, newStatus) {
  const url = "/manage/update-site-item-status";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      itemName: itemName,
      newStatus: newStatus
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response.body}`);
  }
  return await response.text();
}

async function addItem(siteId) {
  const itemName = document.getElementById("newItemText").value;
  const itemNameEncoded = htmlEncode(itemName);

  if (itemName === "") {
    return;
  }

  const requestedChecked = document.getElementById("requestedRadioNew")
      .checked ? "checked" : "";
  const urgentChecked = document.getElementById("urgentlyNeededRadioNew")
      .checked ? "checked" : "";
  const oversupplyChecked = document.getElementById("oversupplyRadioNew")
      .checked ? "checked" : "";

  let labelStyle = "";
  let status = "Requested";
  if (requestedChecked) {
    labelStyle = "requested";
    status = "Requested";
  } else if (urgentChecked) {
    labelStyle = "urgent";
    status = "Urgent Need";
  } else if (oversupplyChecked) {
    labelStyle = "oversupply";
    status = "Oversupply";
  }

  let result = await sendAddNewItem(siteId, itemName, status);
  if (!result) {
    return;
  }

  const newItemRowHtml = `
        <tr>
          <!-- Item Checkbox -->
          <td>
            <input type="checkbox" id="${itemNameEncoded}Checkbox"
                   onclick="toggleInventory('${siteId}', '${itemNameEncoded}')" checked />
          </td>

           <!-- Item Label -->
          <td>
            <div>
              <label for="${itemNameEncoded}Checkbox" class="larger ${labelStyle}" id="${itemNameEncoded}Label">${itemNameEncoded}</label>
            </div>
            <div class="horizontal update-confirm-div" id="${itemNameEncoded}UpdateConfirm">
              <span class="green-check" id="update-confirm">&#10003;</span>
              <span>Updated</span>
            </div>
          </td>

          <!-- Item Status Radio buttons -->
          <td>
            <div class="horizontal">
            
              <fieldset class="vertical">
                <div class="horizontal">
                  <input 
                      type="radio" 
                      id="${itemNameEncoded}Requested" 
                      name="${itemNameEncoded}Status" 
                      onclick="changeItemStatus(siteId, '${itemNameEncoded}')"
                      ${requestedChecked}/>
                  <label 
                      for="${itemNameEncoded}Requested" 
                      class="requested" 
                      id="${itemNameEncoded}RequestedLabel">
                    Requested or Available
                  </label>
                </div>
                <div class="horizontal">
                  <input 
                      type="radio" 
                      id="${itemNameEncoded}Urgent" 
                      name="${itemNameEncoded}Status" 
                      onclick="changeItemStatus(siteId, '${itemNameEncoded}')"
                      ${urgentChecked}/>
                  <label 
                      for="${itemNameEncoded}Urgent" 
                      class="urgent" 
                      id="${itemNameEncoded}UrgentLabel">
                    Urgently Needed
                  </label>
                </div>
                <div class="horizontal">
                  <input 
                      type="radio" 
                      id="${itemNameEncoded}Oversupply" 
                      name="${itemNameEncoded}Status" 
                      onclick="changeItemStatus(siteId, '${itemNameEncoded}')"
                      ${oversupplyChecked}/>
                  <label 
                      for="${itemNameEncoded}Oversupply" 
                      class="oversupply" 
                      id="${itemNameEncoded}OversupplyLabel">
                    Oversupply (too much)
                  </label>
                </div>
              </fieldset>
            </div>
          </td>
        </tr>
  `;

  document.getElementById("inventoryTableBody").innerHTML +=
      newItemRowHtml;

  document.getElementById("newItemText").value = "";

  showUpdateConfirmation(itemNameEncoded);
}

async function sendAddNewItem(siteId, itemName, itemStatus) {
  const url = "/manage/add-site-item";

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        siteId: siteId,
        itemName: itemName,
        itemStatus: itemStatus
      })
    });

    if (!response.ok) {
      if (response.status === 400) {
        document.getElementById("addItemResult").innerHTML = "Item not added, already exists."
        document.getElementById("addItemResult").style.display = 'block';
        return false;
      } else {
        showError(new Error(`Response status: ${response.status}, ${response.text}`));
        return false;
      }
    }
    document.getElementById("addItemResult").style.display = 'none';
    return true;
  } catch (error) {
    showError(error);
    return false;
  }
}

const timeouts = [];


/**
 * Shows 'update confirm' checkmark. If the indicator is already visible, then we hide it briefly
 * before showing it again. If there was a previous timer that was going to hide it, we cancel
 * that so we can renew the hide timer.
 */
function showUpdateConfirmation(itemName) {

  // remove any preceding timeouts which have yet to fire.
  clearTimeout(timeouts[itemName]);

  // if update confirmation is visible, hide it very briefly and then show it again.
  if (document.getElementById(`${itemName}UpdateConfirm`).style.display === "block") {
    // hide the element for 200ms
    document.getElementById(`${itemName}UpdateConfirm`).style.display = "none";
    timeouts[itemName] = setTimeout(function () {
          document.getElementById(`${itemName}UpdateConfirm`).style.display = 'block';
        },
        100);

  } else {
    // if update confirm is not visible, then make it visible.
    document.getElementById(`${itemName}UpdateConfirm`).style.display = "block";
  }
  // document.getElementById(`${itemName}UpdateConfirm`).style.display = "block";
  timeouts[itemName] = setTimeout(function () {
        document.getElementById(`${itemName}UpdateConfirm`).style.display = 'none';
      },
      1000);
}

function showError(error) {
  let errorDiv = document.getElementById("error-div");
  errorDiv.innerHTML = "Update failed. Error contacting server. " + error;
  errorDiv.style.display = 'block';

  console.log("Error: " + error);
  clearTimeout(timeouts['error']);
  timeouts['error'] = setTimeout(function () {
        errorDiv.style.display = 'none';
      },
      3000);

}

function htmlEncode(input) {
  input = input.replace(/&/g, '&amp;');
  input = input.replace(/</g, '&lt;');
  input = input.replace(/>/g, '&gt;');
  return input;
}
