function selectSite() {
  const siteSelect = document.getElementById("site-select");
  const siteId = siteSelect.value;
  const siteName = siteSelect.options[siteSelect.selectedIndex].text;

  if(siteId === '') {
    return;
  }

  document.getElementById("site-select-div").style.display = 'none';
  document.getElementById("site-manage-select-div").style.display = 'block';
  document.getElementById("site-name-header").innerText = siteName;

  document.getElementById("manage-site-inventory-href").href =
      `manage-inventory.html?siteId=${siteId}&siteName=${encodeURIComponent(siteName)}`;
  document.getElementById("manage-site-contact-href").href =
      `manage-inventory.html?siteId=${siteId}&siteName=${encodeURIComponent(siteName)}`;
  document.getElementById("manage-site-status-href").href =
      `manage-inventory.html?siteId=${siteId}&siteName=${encodeURIComponent(siteName)}`;

}



function reselectSite() {
  document.getElementById("site-select").selectedIndex = 0;
  document.getElementById("site-select-div").style.display = 'block';
  document.getElementById("site-manage-select-div").style.display = 'none';

}


