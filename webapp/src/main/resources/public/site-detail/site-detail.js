function goBack(){
  //if it was the first page
  if(history.length === 1){
    window.location = "https://wnc-supplies.com/supplies"
  } else {
    history.back();
  }
}
