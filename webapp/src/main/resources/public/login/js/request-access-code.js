function togglePassword() {
  var x = document.getElementById("password");
  if (x.type === "password") {
    x.type = "text";
  } else {
    x.type = "password";
  }
}

function sendSms() {
  const fieldValue = document.getElementById("phone-number")
      .value.trim().replace(/\D/g, '');


  if (fieldValue.length < 10) {
    showSendAccessCodeError("Phone number too short");
  } else if (fieldValue.length > 11) {
    showSendAccessCodeError("Phone number too long");
  } else {
    document.getElementById("phone-number-error-message")
        .innerHTML = "";
  }

  fetch("/send-access-code", {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      number: fieldValue
    })
  })
  .then(
      async function (response) {
        if (response.ok) {
          const responseJson = await response.json();
          document.getElementById("csrf").value = responseJson.csrf;
        } else {
          const responseJson = await response.json();
          showSendAccessCodeError("Error from server: " + responseJson.error);
        }
      },
      function (error) {
        showSendAccessCodeError("Failed to send access code, server error: " + error);
      });
}

function showSendAccessCodeError(message) {
  document.getElementById("phone-number-error-message")
      .innerHTML = message;
}
