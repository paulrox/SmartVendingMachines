/*!
 * Smart Vending Machines - WebApp
 * 
 * Main Javascript file.
 * 
 * @author Paolo Sassi
 * @author Matteo Rotundo
 */



/** Global variables */
var socket;
var socket_ok = false;
var current_page;
var update_req = JSON.stringify({"type": "R"});

/*===========================================================================*/
/*======================== WebSocket Functions ==============================*/
/*===========================================================================*/

/**
 * WebSocket onopen event handler.
 */
function onOpenHandler() {
    socket_ok = true;
    $(".ws_status").children().empty();
    $(".ws_status").children().first().attr("id", "ws_online");
    $(".ws_status").children().append("ONLINE");
    
    /* Send the initial request to the server */
    socket.send(update_req);
}

/**
 * WebSocket onerror event handler.
 */
function onErrorHandler() {
    if (!socket_ok) {
        $(".ws_status").children().empty();
        $(".ws_status").find("span").attr("id", "ws_offline");
        $(".ws_status").children().append("OFFLINE");
    } else {
        socket_ok = false;
        /* If the user is visiting a page which depends on the WebSocket,
        * bring it back to the index page. */
        createIndexPage();
    }
}

/**
 * WebSocket onclose event handler.
 */
function onCloseHandler() {
    socket_ok = false;
    $(".ws_status").children().empty();
    $(".ws_status").find("span").attr("id", "ws_offline");
    $(".ws_status").children().append("OFFLINE");
    
    /* If the user is visiting a page which depends on the WebSocket,
     * bring it back to the index page. */
    createIndexPage();
}

/**
 * WebSocket onmessage event handler.
 * @param {STRING} msg Received message
 */
function onMessageHandler(msg) {
    alert(msg.data);
    var mydata = JSON.stringify(msg.data);
    var obj = JSON.parse(mydata);
    var vm_index, prod_index;
    var tmp_vm;
    
    
    if (obj.type == "OK") {
        /* Get the VM resources */
        for (vm in obj.content) {
            /* Examine each VM resource */
            for (res in vm) {
                if (res == "id") {
                    vm_index = findVM(vm.id);
                    if (vm_index < 0) {
                    /* A new VM has been found */
                    svm.push(new VendingMachine(vm.id));
                    vm_index = svm.length - 1;
                    }
                } else if (res == "products") {
                    /* Examine each product */
                    for (prod in vm[res]) {
                        /* Examine each resource in each product */
                        for (prod_res in prod) {
                            if (prod_res == "id") {
                                prod_index = findProduct(vm.id, prod.id);
                                if (prod_index < 0) {
                                    /* A new product has been found */
                                    svm[vm_index].products.push(
                                        new Product(prod.id));
                                    prod_index = svm[vm_index].products.length
                                        - 1;
                                }
                            } else {
                                svm[vm_index].products[prod_index].prod_res = 
                                    prod[prod_res];
                            }
                        }
                    }
                } else {
                    svm[vm_index].res = vm[res];
                }
            }
        }
    }
}

/**
 * Connect to the WebSocket Server (if possible).
 */
function connectWS() {
    var host;
    if (!("WebSocket" in window)) {
        alert("Your browser doesn't support WebSockets, please " +
                          "use Google Chrome or Mozilla Firefox");
        return;
    }
    
    try {
        
        host = "ws://127.0.0.1:8000/";
        socket = new WebSocket(host);
        
        socket.onopen = onOpenHandler;
        
        socket.onerror = onErrorHandler;
        
        socket.onclose = onCloseHandler;
        
        socket.onmessage = onMessageHandler;
        
    } catch (exception) {
        alert("Exception in WebSocket connection: " + exception);
    }
}

/*===========================================================================*/
/*====================== Page Creation Functions ============================*/
/*===========================================================================*/

/**
 * Creates the index page contents.
 */
function createIndexPage() {
    current_page = "index";
    
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    
    
    /* Add the new content */
    $(".page-header").append("Overview");
    $("#main_cont").append("<img id=\"ov_img\" src=\"img/overview.jpg\"" +
                           "alt=\"overview image\" class=\"img-rounded " +
                           "center-block\">");
    $("#main_cont").append("<p> This page is the user frontend of the Web " +
                           " app for the Internet of Things project.</p>");
    $("#main_cont").append("<p> The system administrator can use this page " +
                           "to manage remotely the vending machines and " +
                           "to plan technical staff routes. In particular:" + 
                           "<ul> <li>The \"<strong>City Map</strong>\" page " +
                           "shows machines position on the city center map;" +
                           "</li> <li>The \"<strong>Analytics</strong>\" " +
                           "page is used to monitor the actual machines " +
                           "status and the daily income;</li> <li>The \"" +
                           "<strong>Plan Route</strong>\" page is used to " +
                           "generate the supplies and technical interventions" +
                           " routes;</li></ul></p>");
}


/**
 * Creates the City Map page contents.
 */
function createMapPage() {
    current_page = "map";
    
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    
    /* Add the new content */
    $(".page-header").append("City Map");
    $(".nav_link").eq(2).attr("class", "nav_link active");
    $("#main_cont").append("<div id=\"city_map\" class=\"map\"></div>");
    
    /* Draw the map */
    cityMap();
}

/**
 * Creates the Analytics page contents.
 */
function createAnalyticsPage() {
    current_page = "analytics";
    
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Analytics");
    $(".nav_link").eq(3).attr("class", "nav_link active");
    $("#main_cont").append("<p>" + printSVM() + "</p>");
}

/**
 * Creates the Plan Route page contents.
 */
function createRoutePage() {
    current_page = "route";
    
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Plan Route");
    $(".nav_link").eq(4).attr("class", "nav_link active");
}

/**
 * Creates the Help page contents.
 */
function createHelpPage() {
    current_page = "help";
    
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Help");
}

/*===========================================================================*/
/*=================== Startup and Cleaning Functions ========================*/
/*===========================================================================*/

/**
 * This code is executed when the document is loaded.
 */
$(document).ready(function(){
    
    createIndexPage();
    
    $(".nav_link").not(".navbar-brand").click(function() {
        if (!socket_ok && $(this).text() != "Help") {
            alert("The WebSocket is not connected!");
            return;
        }
        
        switch($(this).text()) {
            case "City Map":
                createMapPage();
                break;
            case "Analytics":
                createAnalyticsPage();
                break;
            case "Plan Route":
                createRoutePage();
                break;
            case "Help":
                createHelpPage();
            default:
                break;
        }  
    })
    connectWS();
});

/**
 * Closes the WebSocket before closing the window.
 */
$(window).on('beforeunload', function(){
    if (socket.readyState === WebSocket.OPEN)
        socket.close();
});