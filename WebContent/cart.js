let itemsList = jQuery("ul#cartItems");
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
function addToCartandAlert(id) {
  console.log(id);
  jQuery.ajax({
    dataType: "json",
    method: "POST",
    url: "api/cart?action=add&id=" + id,
    success: (resultData) => toggleToolTip(resultData),
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
  jQuery("#t-price").text(`$${data["total"]}`);
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
          <span class="item-price">$${cartItems[i]["total"]}</span>
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
jQuery(".add-to-cart").click(function () {
  addToCartandAlert(movieId);
});
console.log("reloading");
getCartItems();
