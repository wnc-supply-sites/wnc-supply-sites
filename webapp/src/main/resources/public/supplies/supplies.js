function handleSelection(elementSelected) {
  const selectionBox = document.getElementById(elementSelected + "-select");
  const selection = selectionBox.value;

  if (selection === "") {
    return;
  }
  addSelection(elementSelected, selection);
  selectionBox.selectedIndex = 0;
  updateData();
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
      `<div class='box horizontal selection-box' onclick="removeSelection(this)">
                    <div style="margin-right: 5px"><button type="button">X</button></div>
                    <div class="selected-value">${value}</div>\
                </div>`
}

function removeSelection(div) {
  div.parentNode.removeChild(div);
  updateData();
}

function readSelections(selectionElement) {
  const selectionsDiv = document.getElementById(selectionElement + "-selections");
  return [...selectionsDiv.querySelectorAll(".selected-value")].map(v => v.innerHTML);
}

function clearSelections(selectionElement) {
  document.getElementById(selectionElement + "-selections").innerHTML = "";
  document.getElementById(selectionElement + "-select").selectedIndex = 0;
  updateData();
}

async function updateData() {
  try {
    document.getElementById("error-div").innerHTML = "";
    startLoaderAnimation();
    const data = await fetchSupplyData();
    const supplyHubHtml = "<br><span class='supply-hub'>(Supply Warehouse)</span>";
    const notAcceptingDonationsHtml = "<br><span class='not-accepting-donations'>(Not Accepting Donations)</span>";
    // write data to the results table
    document.getElementById('results-table').querySelector("tbody").innerHTML =
        data.results.map(r => `
              <tr>
                  <td>
                    <a href='site-detail?id=${r.id}'><span class="site-name">${r.site}</span></a>
                    ${r.siteType === "Supply Hub" ? supplyHubHtml : ''}
                    ${r.acceptingDonations ? '' : notAcceptingDonationsHtml}
                  </td>
                  <td>
                    ${r.county}
                  </td>
                  <td>${formatItems(r.items)}</td>
                  <td class="date-column">${r.inventoryLastUpdated}</td>
              </tr>`)
        .join("\n");
    document.getElementById('result-count').innerHTML = `${data.resultCount} results`;
    stopLoaderAnimation();
  } catch (error) {
    document.getElementById('result-count').innerHTML = "";
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
  document.getElementById('result-count').innerHTML = "";
  document.getElementById('loader-div').style.animationPlayState = 'running';
  document.getElementById('loader-div').style.display = 'block';
}

function stopLoaderAnimation() {
  document.getElementById('loader-div').style.animationPlayState = 'paused';
  document.getElementById('loader-div').style.display = 'none';
}

async function fetchSupplyData() {
  const url = "/supplies/site-data";
  const sites = readSelections('site');
  const counties = readSelections('county');
  const items = readSelections('item');
  const itemStatus = [...document.getElementById('item-status').querySelectorAll("input:checked")].map(c => c.value);
  const siteType = [...document.getElementById('site-type').querySelectorAll("input:checked")].map(c => c.value);
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
      siteType: siteType,
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
  //   "resultCount": 2,
  //   "results": [
  //     {"site": "site1", "county": "Ashe", "items": [
  //         {"name": "heater", "status": "urgent"}
  //         {"name": "bread", "status": "need"},
  //         {"name": "butter", "status": "available"},
  //         {"name": "water", "status": "oversupply"},
  //       ]},
  //     {"site": "site2", "county": "Watauga", "items": [
  //         {"name": "water", "status": "need"},
  //         {"name": "bread", "status": "available"}
  //       ]}
  //   ]
  // }`);
}

function formatItems(items) {
  return "<ul>\n" +
      items.map(i =>
          `<li><div class="${i.displayClass}">${i.name}</div></li>\n`
      ).join("\n")
      + "\n</ul>";
}
