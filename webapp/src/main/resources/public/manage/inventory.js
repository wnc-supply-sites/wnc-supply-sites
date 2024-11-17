/**
 * Fires when inventory checkbox is checked or unchecked.
 */
function toggleInventory(itemName) {
  const checked = document.getElementById(`${itemName}Checkbox`).checked;

  document.getElementById(`${itemName}Requested`).disabled = !checked;
  document.getElementById(`${itemName}Urgent`).disabled = !checked;
  document.getElementById(`${itemName}Oversupply`).disabled = !checked;

  if (checked) {
    document.getElementById(`${itemName}RequestedLabel`)
    .classList.remove("disabledInventory");
    document.getElementById(`${itemName}UrgentLabel`)
    .classList.remove("disabledInventory");
    document.getElementById(`${itemName}OversupplyLabel`)
    .classList.remove("disabledInventory");
  } else {
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


  const newItemRowHtml = `
          <tr>
          <td><input type="checkbox" id="${itemNameEncoded}Checkbox"
                     onclick="toggleInventory('${itemNameEncoded}')" checked></input></td>
          <td><label for="${itemNameEncoded}Checkbox" class="larger">${itemNameEncoded}</label></td>
          <td>
            <div class="horizontal">
              <fieldset class="vertical">
                <div class="horizontal">
                  <input type="radio" id="${itemNameEncoded}Requested" name="${itemNameEncoded}Status" ${requestedChecked}/>
                  <label for="${itemNameEncoded}Requested" class="requested" id="${itemNameEncoded}RequestedLabel">Requested or
                    Available</label>
                </div>
                <div class="horizontal">
                  <input type="radio" id="${itemNameEncoded}Urgent" name="${itemNameEncoded}Status" ${urgentChecked}/>
                  <label for="${itemNameEncoded}Urgent" class="urgent" id="${itemNameEncoded}UrgentLabel">Urgently Needed</label>
                </div>
                <div class="horizontal">
                  <input type="radio" id="${itemNameEncoded}Oversupply" name="${itemNameEncoded}Status" ${oversupplyChecked}/>
                  <label for="${itemNameEncoded}Oversupply" class="oversupply" id="${itemNameEncoded}OversupplyLabel">Oversupply (too
                    much)</label>
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

}


function htmlEncode(input) {
  input = input.replace(/&/g, '&amp;');
  input = input.replace(/</g, '&lt;');
  input = input.replace(/>/g, '&gt;');
  return input;
}

