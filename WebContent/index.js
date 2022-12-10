/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
let itemsList = jQuery("ul#cartItems");
let initials = [
  "A",
  "B",
  "C",
  "D",
  "E",
  "F",
  "G",
  "H",
  "I",
  "J",
  "K",
  "L",
  "M",
  "N",
  "O",
  "P",
  "Q",
  "R",
  "S",
  "T",
  "U",
  "V",
  "W",
  "X",
  "Y",
  "Z",
  0,
  1,
  2,
  3,
  4,
  5,
  6,
  7,
  8,
  9,
  "*",
];

// autocomplete
function handleLookup(query, doneCallback) {
  console.log("autocomplete initiated")
  console.log("sending AJAX request to backend Java Servlet")
  console.log(query);

  // TODO: if you want to check past query results first, you can do it here
  if (localStorage.getItem(query)){
    console.log("Cached")
    handleLookupAjaxSuccess(localStorage.getItem(query), query, doneCallback)
  }
  else {
    console.log("server")
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
      "method": "GET",
      // generate the request url from the query.
      // escape the query string to avoid errors caused by special characters
      "url": "api/autocomplete?query=" + escape(query) + "&normal=false",
      "success": function (data) {
        // pass the data, query, and doneCallback function into the success handler
        handleLookupAjaxSuccess(data, query, doneCallback)
      },
      "error": function (errorData) {
        console.log("lookup ajax error")
        console.log(errorData)
      }
    })
  }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
  console.log("lookup ajax successful")

  // parse the string into JSON
  var jsonData = JSON.parse(data);
  console.log(jsonData)

  // TODO: if you want to cache the result into a global variable you can do it here
  localStorage.setItem(query, data);

  // call the callback function provided by the autocomplete library
  // add "{suggestions: jsonData}" to satisfy the library response format according to
  //   the "Response Format" section in documentation
  doneCallback( { suggestions: jsonData } );
}

function handleNormalSearch(query) {
  window.location.href = 'results.html?title=' + query + '&type=autocomplete';
}

function handleSelectSuggestion(suggestion) {
  // TODO: jump to the specific result page based on the selected suggestion
  window.location.href = 'single-movie.html?id=' + suggestion["data"]["movieID"];

  console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["heroID"]);
}

let autocomplete_form = jQuery("#autocomplete-form");

$("#autocomplete").autocomplete({
  minChars:3,
  deferRequestBy: 300,
  lookup: function (query, doneCallback) {
    handleLookup(query, doneCallback)
  },
  onSelect: function(suggestion) {
    handleSelectSuggestion(suggestion)
  },
})

$("#autocomplete").keypress(function(event) {
  if (event.keyCode ==13) {
    handleNormalSearch($("#autocomplete").val())
  }
})

autocomplete_form.submit(function (e) {
  e.preventDefault()
  handleNormalSearch($("#autocomplete").val())
});

let advanced_search_form = jQuery("#advanced-search");
function querify(jsonObj) {
  let substrList = [];
  for (var k in jsonObj) {
    substrList.push(k + "=" + jsonObj[k]);
  }
  return substrList.join("&");
}
advanced_search_form.submit(function (e) {
  e.preventDefault();
  console.log(advanced_search_form.serialize());
  localStorage.setItem("query", advanced_search_form.serialize());
  window.location.href = "results.html?" + advanced_search_form.serialize();
});
function genresHtml(genresList) {
  let htmlstr = "";
  for (let i = 0; i < genresList.length; i++) {
    htmlstr += "<li>" + genresList[i] + "</li>";
  }
  return htmlstr;
}
function starsHtml(starsList) {
  let htmlstr = "";
  for (let i = 0; i < starsList.length; i++) {
    htmlstr +=
        "<li><a href='single-star.html?id=" +
        starsList[i]["star_id"] +
        "'>" +
        starsList[i]["star_name"] +
        "</a></li>";
  }
  return htmlstr;
}
let char_list = jQuery("ol#char-list");
function displayCharList() {
  char_list.empty();
  initials.forEach((e, i) => {
    let liHtML = `<li>`;
    liHtML += `<a href="results.html?type=char&initial=${e}">
    ${e}</a></li>`;
    char_list.append(liHtML);
  });
}
let genre_list = jQuery("ul#genre-list");
function displayGenreList(resultData) {
  genre_list.empty();
  for (let i = 0; i < resultData.length; i++) {
    let liHtml = `<li id="genre${resultData[i]["id"]}">`;
    liHtml += `<a href="results.html?type=genre&id=${resultData[i]["id"]}">
    ${resultData[i]["name"]}</a></li>`;
    genre_list.append(liHtml);
  }
}
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
  console.log("handleStarResult: populating star table from resultData");

  // Populate the star table
  // Find the empty table body by id "star_table_body"
  let starTableBodyElement = jQuery("#top_movies_table_body");

  // Iterate through resultData, no more than 10 entries
  for (let i = 0; i < Math.min(20, resultData.length); i++) {
    // Concatenate the html tags with resultData jsonObject
    let rowHTML = "";
    rowHTML += "<tr>";

    rowHTML +=
        "<th class='no'>" +
        (i + 1) +
        "</th>" +
        "<th><a href='single-movie.html?id=" +
        resultData[i]["movie_id"] +
        "'>" +
        resultData[i]["movie_title"] +
        "</a><span class='year'>(" +
        resultData[i]["movie_year"] +
        ")</span><br><ul class='genres'>" +
        genresHtml(resultData[i]["genres"]) +
        "<ul></th>";

    rowHTML +=
        "<th class='starsdirector'><section class='lists'><h3 class='subheading'>director: </h3>" +
        resultData[i]["movie_director"] +
        "</section><section class='lists'><h3 class='subheading'>stars: </h3><ul class='stars middledot'>" +
        starsHtml(resultData[i]["stars"]) +
        "</ul></section></th>";
    rowHTML +=
        "<th><span class='star'>&#10029;</span>" +
        resultData[i]["movie_rating"] +
        "</th>";
    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    starTableBodyElement.append(rowHTML);
  }
}

function handleLogout(){
  jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "POST", // Setting request method
    url: "api/logout", // Setting request url, which is mapped by StarsServlet in Stars.java
  });
  window.location.href ="login.html";
}
function setDefaultSetting() {
  let query = window.location.search;
  let limit = 10;
  let sort1 = "rating";
  let order1 = "desc";
  let sort2 = "title";
  let order2 = "asc";
  let page = 1;
  let settings = {
    limit: limit,
    sort1: sort1,
    order1: order1,
    sort2: sort2,
    order2: order2,
    page: page,
  };

  localStorage.setItem("setting", JSON.stringify(settings));
}
setDefaultSetting();
/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult

jQuery.ajax({
  dataType: "json", // Setting return data type
  method: "GET", // Setting request method
  url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
  success: (resultData) => handleMovieResult(resultData), // Setting callback function to handle data returned successfully by the StarsServlet
});

jQuery.ajax({
  dataType: "json", // Setting return data type
  method: "GET", // Setting request method
  url: "api/all-genres", // Setting request url, which is mapped by StarsServlet in Stars.java
  success: (resultData) => displayGenreList(resultData), // Setting callback function to handle data returned successfully by the StarsServlet
});
displayCharList();

function cartAlert(resultData) {
  getCartItems();
}
function removeFromCart(id) {
  console.log(id);
  jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/cart?action=remove&id=" + id,
    success: (resultData) =>
        (function () {
          console.log(resultData);
          getCartItems();
        })(),
  });
}
function deleteFromCart(id) {
  console.log(id);
  jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/cart?action=delete&id=" + id,
    success: (resultData) =>
        (function () {
          getCartItems();
          console.log("getting cart info");
        })(),
  });
}
function addToCart(id) {
  console.log(id);
  jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/cart?action=add&id=" + id,
    success: (resultData) => cartAlert(resultData),
  });
}

function populateCartItems(data) {
  itemsList.empty();
  let cartItems = data["items"];
  for (let i = 0; i < cartItems.length; i++) {
    let incFuc = `(function(){addToCart('${cartItems[i]["id"]}')})()`;
    let decFuc = `(function(){removeFromCart('${cartItems[i]["id"]}')})()`;
    let delFuc = `(function(){deleteFromCart('${cartItems[i]["id"]}')})()`;
    let htmlList = `<li class="list-group-item d-flex justify-content-between">
          <span class="item-title">${cartItems[i]["title"]}</span>
          <div class="item-qty"><button type="button" class="btn btn-outline-secondary dec" onclick=${decFuc}>-</button>
          <span>${
        cartItems[i]["quantity"]
    }</span><button type="button" class="btn btn-outline-secondary" onclick=${incFuc} inc>+</button></div>
          <span class="item-price">$${cartItems[i]["quantity"] * 10}</span>
          <button type="button" class="btn btn-light del" onclick=${delFuc}>
          <span class="material-symbols-outlined">
          delete
          </span>
          </button>
          </li>`;
    itemsList.append(htmlList);
  }
}
function getCartItems() {
  console.log("getting cart items");
  jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart",
    success: (resultData) => populateCartItems(resultData),
  });
}

getCartItems();
console.log("reloading");
localStorage.removeItem("query");