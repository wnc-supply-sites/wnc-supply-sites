/**
 * Fires when inventory checkbox is checked or unchecked.
 */
function toggleInventory(itemName) {
  const checked = document.getElementById(`${itemName}Checkbox`).checked;

  document.getElementById(`${itemName}Requested`).disabled = !checked;
  document.getElementById(`${itemName}Urgent`).disabled = !checked;
  document.getElementById(`${itemName}Oversupply`).disabled = !checked;

  const requestedChecked = document.getElementById(`${itemName}Requested`)
      .checked;
  const urgentChecked = document.getElementById(`${itemName}Urgent`)
      .checked;
  const oversupplyChecked = document.getElementById(`${itemName}Oversupply`)
      .checked;

  if (checked) {
    let labelClass;
    if(requestedChecked) {
      labelClass = "requested";
    } else if(urgentChecked) {
      labelClass = "urgent";
    } else {
      labelClass = "oversupply";
    }

    document.getElementById(`${itemName}Label`).classList.value = "larger " + labelClass;
    // .classList.add("larger");
    // document.getElementById(`${itemName}Label`)
    // .classList.add(labelClass);

    document.getElementById(`${itemName}RequestedLabel`)
    .classList.remove("disabledInventory");
    document.getElementById(`${itemName}UrgentLabel`)
    .classList.remove("disabledInventory");
    document.getElementById(`${itemName}OversupplyLabel`)
    .classList.remove("disabledInventory");
  } else {
    document.getElementById(`${itemName}Label`).classList.value = "larger disabledInventory";

    // document.getElementById(`${itemName}Label`)
    // .classList.add("larger");
    // document.getElementById(`${itemName}Label`)
    // .classList.add("disabledInventory");

    document.getElementById(`${itemName}RequestedLabel`)
    .classList.add("disabledInventory");
    document.getElementById(`${itemName}UrgentLabel`)
    .classList.add("disabledInventory");
    document.getElementById(`${itemName}OversupplyLabel`)
    .classList.add("disabledInventory");
  }
}

function changeItemStatus(itemName) {
  const requestedChecked = document.getElementById(`${itemName}Requested`)
      .checked ? "checked" : "";
  const urgentChecked = document.getElementById(`${itemName}Urgent`)
      .checked ? "checked" : "";
  const oversupplyChecked = document.getElementById(`${itemName}Oversupply`)
      .checked ? "checked" : "";

  document.getElementById(`${itemName}Label`)
  .classList.remove("requested", "urgent", "oversupply");

  if (requestedChecked) {
    document.getElementById(`${itemName}Label`)
    .classList.add("requested");
  } else if (urgentChecked) {
    document.getElementById(`${itemName}Label`)
    .classList.add("urgent");
  } else {
    document.getElementById(`${itemName}Label`)
    .classList.add("oversupply");
  }


}

function addItem() {
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
  if (requestedChecked) {
    labelStyle = "requested";
  } else if (urgentChecked) {
    labelStyle = "urgent";
  } else {
    labelStyle = "oversupply";
  }

  const newItemRowHtml = `
        <tr>
          <!-- Item Checkbox -->
          <td><input type="checkbox" id="${itemNameEncoded}Checkbox"
                     onclick="toggleInventory('${itemNameEncoded}')" checked /></td>
                     
           <!-- Item Label -->
          <td><label for="${itemNameEncoded}Checkbox" class="larger ${labelStyle}" id="${itemNameEncoded}Label">${itemNameEncoded}</label></td>
          <!-- Item Status Radio buttons -->
          <td>
            <div class="horizontal">
            
              <fieldset class="vertical">
                <div class="horizontal">
                  <input 
                      type="radio" 
                      id="${itemNameEncoded}Requested" 
                      name="${itemNameEncoded}Status" 
                      onclick="changeItemStatus('${itemNameEncoded}')"
                      ${requestedChecked}/>
                  <label 
                      for="${itemNameEncoded}Requested" 
                      class="requested" 
                      id="${itemNameEncoded}RequestedLabel">
                    Requested or Available</label>
                </div>
                <div class="horizontal">
                  <input 
                      type="radio" 
                      id="${itemNameEncoded}Urgent" 
                      name="${itemNameEncoded}Status" 
                      onclick="changeItemStatus('${itemNameEncoded}')"
                      ${urgentChecked}/>
                  <label 
                      for="${itemNameEncoded}Urgent" 
                      class="urgent" 
                      id="${itemNameEncoded}UrgentLabel">
                    Urgently Needed</label>
                </div>
                <div class="horizontal">
                  <input 
                      type="radio" 
                      id="${itemNameEncoded}Oversupply" 
                      name="${itemNameEncoded}Status" 
                      onclick="changeItemStatus('${itemNameEncoded}')"
                      ${oversupplyChecked}/>
                  <label 
                      for="${itemNameEncoded}Oversupply" 
                      class="oversupply" 
                      id="${itemNameEncoded}OversupplyLabel">
                    Oversupply (too much)</label>
                </div>
              </fieldset>
              <div class="horizontal update-confirm-div" id="${itemNameEncoded}UpdateConfirm">
                <div class="green-check" id="update-confirm">&#10003;</div>
                <span>Updated</span>
              </div>
            </div>

          </td>
        </tr>
  `;

  document.getElementById("inventoryTableBody").innerHTML +=
      newItemRowHtml;

  document.getElementById("newItemText").value = "";

  document.getElementById(`${itemNameEncoded}UpdateConfirm`).style.display = "block";
  setTimeout(function () {
        document.getElementById(`${itemNameEncoded}UpdateConfirm`).style.display = 'none';
      },
      1500);
}


function htmlEncode(input) {
  input = input.replace(/&/g, '&amp;');
  input = input.replace(/</g, '&lt;');
  input = input.replace(/>/g, '&gt;');
  return input;
}

