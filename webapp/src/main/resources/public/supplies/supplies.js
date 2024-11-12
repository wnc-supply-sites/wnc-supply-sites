function handleSelection(elementSelected) {
  const selectionBox = document.getElementById(elementSelected + "-select");
  const selection = selectionBox.value;

  if (selection === "") {
    return;
  }
  addSelection(elementSelected, selection);
}

function addSelection(elementSelected, value) {
  if (value === "") {
    return;
  }
  const currentSelections = readSelections(elementSelected);
  if (currentSelections.includes(value)) {
    return;
  }
  const selectionDiv = document.getElementById(elementSelected + "-selections");
  selectionDiv.innerHTML +=
      `<div class='box horizontal' onclick="removeDiv(this)">
                    <div style="margin-right: 5px"><button type="button">X</button></div>
                    <div class="selected-value">${value}</div>\
                </div>`
}

function removeDiv(div) {
  div.parentNode.removeChild(div);
}

function readSelections(selectionElement) {
  const selectionsDiv = document.getElementById(selectionElement + "-selections");
  return [...selectionsDiv.querySelectorAll(".selected-value")].map(v => v.innerHTML);
}

function selectAll(selectionElement) {
  const selectElement = document.querySelector('[id=' + selectionElement + '-select]');
  [...selectElement.options].map(o => o.value).forEach(v => addSelection(selectionElement, v));
  document.getElementById(selectionElement + "-select").selectedIndex = 0;
}

function clearSelections(selectionElement) {
  document.getElementById(selectionElement + "-selections").innerHTML = "";
  document.getElementById(selectionElement + "-select").selectedIndex = 0;
}

async function updateData() {
  try {
    document.getElementById("error-div").innerHTML = "";
    startLoaderAnimation();
    const data = await fetchSupplyData();
    const notAcceptingDonationsHtml = "<br><div class='not-accepting-donations'>(Not Accepting Donations)</div>";
    // write data to the results table
    document.getElementById('results-table').querySelector("tbody").innerHTML =
        data.results.map(r => `
              <tr>
                  <td>${r.site}${r.acceptingDonations ? '' : notAcceptingDonationsHtml}</td>
                  <td>${r.county}</td>
                  <td>${formatItems(r.items)}</td>
              </tr>`)
        .join("\n");
    document.getElementById('result-count').innerHTML = `${data.resultCount} results`;
    stopLoaderAnimation();
  } catch (error) {
    stopLoaderAnimation();
    showError(error);
  }
}

function showError(error) {
  console.error(error, error.stack);
  document.getElementById("error-div").innerHTML =
      `Error fetching data. Server is not available. Error: ${error.message}`;
}

function startLoaderAnimation() {
  document.getElementById('popup-div').style.animationPlayState = 'running';
  document.getElementById('popup-div').style.display = 'block';
  document.getElementById('update-button').innerText = '';
}

function stopLoaderAnimation() {
  document.getElementById('popup-div').style.animationPlayState = 'paused';
  document.getElementById('popup-div').style.display = 'none';
  document.getElementById('update-button').innerText = 'Update';

}

async function fetchSupplyData() {
  const url = "supplies";
  const sites = readSelections('site');
  const counties = readSelections('county');
  const items = readSelections('item');
  const itemStatus = [...document.getElementById('item-status').querySelectorAll("input:checked")].map(c => c.value);
  const acceptingDonations = document.getElementById('accepting-donations').checked;
  const notAcceptingDonations = document.getElementById('not-accepting-donations').checked;

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      sites: sites,
      items: items,
      counties: counties,
      itemStatus: itemStatus,
      acceptingDonations: acceptingDonations,
      notAcceptingDonations: notAcceptingDonations
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response}`);
  }
  return await response.json();

  //    Example response JSON
  // var data = JSON.parse(`{
  //   "resultCount": 30,
  //   "results": [
  //     {"site": "site1", "county": "Ashe", "items": [
  //         {"name": "water", "status": "oversupply"},
  //         {"name": "bread", "status": "requested"},
  //         {"name": "heater", "status": "urgent"}
  //       ]},
  //     {"site": "site2", "county": "Watauga", "items": [
  //         {"name": "water", "status": "requested"},
  //         {"name": "bread", "status": "requested"}
  //       ]}
  //   ]
  // }`);
}

function formatItems(items) {
  return "<ul>\n" +
      items.map(i =>
          `<li><div class="${i.status}">${i.name}</div></li>\n`
      ).join("\n")
      + "\n</ul>";
}
