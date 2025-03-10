function goBack(){
  //if it was the first page
  if(history.length === 1){
    window.location = "/supplies/site-list"
  } else {
    history.back();
  }
}


function addHeaderToItemsList(parentElement) {
    const inventoryList = parentElement.querySelector("ul");
    const items = inventoryList.children;

     for (let i = 0; i < items.length; i++) {
        const item = items[i];

        if (i === 0) {
            let sectionHeader = document.createElement('div');
            sectionHeader.textContent = `${item.dataset.value[0]}`;
            sectionHeader.setAttribute('class','items-section-header');
            item.parentNode.insertBefore(sectionHeader, item);
            i+= 1;
            continue;
        };

        if (i != items.length - 1){
            const nextNeed = items[i + 1]
            if (item.dataset.value[0] !== nextNeed.dataset.value[0]) {
                let sectionHeader = document.createElement('div');
                sectionHeader.textContent = `${nextNeed.dataset.value[0]}`;
                sectionHeader.setAttribute('class','items-section-header');
                item.parentNode.insertBefore(sectionHeader, item.nextSibling);
                i+= 1;
            };
        };
    }
}

function selectInventoryList(listToToggle) {
    const itemsContainers = document.getElementsByClassName('site-items-container');

    for (const container of itemsContainers) {
        console.log(container.id)
        if (container.id === `site-items-${listToToggle}`){
            container.classList.remove("hidden")
        } else {
            container.classList.add("hidden");
        }
    };

}
