function selectSiteFilter() {
  const selectedSite = document.getElementById("site").value;
  selectSite(selectedSite);
}

function selectSite(wssId) {
  location.href = `?siteWssId=${wssId}&page=1&county=${currentCounty}`
}

function selectCountyFilter() {
  const selectedCounty = document.getElementById("county").value;
  selectCounty(selectedCounty);
}

function selectCounty(selection) {
  location.href = `?siteWssId=${currentSite}&page=1&county=${selection}`
}

function clearFilters() {
  document.getElementById("site").value = '';
  document.getElementById("county").value = '';
  location.href = `?page=1`
}

function volunteerClicked() {
  const volunteerButtonRow = event.target.parentNode;
  volunteerButtonRow.classList.add("hidden");

  const confirmVolunteer = event.target.parentNode.parentNode.querySelector(".confirm-volunteer")
  confirmVolunteer.classList.remove("hidden");
}

function cancelVolunteerClicked() {
    const volunteerButtonRow = event.target.parentNode.parentNode.parentNode.querySelector(".volunteer-button-row");
    volunteerButtonRow.classList.remove("hidden");

    const confirmVolunteer = event.target.parentNode.parentNode.parentNode.querySelector(".confirm-volunteer")
    confirmVolunteer.classList.add("hidden");
}

function volunteerConfirmClicked(fromSiteWssId, fromSiteName, toSiteWssId, toSiteName,itemList) {
  const confirmVolunteer = event.target.parentNode.parentNode;

  const confirmMessage = confirmVolunteer.parentNode.querySelector("div[class=volunteer-confirm-message]")

  const errorDiv = confirmVolunteer.parentNode.querySelector("div[class=errorMessage]")

  const fromDate = confirmVolunteer.querySelector("select[class=fromDates]").value;
  const toDate = confirmVolunteer.querySelector("select[class=toDates]").value;


  fetch("/browse/routes/volunteer", {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      fromSiteWssId: fromSiteWssId,
      fromSiteName: fromSiteName,
      toSiteWssId: toSiteWssId,
      toSiteName: toSiteName,
      itemList: JSON.parse(itemList),
      fromDate: fromDate,
      toDate: toDate
    })
  })
  .then(
      async function (response) {
        if (response.ok) {
          errorDiv.innerHTML = "";
          confirmVolunteer.style.display = "none";
          confirmMessage.style.display = "block";
        } else {
          const responseJson = await response.json();
          errorDiv.innerHTML = "Error: " + responseJson.error;
        }
      },
      function (error) {
        errorDiv.innerHTML = "Error, server not available: " + error;
      });
}
