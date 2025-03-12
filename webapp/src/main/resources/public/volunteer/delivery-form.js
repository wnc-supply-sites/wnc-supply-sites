async function fetchSiteData (siteId) {
    const response = await fetch(`/volunteer/site-items?siteId=${siteId}`);
}

function instantiateEventListeners() {

    const siteSelect = document.getElementById("site-select");
    siteSelect.addEventListener("change", (e) => {
    });



}

window.addEventListener("load", () => {
    instantiateEventListeners();
})