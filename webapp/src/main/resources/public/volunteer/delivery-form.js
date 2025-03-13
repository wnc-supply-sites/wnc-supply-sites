

async function fetchSiteData (siteId) {
    const response = await fetch(`/volunteer/site-items?siteId=${siteId}`);
    console.log(await response.json());
}

function instantiateEventListeners() {
    const siteSelect = document.getElementById("site-select");
    siteSelect.addEventListener("change", (e) => {
        console.log("Site changed :", e.target.value);
        fetchSiteData(e.target.value);
    });

}


window.addEventListener("load", () => {
    instantiateEventListeners();
})