function selectSite() {
  const selectedSite = document.getElementById("site").value;

  location.href = `?siteWssId=${selectedSite}&page=${currentPage}`
}
