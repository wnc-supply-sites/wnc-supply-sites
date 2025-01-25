function sendUpdate() {
  let dataMissing = false;

  const location = document.getElementById("location");
  if(location.value.trim() === "") {
    location.classList.add("missingData");
    dataMissing = true;
  } else {
    location.classList.remove("missingData");
  }

  const licensePlates = document.getElementById("licensePlates");
  if(licensePlates.value.trim() === "") {
    licensePlates.classList.add("missingData");
    dataMissing = true;
  } else {
    licensePlates.classList.remove("missingData");
  }

  const availability = document.getElementById("availability");
  const comments = document.getElementById("comments");
  const errorDiv = document.getElementById("errorMessage");
  const canLift50lbs = document.getElementById("can-lift").value;
  const palletCapacity = document.getElementById("pallet-capacity").value;

  if(dataMissing) {
    errorDiv.innerHTML = "Missing data in required fields";
    document.getElementById("greenCheck").classList.add("hidden");
    document.getElementById("confirmation").innerHTML = "";
    return;
  }

  fetch("/driver/update", {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      location: location.value.trim(),
      licensePlates: licensePlates.value.trim(),
      availability: availability.value.trim(),
      comments: comments.value.trim(),
      canLift50lbs: canLift50lbs,
      palletCapacity: palletCapacity,
    })
  })
  .then(
      async function (response) {
        if (response.ok) {
          document.getElementById("greenCheck").classList.remove("hidden");
          document.getElementById("confirmation").innerHTML = "Updated!";

          setTimeout(function () {
                document.getElementById("greenCheck").classList.add("hidden");
                document.getElementById("confirmation").innerHTML = "";
              },
              2500);

        } else {
          const responseJson = await response.json();
          errorDiv.innerHTML = "Error: " + responseJson;
          document.getElementById("greenCheck").classList.add("hidden");
          document.getElementById("confirmation").innerHTML = "";
        }
      },
      function (error) {
        errorDiv.innerHTML = "Failed, server error (server not available): " + error;
        document.getElementById("greenCheck").classList.add("hidden");
        document.getElementById("confirmation").innerHTML = "";
      });
}
