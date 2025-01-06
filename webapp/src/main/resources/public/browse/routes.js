function toggleFiltersVisibility() {
  const filtersDiv = document.getElementById("filters");
  let isVisible = (filtersDiv.style.display !== "none");

  if(isVisible) {
    filtersDiv.style.display = "none";
    document.getElementById("filterDisplayToggle").innerHTML = "[show]";
  } else {
    filtersDiv.style.display = "block";
    document.getElementById("filterDisplayToggle").innerHTML = "[hide]";
  }
}


function selectSite() {
  const selectedSite = document.getElementById("site").value;

  location.href = `?siteWssId=${selectedSite}&page=${currentPage}`
}
