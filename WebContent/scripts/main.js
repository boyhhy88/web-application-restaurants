(function() {
  /**
   * Variables
   */
  var user_id = "";
  var user_fullname = "";
  var lng = -122.341;
  var lat = 47.608;

  //--------------------------------------------------------------------------
  // Helper Functions
  //--------------------------------------------------------------------------

  /**
   * A helper function that gets or creates a DOM element <tag options...>
   * @param {*} tag, id if options is not provided; otherwise it is the tag name to be created
   * @param {*} options, object for tag attributes, e.g., {className:..., id:..., href:...}
   * @returns
   */
  function $(tag, options) {
    // if options is not provided
    if (!options) {
      // The getElementById() method returns the element that has the ID attribute with the specified value
      return document.getElementById(tag);
    }

    // The createElement() method creates an Element Node with the specified name.
    var element = document.createElement(tag);
    for (var option in options) {
      // The hasOwnProperty() method returns a boolean indicating whether the object has the specified property as its own property (as opposed to inheriting it).
      // only check for properties that belong to the object itself, not its prototype chain.
      if (options.hasOwnProperty(option)) {
        element[option] = options[option];
      }
    }
    return element;
  }

  /**
   * A helper function that makes a navigation button (in the side bar) active, i.e., hightlighted
   * @param {*} btnId
   */
  function activeBtn(btnId) {
    // The getElementsByClassName() method returns a collection of all elements in the document with the specified class name, as a NodeList object.
    // The NodeList object represents a collection of nodes. The nodes can be accessed by index numbers. The index starts at 0.
    // You can use the length property of the NodeList object to determine the number of elements, then loop through all elements and extract the info you want.
    var btns = document.getElementsByClassName("main-nav-btn");

    // deactivate all navigation buttons
    for (var i = 0; i < btns.length; i++) {
      // The \b metacharacter is used to find a match at the beginning or end of a word.
      // slash '/' is used to open and close the regexp: /...pattern.../
      // string is a sequence characters enclosed in either single or double quotes.
      btns[i].className = btns[i].className.replace(/\bactive\b/, "");
    }

    // activate the one that has id = btnId
    var btn = $(btnId);
    btn.className += " active";
  }

  /**
   * A helper function that shows loading icon and message in the restaurant list section.
   * @param {} msg
   */
  function showLoadingMessage(msg) {
    var restaurantList = $("restaurant-list");
    // The innerHTML property sets or returns the HTML content (inner HTML) of an element.
    restaurantList.innerHTML =
      '<p class="notice"><i class="fa fa-spinner fa-spin"></i>' + msg + "</p>";
  }

  /**
   * A helper function that shows warning icon and message in the restaurant list section.
   * @param {*} msg
   */
  function showWarningMessage(msg) {
    var restaurantList = $("restaurant-list");
    restaurantList.innerHTML =
      '<p class="notice"><i class="fa fa-exclamation-triangle"></i>' +
      msg +
      "</p>";
  }

  /**
   * A helper function that shows error icon and message in the restaurant list section.
   * @param {*} msg
   */
  function showErrorMessage(msg) {
    var restaurantList = $("restaurant-list");
    restaurantList.innerHTML =
      '<p class="notice"><i class="fa fa-exclamation-circle"></i>' +
      msg +
      "</p>";
  }

  /**
   * A helper function that hides an element object.
   * @param {} element
   */
  function hideElement(element) {
    // The style.display property sets or returns the element's display type.
    // if you set display:none, it hides the entire element, while visibility:hidden means that the contents of the element will be invisible, but the element stays in its original position and size.
    element.style.display = "none";
  }

  /**
   * A helper function that shows an element object. Set display:block if style is not provided.
   * @param {*} element
   * @param {*} style
   */
  function showElement(element, style) {
    var displayStyle = style ? style : "block";
    element.style.display = displayStyle;
  }

  /**
   * AJAX helper
   * @param {} method, GET|POST|PUT|DELETE
   * @param {*} url, file location
   * @param {*} data, data to be sent, in JSON format
   * @param {*} callback(response text), function called when request is successful
   * @param {*} errorHandler, function called when request fails
   */
  function ajax(method, url, data, callback, errorHandler) {
    // The XMLHttpRequest object can be used to exchange data with a web server behind the scenes.
    // This means that it is possible to update parts of a web page, without reloading the whole page.
    var xhr = new XMLHttpRequest();

    // open(method, url, async, user, psw):
    // Specifies the request
    // method: the request type GET or POST; url: the file location; async: true (asynchronous) or false (synchronous)
    // user: optional user name; psw: optional password
    xhr.open(method, url, true);

    if (data === null) {
      // send(body) sends the request to the server. Accepts an optional parameter body of data to be sent in the XHR request.
      xhr.send();
    } else {
      // setRequestHeader() sets the value of an HTTP request header. You must call it after calling open(), but before calling send().
      // Content-type: application/json; charset=utf-8: designates the content to be in JSON format, encoded in the UTF-8 character encoding.
      xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
      xhr.send(data);
    }

    // .onload is the function called when an XMLHttpRequest transaction completes successfully.
    xhr.onload = function() {
      // status: Returns the status-number of a request
      switch (xhr.status) {
        // 200: "OK"
        case 200:
          // responseText: Returns the response data as a string
          callback(xhr.responseText);
          break;
        // 400: "Bad Request", error
        case 400:
          errorHandler();
          break;
        // 403: "Forbidden", session invalid
        case 403:
          onSessionInvalid();
          break;
        // 401: "Unauthorized"
        case 401:
          errorHandler();
          break;
      }
    };

    // .onerror is the function called when an XMLHttpRequest transaction fails due to an error.
    xhr.onerror = function() {
      // The console.error() method writes an error message to the console.
      // The console is useful for testing purposes.
      console.error("The request couldn't be completed.");
      errorHandler();
    };
  }

  // -----------------------------------------------------------------------------------------------------
  // AJAX call server-side APIs
  // -----------------------------------------------------------------------------------------------------

  /**
   * API #1
   * Load the nearby restaurants in the restaurant list section
   */
  function loadNearbyRestaurants() {
    console.log("loadNearbyRestaurants");
    activeBtn("nearby-btn");

    // request parameter
    // ./ is relative path
    var url = "./restaurants";
    var params = "user_id=" + user_id + "&lat=" + lat + "&lon=" + lng;
    // When sending data to a web server, the data has to be a string. Convert a JavaScript object into a string with JSON.stringify().
    var data = JSON.stringify({});

    // display loading message
    showLoadingMessage(" Loading nearby restaurants...");

    // make AJAX call
    ajax(
      "GET",
      url + "?" + params,
      data,
      // successful callback
      function(res) {
        // When receiving data from a web server, the data is always a string. Parse the data with JSON.parse(), and the data becomes a JavaScript object.
        var restaurants = JSON.parse(res);
        if (!restaurants || restaurants.length === 0) {
          showWarningMessage(" No nearby restaurant found.");
        } else {
          listRestaurants(restaurants);
        }
      },
      // failed callback
      function() {
        showErrorMessage(" Cannot load nearby restaurants.");
      }
    );
  }

  /**
   * API #2
   * Load favorite (or visited) restaurants in the restaurant list section
   */
  function loadFavoriteRestaurants() {
    console.log("loadFavoriteRestaurants");
    activeBtn("fav-btn");

    // request parameter
    var url = "./history";
    var params = "user_id=" + user_id;
    var data = JSON.stringify({});

    // display loading message
    showLoadingMessage(" Loading favorite restaurants...");

    // make AJAX call
    ajax(
      "GET",
      url + "?" + params,
      data,
      // successful callback
      function(res) {
        var restaurants = JSON.parse(res);
        if (!restaurants || restaurants.length === 0) {
          showWarningMessage(" No favorite restaurant.");
        } else {
          listRestaurants(restaurants);
        }
      },
      // failed callback
      function() {
        showErrorMessage(" Cannot load favorite restaurants.");
      }
    );
  }

  /**
   * API #3
   * Load recommended restaurants in the restaurant list section
   */
  function loadRecommendedRestaurants() {
    console.log("loadRecommendedRestaurants");
    activeBtn("recommend-btn");

    // request parameter
    var url = "./recommendation";
    var params = "user_id=" + user_id;
    var data = JSON.stringify({});

    // display loading message
    showLoadingMessage(" Loading recommended restaurants...");

    // make AJAX call
    ajax(
      "GET",
      url + "?" + params,
      data,
      // successful callback
      function(res) {
        var restaurants = JSON.parse(res);
        if (!restaurants || restaurants.length === 0) {
          showWarningMessage(
            " No recommended restaurant. Make sure you have favorites."
          );
        } else {
          listRestaurants(restaurants);
        }
      },
      // failed callback
      function() {
        showErrorMessage(" Cannot load recommended restaurants.");
      }
    );
  }

  /**
   * API #4
   * Toggle favorite (or visited) restaurants, called when 'fav-link' icon is clicked
   * @param {*} business_id, the restaurant's business id
   */
  function changeFavoriteRestaurant(business_id) {
    // Check whether this restaurant has been liked (visited) or not, get the opposite result (to be set to)
    var li = $("restaurant" + business_id);
    var isVisited = li.dataset.visited !== "true";

    // request parameters
    var url = "./history";
    var data = JSON.stringify({ user_id: user_id, visited: [business_id] });
    var method = isVisited ? "POST" : "DELETE";

    ajax(
      method,
      url,
      data,
      // successful callback
      function(res) {
        var result = JSON.parse(res);
        if (result.status === "OK") {
          li.dataset.visited = isVisited;
          var favIcon = $("fav-icon-" + business_id);
          favIcon.className = isVisited ? "fa fa-heart" : "fa fa-heart-o";
        }
      }
      // skip the failed callback
    );
  }

  // -----------------------------------------------------------------------------------------------------
  // Create restaurant list
  // -----------------------------------------------------------------------------------------------------

  /**
   * List restaurants
   * @param {*} restaurants, an array of restaurant JSON objects
   */
  function listRestaurants(restaurants) {
    // Clear the current results
    var restaurantList = $("restaurant-list");
    restaurantList.innerHTML = "";

    for (var i = 0; i < restaurants.length; i++) {
      addRestaurant(restaurantList, restaurants[i]);
    }
  }

  /**
   * Add a restaurant to the list
   * @param {*} restaurantList, the <ul id="restaurant-list"> tag
   * @param {*} restaurant, the restaurant data (JSON object)
   */
  function addRestaurant(restaurantList, restaurant) {
    var business_id = restaurant.business_id;

    // Create the <li> tag and specify the id and class attributes
    var li = $("li", {
      id: "restaurant" + business_id,
      className: "restaurant"
    });

    // Set the data attribute of the <li> tag
    // The dataset property provides read/write access to all the custom data attributes (data-*) set on the element.
    li.dataset.businessId = business_id;
    li.dataset.visited = restaurant.is_visited;

    // restaurant image
    // The appendChild() method appends a node as the last child of a node.
    li.appendChild($("img", { src: restaurant.image_url, alt: "image error" }));

    // Create <div> tag in <li>, for restaurant name, category, stars and price level
    var section = $("div", {});

    // restaurant name
    // The target attribute specifies where to open the linked document. _blank: Opens the linked document in a new window or tab
    var name = $("a", {
      className: "restaurant-name",
      href: restaurant.url,
      target: "_blank"
    });
    name.innerHTML = restaurant.name;
    section.appendChild(name);

    // restaurant category
    var category = $("p", { className: "restaurant-category" });
    category.innerHTML = "Category: " + restaurant.categories.join(",");
    section.appendChild(category);

    // stars (restaurant rating)
    var stars = $("div", { className: "stars" });
    var stars_title = $("span", {});
    stars_title.innerHTML = "Rating (1 to 5):&nbsp;";
    stars.appendChild(stars_title);
    if (restaurant.stars) {
      // The floor() method rounds a number DOWNWARDS to the nearest integer, and returns the result.
      for (var i = 0; i < Math.floor(restaurant.stars); i++) {
        var star = $("i", { className: "fa fa-star" });
        stars.appendChild(star);
      }
      // "" + restaurant.stars: converts to String type
      // match() method searches a string for a match against a regular expression; n$: Matches any string with n at the end of it; \. represents a dot.
      if (("" + restaurant.stars).match(/\.5$/)) {
        stars.appendChild($("i", { className: "fa fa-star-half-o" }));
      }
    }
    section.appendChild(stars);

    // price level
    var price = $("div", { className: "price" });
    var price_content = $("span", {});
    price_content.innerHTML = "Price Level (1 to 4): ";
    if (restaurant.price) {
      price_content.innerHTML += restaurant.price;
    }
    price.appendChild(price_content);
    section.appendChild(price);

    li.appendChild(section);

    // restaurant address
    var address = $("p", { className: "restaurant-address" });
    // start a new line at comma
    // The g modifier is used to perform a global match (find all matches rather than stopping after the first match).
    if (restaurant.full_address) {
      address.innerHTML = restaurant.full_address.replace(/,/g, "<br/>");
      if (restaurant.distance && restaurant.distance >= 0) {
        address.innerHTML +=
          "<br/>" +
          "(Distance: " +
          (restaurant.distance / 1600).toFixed(2).toString() +
          " mi)";
      }
    }
    li.appendChild(address);

    // favorite link
    var favLink = $("div", { className: "fav-link" });
    // The onclick property is the EventHandler for processing click events on a given element.
    favLink.onclick = function() {
      changeFavoriteRestaurant(business_id);
    };
    // if the restaurant is liked (visited), set as favorite
    favLink.appendChild(
      $("i", {
        id: "fav-icon-" + business_id,
        className: restaurant.is_visited ? "fa fa-heart" : "fa fa-heart-o"
      })
    );

    li.appendChild(favLink);

    restaurantList.appendChild(li);
  }

  // ----------------------------------------------------------------------------------------------------------
  // Main functions
  // ----------------------------------------------------------------------------------------------------------

  /**
   * Initialize
   */
  function init() {
    // Register event listeners
    // addEventListener() sets up a function that will be called whenever the specified event is delivered to the target.
    $("sign-up-btn-top").addEventListener("click", signupPage);
    $("login-btn").addEventListener("click", login);
    $("signup-btn").addEventListener("click", signup);
    $("nearby-btn").addEventListener("click", loadNearbyRestaurants);
    $("fav-btn").addEventListener("click", loadFavoriteRestaurants);
    $("recommend-btn").addEventListener("click", loadRecommendedRestaurants);

    // Trigger sign up button after pressing "Enter" in the "last-name" input field
    // Execute a function when the user releases a key on the keyboard
    $("last-name").addEventListener("keyup", function(event) {
      // Number 13 is the "Enter" key on the keyboard
      if (event.keyCode === 13) {
        // Cancel the default action, if needed
        event.preventDefault();
        // Trigger the button element with a click
        $("signup-btn").click();
      }
    });

    // Trigger log in button after pressing "Enter" in the "password" input field
    $("password").addEventListener("keyup", handleKeyUpPassword);

    clearLoginError();

    validateSession();
  }

  /**
   * Validate Session
   */
  function validateSession() {
    // request parameters
    var url = "./LoginServlet";
    var data = JSON.stringify({});

    // display loading message in the restaurant list section
    showLoadingMessage(" Validating session...");

    // make AJAX call
    ajax(
      "GET",
      url,
      data,
      // session is still valid
      function(res) {
        var result = JSON.parse(res);
        if (result.status === "OK") {
          onSessionValid(result);
        }
      }
    );
  }

  /**
   * Function if logged in and session is still valid, show the webpage for the user
   * @param {*} result, response from server when session is validated, converted to JSON object
   */
  function onSessionValid(result) {
    user_id = result.user_id;
    user_fullname = result.name;

    var signUpBtnTop = $("sign-up-btn-top");
    var signInBtnTop = $("sign-in-btn-top");
    var loginForm = $("login-form");
    var backgroundImg = $("background-img");
    var restaurantNav = $("restaurant-nav");
    var restaurantList = $("restaurant-list");
    var avatar = $("avatar");
    var welcomeMsg = $("welcome-msg");
    var logoutBtn = $("logout-link");

    welcomeMsg.innerHTML = "Welcome, " + user_fullname;

    hideElement(signUpBtnTop);
    hideElement(signInBtnTop);
    hideElement(loginForm);
    showElement(backgroundImg);
    showElement(restaurantNav);
    showElement(restaurantList);
    showElement(avatar);
    showElement(welcomeMsg);
    showElement(logoutBtn);

    initGeoLocation();
  }

  /**
   * Function if session is not valid, show the login webpage
   */
  function onSessionInvalid() {
    var signUpBtnTop = $("sign-up-btn-top");
    var signInBtnTop = $("sign-in-btn-top");
    var loginForm = $("login-form");
    var signupFrom = $("signup-form");
    var signupBtn = $("signup-btn");
    var backgroundImg = $("background-img");
    var restaurantNav = $("restaurant-nav");
    var restaurantList = $("restaurant-list");
    var avatar = $("avatar");
    var welcomeMsg = $("welcome-msg");
    var logoutBtn = $("logout-link");

    showElement(signUpBtnTop);
    hideElement(signInBtnTop);
    showElement(loginForm);
    hideElement(signupFrom);
    hideElement(signupBtn);
    showElement(backgroundImg);
    hideElement(restaurantNav);
    hideElement(restaurantList);
    hideElement(avatar);
    hideElement(welcomeMsg);
    hideElement(logoutBtn);
  }

  /**
   * Get current location and show nearby restaurants; use default location if failed.
   */
  function initGeoLocation() {
    // The Navigator.geolocation read-only property returns a Geolocation object that gives Web content access to the location of the device.
    if (navigator.geolocation) {
      // The Geolocation.getCurrentPosition(success[, error[, [options]]) method is used to get the current position of the device.
      // maximumAge: integer (milliseconds) | infinity - maximum cached position age. If set to 0, it means that the device cannot use a cached position and must attempt to retrieve the real current position.
      navigator.geolocation.getCurrentPosition(
        onPositionUpdated,
        onLoadPositionFailed,
        { maximumAge: 60000 }
      );
      showLoadingMessage(" Retrieving your location...");
    } else {
      onLoadPositionFailed();
    }
  }

  /**
   * Show nearby restaurants after getting the location
   * @param {} position, Position object returned by navigator.geolocation.getCurrentPosition()
   */
  function onPositionUpdated(position) {
    // coords.latitude:	The latitude as a decimal number
    lat = position.coords.latitude;
    // coords.longitude	The longitude as a decimal number
    lng = position.coords.longitude;
    loadNearbyRestaurants();
  }

  /**
   * Function called when location cannot be loaded; use default location.
   */
  function onLoadPositionFailed() {
    // The console.warn() method writes a warning to the console.
    console.warn("navigator.geolocation is not available");

    loadNearbyRestaurants();
  }

  // -----------------------------------------------------------------------------------------------------
  // Log in/Sign up
  // -----------------------------------------------------------------------------------------------------

  /**
   * Show sign up page
   */
  function signupPage() {
    var signUpBtnTop = $("sign-up-btn-top");
    var signInBtnTop = $("sign-in-btn-top");
    var loginForm = $("login-form");
    var signupFrom = $("signup-form");
    var loginBtn = $("login-btn");
    var signupBtn = $("signup-btn");
    var backgroundImg = $("background-img");
    var restaurantNav = $("restaurant-nav");
    var restaurantList = $("restaurant-list");
    var avatar = $("avatar");
    var welcomeMsg = $("welcome-msg");
    var logoutBtn = $("logout-link");

    hideElement(signUpBtnTop);
    showElement(signInBtnTop);
    showElement(loginForm);
    showElement(signupFrom);
    hideElement(loginBtn);
    showElement(signupBtn);
    hideElement(backgroundImg);
    hideElement(restaurantNav);
    hideElement(restaurantList);
    hideElement(avatar);
    hideElement(welcomeMsg);
    hideElement(logoutBtn);

    clearLoginError();

    // Don't trigger log in button after pressing "Enter" in the "password" input field
    var pwd = $("password");
    // For all major browsers, except IE 8 and earlier
    if (pwd.removeEventListener) {
      pwd.removeEventListener("keyup", handleKeyUpPassword);
      // For IE 8 and earlier versions
    } else if (pwd.detachEvent) {
      pwd.detachEvent("keyup", handleKeyUpPassword);
    }
  }

  /**
   * login
   */
  function login() {
    // The value property sets or returns the value of the value attribute of a text field.
    var username = $("username").value;
    var password = $("password").value;
    // md5: a JavaScript function for hashing messages with MD5.
    password = md5(username + md5(password));

    // request parameters
    var url = "./LoginServlet";
    var params = "user_id=" + username + "&password=" + password;
    var data = JSON.stringify({});

    ajax(
      "POST",
      url + "?" + params,
      data,
      // successful callback
      function(res) {
        var result = JSON.parse(res);
        // successfully logged in
        if (result.status === "OK") {
          onSessionValid(result);
        } else {
          showLoginError();
        }
      },
      //error
      function() {
        showLoginError();
      }
    );
  }

  /**
   * signup
   */
  function signup() {
    // The value property sets or returns the value of the value attribute of a text field.
    var username = $("username").value;
    var password = $("password").value;
    var firstName = $("first-name").value;
    var lastName = $("last-name").value;

    if (username.trim() === "" || password.trim() === "") {
      showLoginError();
      return;
    }

    // md5: a JavaScript function for hashing messages with MD5.
    password = md5(username + md5(password));

    // request parameters
    var url = "./SignupServlet";
    var params =
      "user_id=" +
      username +
      "&password=" +
      password +
      "&first_name=" +
      firstName +
      "&last_name=" +
      lastName;
    var data = JSON.stringify({});

    ajax(
      "POST",
      url + "?" + params,
      data,
      // successful callback
      function(res) {
        var result = JSON.parse(res);
        // successfully signed up
        if (result.status === "OK") {
          onSessionValid(result);
        } else {
          showLoginError();
        }
      },
      //error
      function() {
        showLoginError();
      }
    );
  }

  /**
   * Trigger login button after pressing "Enter"
   * @param {*} event
   */
  function handleKeyUpPassword(event) {
    if (event.keyCode === 13) {
      event.preventDefault();
      $("login-btn").click();
    }
  }

  /**
   * Show error message if log in/sign up fails.
   */
  function showLoginError() {
    $("login-error").innerHTML = "Invalid username or password!";
  }

  /**
   * Clear login error message.
   */
  function clearLoginError() {
    $("login-error").innerHTML = "";
  }

  //-------------------------------------------------------------------------------------------------------
  // Entrance
  // ------------------------------------------------------------------------------------------------------
  init();
})();
