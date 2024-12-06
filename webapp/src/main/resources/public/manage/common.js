async function changeCounty() {
  const state = document.getElementById('State').value;
  const counties = countyMapByState[state];
  document.getElementById('County')
      .innerHTML =
      counties.map(c =>
          `<option value="${c}">${c}</option>`
      ).sort()
}
