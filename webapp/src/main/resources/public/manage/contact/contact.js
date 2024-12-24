function updatePhoneNumber(siteId, fieldName) {
  const phoneField = document.getElementById(fieldName);

  const phoneValue = phoneField.value.replace(/\D/g, '');

  if (phoneValue.length != 10 && phoneValue.length != 11) {
    phoneField.classList.add("missingData");

    document.getElementById("Contact Number-update-confirm")
        .innerHTML = "Check phone number, invalid length."
  } else {
    phoneField.classList.remove("missingData");

    document.getElementById("Contact Number-update-confirm")
        .innerHTML = "";
    showUpdateConfirmation(siteId, fieldName);
  }
}

