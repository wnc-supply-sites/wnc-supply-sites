async function showUpdateConfirmation(siteId) {
  let newNumber =
      document.getElementById("contactNumber").value;


  try {
    await sendContactUpdate(siteId, newNumber)

    document.getElementById("update-confirm").style.display = 'block';
    document.getElementById("update-button").innerHTML = "Updated";

    setTimeout(function () {
          document.getElementById("update-confirm").style.display = 'none';
          document.getElementById("update-button").innerHTML = "Update";
        },
        1500);

    if (newNumber === "") {
      document.getElementById("contactUpdateConfirm").innerHTML =
          `Contact number was deleted`;
    } else {
      document.getElementById("contactUpdateConfirm").innerHTML =
          `Contact number updated to: ${newNumber}`;
    }

  } catch (error) {
    document.getElementById("contactUpdateConfirm").innerHTML =
        `An error occurred, contact number was not updated`;
  }
}

async function sendContactUpdate(siteId, contactNumber) {
  const url = "/manage/update-contact";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      contactNumber: contactNumber
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response}`);
  }
  return await response.json();
}
