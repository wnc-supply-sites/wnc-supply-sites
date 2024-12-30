const divList = [
    "phone-number-div",
    "access-code-div",
    "set-password-div",
    "password-updated-success-div"
];

function showAccessCodeDiv() {
  hideAll();
  showDiv("access-code-div");
}

function showSetPasswordDiv() {
  hideAll();
  showDiv("set-password-div");
}

function showPasswordUpdatedDiv() {
  hideAll();
  showDiv("password-updated-success-div");
}

function hideAll() {
  divList.forEach((item) => {
    document.getElementById(item).classList.add("hidden");
  })
}

function showDiv(div) {
  document.getElementById(div).classList.remove("hidden");
}
