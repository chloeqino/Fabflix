/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */

function getParameterByName(target) {
  // Get request URL
  let url = window.location.href;
  // Encode target parameter name to url encoding
  target = target.replace(/[\[\]]/g, "\\$&");

  // Ues regular expression to find matched parameter value
  let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
    results = regex.exec(url);
  if (!results) return null;
  if (!results[2]) return "";

  // Return the decoded parameter value
  return decodeURIComponent(results[2].replace(/\+/g, " "));
}
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
      starsList[i]["id"] +
      "'>" +
      starsList[i]["name"] +
      "</a></li>";
  }
  return htmlstr;
}
/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
  console.log("handleResult: populating move info from resultData");

  // populate the star info h3
  // find the empty h3 body by id "star_info"

  jQuery(".movie-title").append(resultData["title"]);
  jQuery("#year").append(resultData["year"]);
  jQuery("#director").append(resultData["director"]);
  jQuery(".genres").append(genresHtml(resultData["genres"]));
  jQuery("#stars").append(starsHtml(resultData["stars"]));
  let r = resultData["rating"];
  if (r) {
    jQuery(".rating").append(resultData["rating"]);
  } else {
    jQuery(".rating").text("No Rating");
    jQuery(".rating").addClass("na");
    jQuery("svg.star").css("fill", "#a6a9af");
    jQuery("#outof").remove();
  }

  console.log("handleResult: populating movie table from resultData");
  let movieTableBodyElement = jQuery("#movie_table_body");
  for (let i = 0; i < resultData["stars"].length; i++) {
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML +=
      "<th>" +
      // Add a link to single-star.html with id passed with GET url parameter
      '<a href="single-star.html?id=' +
      resultData["stars"][i]["id"] +
      '">' +
      resultData["stars"][i]["name"] + // display star_name for the link text
      "</a>" +
      "</th>";
    rowHTML += "</tr>";
    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
  }
}
if (localStorage.getItem("query")) {
  jQuery("a#back").attr(
    "href",
    "results.html?" + localStorage.getItem("query")
  );
  jQuery("a#back").text("Movies List");
}
/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName("id");

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
  dataType: "json", // Setting return data type
  method: "GET", // Setting request method
  url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
  success: (resultData) => handleResult(resultData), // Setting callback function to handle data returned successfully by the SingleStarServlet
});
