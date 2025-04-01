
async function handleVerificationSubmission(e) {
    const phoneNumber = document.getElementById("phone-verification").value.replaceAll("-", "");
    const urlKey = document.getElementById("requestData").dataset.urlkey;
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

    const volunteerContent = document.getElementById("requestData");

    const verificationContainer = document.getElementById("verification-container");
    verificationContainer.classList.add("hidden");
    volunteerContent.classList.remove("hidden");
};

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