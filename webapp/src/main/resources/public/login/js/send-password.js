function togglePassword() {
  var x = document.getElementById("password");
  if (x.type === "password") {
    x.type = "text";
  } else {
    x.type = "password";
  }
}

function sendPassword() {
  const validationToken = document.getElementById("validationToken").value;
  const password = document.getElementById("password").value;

  if (password.length < 5) {
    showPasswordError("Password too short");
  } else {
    document.getElementById("phone-number-error-message")
        .innerHTML = "";
  }

  fetch("/set-password", {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      validationToken: validationToken,
      password: password
    })
  })
  .then(
      async function (response) {
        if (response.ok) {
          window.location.href = "/";
          // const responseJson = await response.json();
          // document.getElementById("csrf").value = responseJson.csrf;
        } else {
          const responseJson = await response.json();
          showPasswordError("Error: " + responseJson.error);
        }
      },
      function (error) {
        showPasswordError("Failed, server error: " + error);
      });
}

function showPasswordError(message) {
  document.getElementById("password-error-message")
      .innerHTML = message;
}
