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
