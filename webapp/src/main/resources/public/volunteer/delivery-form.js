

function updateSiteAddress (address, county, state) {
    const siteAddress = document.getElementById("selected-site-address");
    siteAddress.textContent = `Site Address: ${address}, ${county}, ${state}`;
}

function updateNeededItemsList (items) {
    const neededItemsList = document.getElementById("needed-items");

    for (const child of neededItemsList.children) {
        console.log(child)
        child.remove();
    }
    for(const item of items) {
        let itemContainer = document.createElement("div");

        let listItem = document.createElement("input");
        listItem.type = "checkbox"
        listItem.name = "neededItem"
        listItem.value = item.id;
        listItem.id = item.id;

        let itemLabel = document.createElement("label");
        itemLabel.for = item.id;
        itemLabel.textContent = item.name;



        itemContainer.appendChild(listItem);
        itemContainer.appendChild(itemLabel);
        neededItemsList.appendChild(itemContainer);
    }
}

async function updateSelectedSiteData (siteId) {
    console.log("refreshing site data")
    const siteData = await fetchSiteData(siteId);
    console.log("Site Data retrieved: ", siteData);
    updateSiteAddress(siteData.site.address, siteData.site.county, siteData.site.state)
    updateNeededItemsList(siteData.site.items)
}

async function fetchSiteData (siteId) {
    const response = await fetch(`/volunteer/site-items?siteId=${siteId}`);
    return await response.json()
}

function instantiateEventListeners() {
    const siteSelect = document.getElementById("site-select");
    siteSelect.addEventListener("change", (e) => {
        console.log("Site changed :", e.target.value);
        updateSelectedSiteData(e.target.value)
    });
}

window.addEventListener("load", () => {
    instantiateEventListeners();
    const siteSelect = document.getElementById("site-select");
    updateSelectedSiteData(siteSelect.value);
})