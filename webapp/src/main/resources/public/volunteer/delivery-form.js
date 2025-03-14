

/** When A new site is selected
*    - Change address
*    - Update Items list
*    - Remove selected items from selected items list
*/
function updateSiteAddress (address, county, state) {
    const siteAddress = document.getElementById("selected-site-address");
    siteAddress.textContent = `Site Address: ${address}, ${county}, ${state}`;
}

function updateNeededItemsList (items) {
    const neededItemsList = document.getElementById("needed-items");

    while (neededItemsList.firstChild) neededItemsList.removeChild(neededItemsList.firstChild);


    for(const item of items) {
        let itemContainer = document.createElement("div");

        let listItem = document.createElement("input");
        listItem.type = "checkbox"
        listItem.name = "neededItem"
        listItem.value = item.id;
        listItem.id = item.id;
        listItem.dataset.name = item.name;

        let itemLabel = document.createElement("label");
        itemLabel.for = item.id;
        itemLabel.textContent = item.name;

        itemContainer.appendChild(listItem);
        itemContainer.appendChild(itemLabel);
        neededItemsList.appendChild(itemContainer);
    }
}

async function updateSelectedSiteData (siteId) {
    const siteData = await fetchSiteData(siteId);
    updateSiteAddress(siteData.site.address, siteData.site.county, siteData.site.state);
    updateNeededItemsList(siteData.site.items);
    clearSelectedItems();
}

function addSelectedItem(name, id) {
    const itemsContainer = document.getElementById("selected-items");

    // Get the existing selected items from the DOM
    const selectedItems = Array.from(itemsContainer.getElementsByClassName("selected-item"));

    // Create a new element and add it to the DOM before sorting
    const selectedItem = document.createElement("div");
    selectedItem.classList.add("selected-item");
    selectedItem.id = `selected-${id}`;
    selectedItem.dataset.itemName = name;

    const removeSelectionButton = document.createElement("button");
    removeSelectionButton.type = "button";
    removeSelectionButton.dataset.itemId = id;
    removeSelectionButton.textContent = `X`;
    removeSelectionButton.addEventListener("click", (event) => {
        event.preventDefault();
        const item = document.getElementById(id);
        if (item.checked) item.checked = false;
        removeSelectedItem(id);
    })

    const itemName = document.createElement("span");
    itemName.textContent = name;

    selectedItem.appendChild(removeSelectionButton);
    selectedItem.appendChild(itemName);
    itemsContainer.appendChild(selectedItem);

    // Re-fetch the updated list (including the new item)
    const updatedItems = Array.from(itemsContainer.getElementsByClassName("selected-item"));

    // Sort elements alphabetically by text content
    updatedItems.sort((a, b) => a.dataset.itemName.localeCompare(b.dataset.itemName));

    // Re-append sorted elements to reorder them
    updatedItems.forEach(item => itemsContainer.appendChild(item));
}

function handleItemSelect(element) {
    if (element.checked) {
        addSelectedItem(element.dataset.name, element.value);
    } else {
        removeSelectedItem(element.value);
    };
};

function removeSelectedItem(id) {
    const selectedItem = document.getElementById(`selected-${id}`);
    selectedItem.remove();
};

function clearSelectedItems() {
    const selectedItems = document.getElementById(`selected-items`);
    while (selectedItems.firstChild) selectedItems.removeChild(selectedItems.firstChild)
}

/** API requests */
async function fetchSiteData (siteId) {
    const response = await fetch(`/volunteer/site-items?siteId=${siteId}`);
    return await response.json()
}


/** On Load */
function instantiateEventListeners() {
    const siteSelect = document.getElementById("site-select");
    siteSelect.addEventListener("change", (e) => {
        updateSelectedSiteData(e.target.value)
    });

    const neededItemsList = document.getElementById("needed-items");
    neededItemsList.addEventListener("change", (e) => {
        handleItemSelect(e.target);
    });
}

window.addEventListener("load", () => {
    instantiateEventListeners();
    const siteSelect = document.getElementById("site-select");
    updateSelectedSiteData(siteSelect.value);
})