
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
        return loadDeliveryData(data);
    } else {
        return displayVerificationError();
    }
}


// fills and displays delivery data
function loadDeliveryData(data) {
    debugger
    removeVerificationError();

    // Load request status
    if(data.request.status) loadStatus(data.request.status);

    // Load site Name
    if (data.request.siteName) loadSiteDetail("site-name", data.request.siteName);

    // Load site Address and map
    if (data.request.address) {
        const siteAddress = `${data.request.address}, ${data.request.city}`;
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

    // Load contacts
    if (data.request.volunteerPhone) loadSiteDetail("volunteer-contact", data.request.volunteerPhone)
    if (data.request.siteContactName) loadSiteDetail("site-contact-name", data.request.siteContactName);
    if (data.request.siteContactNumber) loadSiteDetail("site-contact-number", data.request.siteContactNumber);

    // Load status change buttons
    loadStatusChangeButtons(data.request.status, data.userPhoneNumber, data.access);

    const deliveryDetails = document.getElementById("delivery-details");
    deliveryDetails.classList.remove("hidden");

    const verificationContainer = document.getElementById("verification-container");
    verificationContainer.classList.add("hidden");
};

// Creates a new <li> element with item name as it's textContent
function createDeliveryItemElement(itemName) {
    const deliveryItem = document.createElement("li");
    deliveryItem.textContent = itemName;
    return deliveryItem;
}

// Creates a new iframe element using
// google maps as the source and the site address as query
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

// unselect all statuses except for the current status
function loadStatus(status){

    const statusElements = document.getElementsByClassName("status");
    const statusText = `status-${status.toLowerCase()}`;

    for (const element of statusElements) {
        const statusIcon = element.querySelector(".request-status-icon");
        if (element.id === statusText) {
            statusIcon.classList.add("status-selected");
            statusIcon.classList.remove("status-unselected");
        } else {
            statusIcon.classList.remove("status-selected");
            statusIcon.classList.add("status-unselected");
        }
    }
}

// Reads the status and displays the appropriate buttons or an inactive message
function loadStatusChangeButtons(status, userPhoneNumber, access) {
    switch(status){
        case "PENDING":
            loadPendingStatusButtons(access, userPhoneNumber);
            break;
        case "ACCEPTED":
             loadCancelButton(userPhoneNumber);
             break;
        default:
            loadDeclinedOrCancelledMessage();
    }
}

// Displays corresponding buttons based on user access
// Also set up the event listeners for the appropriate
function loadPendingStatusButtons(access, userPhoneNumber) {
    if (access.hasManagerAccess) {
        const acceptDeclineButtonGroup = document.getElementById("acceptDeclineButtonGroup")
        initializeButtonGroupEventListener(acceptDeclineButtonGroup, userPhoneNumber);
        acceptDeclineButtonGroup.classList.remove("hidden");
    }

    if (access.hasVolunteerAccess) {
        const cancelButtonGroup = document.getElementById("cancelButtonGroup");
        initializeButtonGroupEventListener(cancelButtonGroup, userPhoneNumber);
        cancelButtonGroup.classList.remove("hidden");
    }
}

// Loads the cancel button
function loadCancelButton(userPhoneNumber) {
    const cancelButtonGroup = document.getElementById("cancelButtonGroup");
    initializeButtonGroupEventListener(cancelButtonGroup, userPhoneNumber);
    cancelButtonGroup.classList.remove("hidden");
};

// displays the declined or cancelled message
function loadDeclinedOrCancelledMessage() {
    const message = document.getElementById("requestInactiveMessage");
    message.classList.remove("hidden");
};

// Initialize event listener for provided status change button group
function initializeButtonGroupEventListener(buttonGroup , userPhoneNumber) {
    buttonGroup.addEventListener("click", (event) => {
        const isButton = event.target.nodeName === 'BUTTON';
        if(isButton){
           const urlKey = document.getElementById("delivery-details").dataset.urlkey;
           const newStatus = event.target.dataset.request;
           return handleStatusUpdate(urlKey, userPhoneNumber, newStatus);
        }
    });
};

// Send update request and fills the data
// If error display error message
async function handleStatusUpdate(urlKey, phoneNumber, status) {
    try {
        updatedDeliveryData = await updateStatus(urlKey, phoneNumber, status);

        // Load update success message

        // Load the updated data
        debugger;
//        loadDeliveryData(updatedDeliveryData);
    } catch (e) {
        console.log(e)
        // Display error message
    }

}

// Sends update request and returns updated data
async function updateStatus(urlKey, phoneNumber, status) {
    // send the urlKey ,update text and the user phone number as the request body
    const response = await fetch("/volunteer/delivery/update", {
        method: "POST",
        headers: {'Content-Type': "application/json"},
        body: JSON.stringify({
            "phoneNumber": phoneNumber,
            "urlKey": urlKey.toUpperCase(),
            "status": status.toUpperCase()
        })
    });

    if (response.ok) {
        return await response.json();
    } else {
        // todo: Handle Error
        console.log("An error occurred!");
        throw Error(e);
    }
}


// Finds the element by the provided elementId.
// Changes the textContent and then removes hidden element
function loadSiteDetail(elementId, value) {
    const detailElement = document.getElementById(elementId);
    detailElement.textContent = value;
    detailElement.parentElement.classList.remove("hidden");
}

function displayVerificationError() {
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