
async function handleVerificationSubmission(e) {
    const phoneNumber = document.getElementById("phone-verification").value.replaceAll("-", "");
    const urlKey = document.getElementById("delivery-details").dataset.urlkey;
    submitVerification(phoneNumber, urlKey);
};

async function submitVerification (phoneNumber, urlKey) {

    const response = await fetch("/volunteer/verify-delivery", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                "phoneNumber": phoneNumber,
                "urlKey": urlKey.toUpperCase(),
                "section": "delivery"
                })
    });

    if (response.ok) {
        const data = await response.json();
        return handleLoadDeliveryData(data);
    } else {
        return displayVerificationError();
    }
}


// fills and displays delivery data
function handleLoadDeliveryData(data) {
    removeVerificationError();


    // Load request status

    // Load site Name
    if (data.request.siteName) loadSiteDetail("site-name", data.request.siteName);

    // Load site Address and map
    if (data.request.address) {
        const siteAddress = `${data.request.address}, ${data.request.city}`
        loadSiteDetail("site-address", siteAddress);
        loadMap(siteAddress);
    };

    // Load Volunteer Name
    if (data.request.volunteerName) loadSiteDetail("volunteer-name", data.request.volunteerName);

    // Load items
    if (data.request.items) {
        const itemsList = document.getElementById("delivery-items");
        for (let item of data.request.items) {
            const deliveryItem = createDeliveryItemElement(item.name);
            itemsList.appendChild(deliveryItem);
        };
    };

    // check status and load contacts


    // Load buttons

    const deliveryDetails = document.getElementById("delivery-details");
    deliveryDetails.classList.remove("hidden");

    const verificationContainer = document.getElementById("verification-container");
    verificationContainer.classList.add("hidden");
};

// Creates a new li element with text content of itemNAme
function createDeliveryItemElement(itemName) {
    const deliveryItem = document.createElement("li");
    deliveryItem.textContent = itemName;
    return deliveryItem;
}

function loadMap(address) {
    const mapElement = document.createElement("div");
    mapElement.innerHTML = `
    <div class="gmap_canvas">
        <iframe
            class="gmap_iframe"
            frameborder="0"
            scrolling="no"
            marginheight="0"
            marginwidth="0"
            src="https://maps.google.com/maps?width=600&amp;height=500&amp;hl=en&amp;q=${address}&amp;t=&amp;z=10&amp;ie=UTF8&amp;iwloc=B&amp;output=embed"
        >
        </iframe>
    </div>`.trim();

    const mapContainer = document.getElementById("map");
    mapContainer.appendChild(mapElement.firstChild);
}


// Finds the element by the provided elementId.
// Changes the textContent and then removes hidden element
function loadSiteDetail(elementId, value) {
    const detailElement = document.getElementById(elementId);
    detailElement.textContent = value;
    detailElement.parentElement.classList.remove("hidden");
}

function displayVerificationError() {
    console.log("displaying verification error");
    const errorMsgElement = document.getElementById("verification-error-msg");
    errorMsgElement.classList.remove("hidden");
}

function removeVerificationError() {
    const errorMsgElement = document.getElementById("verification-error-msg");
    errorMsgElement.classList.add("hidden");
}

window.addEventListener("load", () => {
    const verificationForm = document.getElementById("verification-form");

    // Event Listener for Phone number verification
    verificationForm.addEventListener("submit", (e) => {
        e.preventDefault();
        handleVerificationSubmission(e);
    });
})