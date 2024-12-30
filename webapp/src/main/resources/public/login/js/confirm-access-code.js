function confirmAccessCode() {
  const fieldValue = document.getElementById("confirm-code")
      .value.trim().replace(/\D/g, '');

  if(fieldValue.length !== 6) {
    showConfirmCodeError("Confirm code not valid");
  } else {
    document.getElementById("confirm-code-error-message")
        .innerHTML = "";
  }
  const csrf = document.getElementById("csrf").value;

  fetch("/confirm-access-code", {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      confirmCode: fieldValue,
      csrf: csrf
    })
  })
  .then(
      async function (response) {
        if (response.ok) {
          const responseJson = await response.json();
          document.getElementById("validationToken").value = responseJson.validationToken
          showSetPasswordDiv();
        } else {
          const responseJson = await response.json();
          showConfirmCodeError("Error from server: " + responseJson.error);
        }
      },
      function (error) {
        console.log(error);
        showConfirmCodeError("Failed to confirm access code, server error: " + error);
      });
}

function showConfirmCodeError(message) {
  document.getElementById("confirm-code-error-message")
      .innerHTML = message;
}
