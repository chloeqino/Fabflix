let checkout_form = jQuery("form#checkout-form");

function handleCheckOutResult(resultDataString) {
  let resultDataJson = JSON.parse(resultDataString);

  console.log(resultDataJson);
  console.log(resultDataJson["status"]);

  // If login succeeds, it will redirect the user to index.html
  if (resultDataJson["status"] === "success") {
    window.location.replace("confirm.html");
  } else {
    // If login fails, the web page will display
    // error messages on <div> with id "login_error_message"
    console.log("show error message");
    jQuery("#err").removeClass("dn");
  }
}
checkout_form.submit(function (e) {
  e.preventDefault();
  console.log(checkout_form.serialize());
  $.ajax("api/checkout", {
    method: "POST",
    // Serialize the login form to the data sent by POST request
    data: checkout_form.serialize(),
    success: handleCheckOutResult,
  });
});
