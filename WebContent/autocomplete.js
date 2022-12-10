function handleLookup(query, doneCallback) {
  console.log("autocomplete initiated");
  console.log("sending AJAX request to backend Java Servlet");
  console.log(query);

  // TODO: if you want to check past query results first, you can do it here
  if (localStorage.getItem(query)) {
    console.log("Cached");
    handleLookupAjaxSuccess(localStorage.getItem(query), query, doneCallback);
  } else {
    console.log("server");
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
      method: "GET",
      // generate the request url from the query.
      // escape the query string to avoid errors caused by special characters
      url: "api/autocomplete?query=" + escape(query) + "&normal=false",
      success: function (data) {
        // pass the data, query, and doneCallback function into the success handler
        handleLookupAjaxSuccess(data, query, doneCallback);
      },
      error: function (errorData) {
        console.log("lookup ajax error");
        console.log(errorData);
      },
    });
  }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
  console.log("lookup ajax successful");

  // parse the string into JSON
  var jsonData = JSON.parse(data);
  console.log(jsonData);

  // TODO: if you want to cache the result into a global variable you can do it here
  localStorage.setItem(query, data);

  // call the callback function provided by the autocomplete library
  // add "{suggestions: jsonData}" to satisfy the library response format according to
  //   the "Response Format" section in documentation
  doneCallback({ suggestions: jsonData });
}

function handleNormalSearch(query) {
  window.location.href = "results.html?title=" + query + "&type=autocomplete";
}

function handleSelectSuggestion(suggestion) {
  // TODO: jump to the specific result page based on the selected suggestion
  window.location.href =
    "single-movie.html?id=" + suggestion["data"]["movieID"];

  console.log(
    "you select " +
      suggestion["value"] +
      " with ID " +
      suggestion["data"]["heroID"]
  );
}

let autocomplete_form = jQuery("#autocomplete-form");

$("#autocomplete").autocomplete({
  minChars: 3,
  deferRequestBy: 300,
  lookup: function (query, doneCallback) {
    handleLookup(query, doneCallback);
  },
  onSelect: function (suggestion) {
    handleSelectSuggestion(suggestion);
  },
});

$("#autocomplete").keypress(function (event) {
  if (event.keyCode == 13) {
    handleNormalSearch($("#autocomplete").val());
  }
});

autocomplete_form.submit(function (e) {
  e.preventDefault();
  handleNormalSearch($("#autocomplete").val());
});
