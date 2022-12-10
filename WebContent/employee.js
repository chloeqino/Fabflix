let star_form = $("#star_form");
let movie_form = $("#movie_form");

function handleStarResult(resultDataString) {
  console.log(resultDataString);
  // let resultDataJson = JSON.parse(resultDataString);

  console.log("handle login response");
  console.log(resultDataString["status"]);

  // If login succeeds, it will redirect the user to index.html
  if (resultDataString["status"] === "success") {
    $("#add_star_message").text(resultDataString["message"]);
  } else {
    // If login fails, the web page will display
    // error messages on <div> with id "login_error_message"
    console.log("show error message");
    console.log(resultDataString["message"]);
    $("#add_star_message").text(resultDataString["message"]);
  }
}

function handleMovieResult(resultDataString) {
  console.log(resultDataString);
  // let resultDataJson = JSON.parse(resultDataString);

  console.log("handle login response");
  console.log(resultDataString["status"]);

  // If login succeeds, it will redirect the user to index.html
  if (resultDataString["status"] === "success") {
    $("#add_movie_message").text(resultDataString["message"]);
  } else {
    // If login fails, the web page will display
    // error messages on <div> with id "login_error_message"
    console.log("show error message");
    console.log(resultDataString["message"]);
    $("#add_movie_message").text(resultDataString["message"]);
  }
}

function submitLoginForm(formSubmitEvent) {
  console.log("submit login form");
  /**
   * When users click the submit button, the browser will not direct
   * users to the url defined in HTML form. Instead, it will call this
   * event handler when the event is triggered.
   */
  formSubmitEvent.preventDefault();

    $.ajax("api/employee", {
        method: "POST",
        dataType: "json",
        // Serialize the login form to the data sent by POST request
        data: star_form.serialize(),
        success: handleStarResult,
    });
}

function submitMovieForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax("api/add-movie", {
        method: "POST",
        dataType: "json",
        // Serialize the login form to the data sent by POST request
        data: movie_form.serialize(),
        success: handleMovieResult,

    });
}

star_form.submit(submitLoginForm);
movie_form.submit(submitMovieForm);
