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
let limit_input = jQuery("#limit");
let sort1_input = jQuery("#sort1");
let order1_input = jQuery("input[type=radio][name=order1]:checked");
let sort2_input = jQuery("#sort2");
let order2_input = jQuery("input[type=radio][name=order2]:checked");
let nextBtn = jQuery("button#next");
let prevBtn = jQuery("button#prev");
if (
  !localStorage.getItem("setting") ||
  localStorage.getItem("query") != query.substring(1)
) {
  localStorage.setItem("setting", JSON.stringify(settings));
} else {
  settings = JSON.parse(localStorage.getItem("setting"));

  settings.limit = parseInt(settings.limit);
  settings.page = parseInt(settings.page);
}
limit_input.val(settings.limit);
sort1_input.val(settings.sort1);
sort2_input.val(settings.sort2);
jQuery(`#${settings.order1}1`).prop("checked", true);
jQuery(`#${settings.order2}2`).prop("checked", true);

nextBtn.click(function () {
  settings.page = settings.page + 1;
  updateSetting();
});
prevBtn.click(function () {
  settings.page = settings.page - 1;
  updateSetting();
});
limit_input.change(function () {
  settings.limit = parseInt(limit_input.val());
  updateSetting();
});
jQuery("input[type=radio][name=order1]").change(function () {
  order1_input = jQuery("input[type=radio][name=order1]:checked");
  settings.order1 = order1_input.val();
  console.log(order1_input.val());
  updateSetting();
});
sort1_input.change(function () {
  settings.sort1 = sort1_input.val();
  if (sort1_input.val() == "title") {
    sort2_input.val("rating");
    settings.sort2 = "rating";
  } else {
    sort2_input.val("title");
    settings.sort2 = "title";
  }
  updateSetting();
  console.log(sort1_input.val());
});
jQuery("input[type=radio][name=order2]").change(function () {
  order2_input = jQuery("input[type=radio][name=order2]:checked");
  settings.order2 = order2_input.val();
  updateSetting();
  console.log(order2_input.val());
});
sort2_input.change(function () {
  settings.sort2 = sort2_input.val();
  if (sort2_input.val() == "title") {
    sort1_input.val("rating");
    settings.sort1 = "rating";
  } else {
    sort1_input.val("title");
    settings.sort1 = "title";
  }
  updateSetting();
  console.log(sort2_input.val());
});
function querify(jsonObj) {
  let substrList = [];
  for (var k in jsonObj) {
    substrList.push(k + "=" + jsonObj[k]);
  }
  return substrList.join("&");
}
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
    htmlstr +=
      `<li><a href="results.html?type=genre&id=${genresList[i]["id"]}">` +
      genresList[i]["name"] +
      "</a></li>";
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
function cartAlert(resultData) {
  getCartItems();
}
let timeoutid = [];
function clearAllTimeOut(arr) {
  console.log(arr);
  for (let i = 0; i < arr.length; i++) {
    clearTimeout(arr[i]);
  }
}
function toggleToolTip(resultData) {
  getCartItems();
  clearAllTimeOut([...timeoutid].slice(0, -1));
  jQuery("#cart-tooltip").removeClass("dn");
  let id = setTimeout(() => {
    jQuery("#cart-tooltip").addClass("dn");
  }, 4000);
  timeoutid.push(id);
}
function removeFromCart(id) {
  console.log(id);
  jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/cart?action=remove&id=" + id,
    success: (resultData) => populateCartItems(resultData),
  });
}
function deleteFromCart(id) {
  console.log(id);
  jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/cart?action=delete&id=" + id,
    success: (resultData) => populateCartItems(resultData),
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
function addToCartandAlert(id) {
  console.log(id);
  jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/cart?action=add&id=" + id,
    success: (resultData) => toggleToolTip(resultData),
  });
}
function handleResult(resultData) {
  nextBtn.prop("disabled", !resultData["hasNext"]);
  prevBtn.prop("disabled", settings.page <= 1);

  console.log(resultData);
  let starTableBodyElement = jQuery("#top_movies_table_body");
  starTableBodyElement.empty();
  let moviesList = resultData["movies"];
  // Iterate through resultData, no more than 10 entries
  for (let i = 0; i < Math.min(settings.limit, moviesList.length); i++) {
    // Concatenate the html tags with resultData jsonObject
    let rowHTML = "";
    let movieObj = moviesList[i];
    rowHTML += "<tr>";

    rowHTML +=
      "<th class='no'>" +
      (i + 1) +
      "</th>" +
      "<th><a href='single-movie.html?id=" +
      movieObj["id"] +
      "'>" +
      movieObj["title"] +
      "</a><span class='year'>(" +
      movieObj["year"] +
      ")</span><br><ul class='genres'>" +
      genresHtml(movieObj["genres"]) +
      "<ul></th>";

    rowHTML +=
      "<th class='starsdirector'><section class='lists'><h3 class='subheading'>director: </h3>" +
      movieObj["director"] +
      "</section><section class='lists'><h3 class='subheading'>stars: </h3><ul class='stars middledot'>" +
      starsHtml(movieObj["stars"]) +
      "</ul></section></th>";
    rowHTML +=
      "<th><span class='star'>&#10029;</span>" + movieObj["rating"] + "</th>";

    let funcStr = `(function(){addToCartandAlert('${movieObj["id"]}')})()`;
    //console.log(cartBtn);
    rowHTML += `<th><button class="btn btn-primary" onclick=${funcStr}>Add to Cart ${movieObj["price"]}</button></th>`;

    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    starTableBodyElement.append(rowHTML);
  }
}
function updateSetting() {
  localStorage.setItem("setting", JSON.stringify(settings));
  if (type == "search") {
    jQuery.ajax({
      dataType: "json",
      method: "GET",
      url: "api/search" + query + "&" + querify(settings),
      success: (resultData) => handleResult(resultData),
    });
  } else if (type == "genre") {
    jQuery.ajax({
      dataType: "json",
      method: "GET",
      url: "api/genres" + query + "&" + querify(settings),
      success: (resultData) => handleResult(resultData),
    });
  } else if (type == "char") {
    jQuery.ajax({
      dataType: "json",
      method: "GET",
      url: "api/first-letter" + query + "&" + querify(settings),
      success: (resultData) => handleResult(resultData),
    });
  } else if (type == "autocomplete") {
    jQuery.ajax({
      dataType: "json",
      method: "GET",
      url: "api/autocomplete" + query + "&normal=true&" + querify(settings),
      success: (resultData) => handleResult(resultData),
    });
  }
}

console.assert(getParameterByName("limit") == "");
console.log(localStorage.getItem("setting"));
let type = getParameterByName("type");
console.log(type);
if (type == "search") {
  jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/search" + query + "&" + querify(settings),
    success: (resultData) => handleResult(resultData),
  });
} else if (type == "genre") {
  jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/genres" + query + "&" + querify(settings),
    success: (resultData) => handleResult(resultData),
  });
} else if (type == "char") {
  jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/first-letter" + query + "&" + querify(settings),
    success: (resultData) => handleResult(resultData),
  });
} else if (type == "autocomplete") {
  jQuery("#autocomplete").val(getParameterByName("title"));
  console.log("api/autocomplete" + query + "&normal=true&" + querify(settings));
  jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/autocomplete" + query + "&normal=true&" + querify(settings),
    success: (resultData) => handleResult(resultData),
  });
}

let itemsList = jQuery("ul#cartItems");
function populateCartItems(data) {
  jQuery("#t-price").text(`$${data["total"]}`);
  itemsList.empty();
  let cartItems = data["items"];
  for (let i = 0; i < cartItems.length; i++) {
    let incFuc = `(function(){addToCart('${cartItems[i]["id"]}')})()`;
    let decFuc = `(function(){removeFromCart('${cartItems[i]["id"]}')})()`;
    let delFuc = `(function(){deleteFromCart('${cartItems[i]["id"]}')})()`;

    let htmlList = `<li class="list-group-item d-flex justify-content-between">
        <span class="item-title">${cartItems[i]["title"]}</span>
        <div><button type="button" class="btn btn-outline-secondary" onclick=${decFuc}>-</button>
        <span>${
          cartItems[i]["quantity"]
        }</span><button type="button" class="btn btn-outline-secondary" onclick=${incFuc}>+</button></div>
        <span>$${(
          parseFloat(cartItems[i]["price"]) *
          parseFloat(cartItems[i]["quantity"])
        ).toPrecision(4)}</span>
        <button type="button" class="btn btn-light" onclick=${delFuc}>
        <span class="material-symbols-outlined">
        delete
        </span>
        </button>
        </li>`;
    itemsList.append(htmlList);
  }
}
function getCartItems() {
  jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart",
    success: (resultData) => populateCartItems(resultData),
  });
}
getCartItems();
console.log(query);

localStorage.setItem("query", query.substring(1));
console.log(localStorage.getItem("query"));
