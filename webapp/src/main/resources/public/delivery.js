function toggleConfirmCancel() {
  document.getElementById("cancel-button").classList.add("hidden");
  document.getElementById("confirm-cancel").classList.remove("hidden");
}

function sendCancel(cancelLink) {
  const cancelReason = document.getElementById( "cancelReason").value.trim();

  fetch(cancelLink + "&cancelReason=" + cancelReason, {
    method: 'GET',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    }
  })
  .then(
      async function (response) {
        if (response.ok) {
          window.location.reload();
        } else {
          const responseJson = await response.json();
          alert("Server error: " + responseJson);
        }
      },
      function (error) {
        alert("Failed to save, server error (not available): " + error);
      });
}
