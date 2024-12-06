async function showUpdateConfirmation(siteId, field) {
  let newValue;

  if(field === 'County' || field === 'State') {
    newValue =
        document.getElementById('County').value + ',' + document.getElementById('State').value;
  } else {
    newValue = document.getElementById(field).value;
  }

  try {
    await sendSiteUpdate(siteId, field, newValue)

    document.getElementById(field + "-update-confirm-checkmark").style.display = 'block';

    setTimeout(function () {
          document.getElementById(field + "-update-confirm-checkmark").style.display = 'none';
        },
        1500);

    if (newValue === "") {
      document.getElementById(field + "-update-confirm").innerHTML =
          `${field} was deleted`;
    } else {
      document.getElementById(field + "-update-confirm").innerHTML =
          `${field} updated to: ${newValue}`;
    }
  } catch (error) {
    document.getElementById(field + "-update-confirm").innerHTML =
        `An error occurred, ${field} was not updated`;
  }
}

async function sendSiteUpdate(siteId, field, newValue) {
  const url = "/manage/update-site";

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      siteId: siteId,
      field: field,
      newValue: newValue
    })
  });

  if (!response.ok) {
    throw new Error(`Response status: ${response.status}, ${response}`);
  }
  return await response.text();
}

