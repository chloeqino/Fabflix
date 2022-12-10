let login_form = $("#login_form");
let submit_btn = $("#submit-btn");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
  let resultDataJson = JSON.parse(resultDataString);

  console.log("handle login response");
  console.log(resultDataJson);
  console.log(resultDataJson["status"]);

  // If login succeeds, it will redirect the user to index.html
  if (resultDataJson["status"] === "success") {
    window.location.replace("index.html");
  } else {
    // If login fails, the web page will display
    // error messages on <div> with id "login_error_message"
    console.log("show error message");

    console.log(resultDataJson["message"]);
    submit_btn.prop("disabled", false);
    submit_btn.html("Login");
    grecaptcha.reset();
    jQuery("#err").removeClass("dn");
    jQuery("#err").text(resultDataJson["message"]);
  }
}

function handleDashboard() {
  window.location.replace("_dashboard");
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
  console.log("submit login form");
  submit_btn.html(`<div class="spinner-border text-light" role="status">
  <span class="visually-hidden">Loading...</span>
</div>`);
  submit_btn.prop("disabled", true);

  /**
   * When users click the submit button, the browser will not direct
   * users to the url defined in HTML form. Instead, it will call this
   * event handler when the event is triggered.
   */
  formSubmitEvent.preventDefault();

  $.ajax("api/login", {
    method: "POST",
    // Serialize the login form to the data sent by POST request
    data: login_form.serialize(),
    success: handleLoginResult,
  });
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);
