

async function fetchSiteData (siteId) {
    const response = await fetch(`/volunteer/site-items?siteId=${siteId}`);
    console.log(response);
}

function instantiateEventListeners() {

    const siteSelect = document.getElementById("site-select");
    siteSelect.addEventListener("change", (e) => {
        console.log("Site changed :", e.target.value);
        fetchSiteData(e.target.value);
    });




}


window.addEventListener("load", () => {
    console.log("Site loaded")
    instantiateEventListeners();
})