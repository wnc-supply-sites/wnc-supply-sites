function goBack(){
  //if it was the first page
  if(history.length === 1){
    window.location = "/supplies/site-list"
  } else {
    history.back();
  }
}
