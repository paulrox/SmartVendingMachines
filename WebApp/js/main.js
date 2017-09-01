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
var update_timer = null;

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
    
    /* Wait 5 seconds, then request resources updates every 500 msec */
    setTimeout(function() {
        update_timer = setInterval(requestUpdate, 500);
    }, 5000);
    
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
        /* Disable the update timer */
        if (update_timer != null)
            clearTimeout(update_timer);
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
    /* Disable the update timer */
    if (update_timer != null)
            clearTimeout(update_timer);
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
    var obj = JSON.parse(msg.data);
    var vm_index, prod_index;
    var tmp_vm;
        
    if (obj.type == "OK") {
        /* Get the VM resources */
        for (vm in obj.content) {
            var vm_cnt = obj.content[vm];
            /* Search the VM */
            vm_index = findVM(vm_cnt.id);
            if (vm_index < 0) {
                /* A new VM has been found */
                svm.push(new VendingMachine(vm_cnt.id));
                vm_index = svm.length - 1;
            }
            /* Examine each VM resource */
            for (res in vm_cnt) {
                if (res == "products") {
                    /* Examine each product */
                    for (prod in vm_cnt[res]) {
                        var prod_cnt = vm_cnt[res][prod];
                        prod_index = findProduct(vm_index, prod_cnt.id);
                        if (prod_index < 0) {
                            /* A new product has been found */
                            svm[vm_index].products.push(
                                new Product(prod_cnt.id));
                            prod_index = svm[vm_index].products.length - 1;
                        }
                        /* Examine each resource in each product */
                        for (prod_res in prod_cnt) {
                            switch (prod_res) {
                                case "qty":
                                    svm[vm_index].products[prod_index][prod_res] =
                                    prod_cnt[prod_res];
                                    break;
                                case "price":
                                    svm[vm_index].products[prod_index][prod_res] =
                                    prod_cnt[prod_res].toFixed(2);
                                default:
                                    break;
                            }                
                        }
                    }
                } else {
                    switch (res) {
                        case "tempsens":
                        case "tempdes":
                            svm[vm_index][res] = vm_cnt[res].toFixed(2);
                            break;
                        case "lat":
                        case "lng":
                            svm[vm_index].pos[res] = vm_cnt[res].toFixed(4);
                            break;
                        default:
                            break;        
                    }
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
        showAlert("Exception in WebSocket connection: " + exception, "danger");
    }
}

/**
 * Request a periodic update from the IN
 */
function requestUpdate() {
    socket.send(update_req);
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
    $("#main_cont").append('<div class="row"></div>');
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
    $("#main_cont").append('<div class="row"></div>');
    $("#main_cont").append("<div id=\"city_map\" class=\"map\"></div>");
    
    /* Draw the map */
    cityMap();
}

/**
 * Creates the Analytics page contents.
 */
function createAnalyticsPage() {
    var i = 1;
    current_page = "analytics";
    
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Analytics");
    $(".nav_link").eq(3).attr("class", "nav_link active");
    $("#main_cont").append('<div class="row"></div>');
    for (vm in svm) {
        $("#main_cont").children(".row").last().append(getSVMPanel(vm));
        if (i % 3 == 0) {
            /* Add a row */
            $("#main_cont").append('<div class="row"></div>');
        }
        i++;
    }
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

/**
 * Shows non-invasive alert.
 * @param {STRING} msg Message to be shown
 * @param {STRING} type Type of the alert (optional). It can be one of:
 *                 'success', 'info', 'warning' and 'danger'
 */
function showAlert(msg, type) {
    /* If type is not specified, use 'info' */
    type = type || "info";
    
    $(".main").prepend('<div class="alert alert-' + type + ' alert-' +
                       'dismissible" role="alert"><button type="button"' +
                       'class="close" data-dismiss="alert" aria-label=' +
                       '"Close"><span aria-hidden="true">&times;</span>' +
                       '</button>' + msg + '</div>');
}

/*===========================================================================*/
/*=================== Startup and Cleaning Functions ========================*/
/*===========================================================================*/

/**
 * This code is executed when the document is loaded.
 */
$(document).ready(function(){
    
    createIndexPage();
    
    /* Initialize the svm array */
    svm = Array(0);
    
    $(".close").onclick = function() {
        $().alert('close');
    }
    
    $(".nav_link").not(".navbar-brand").click(function() {
        if (!socket_ok && $(this).text() != "Help") {
            showAlert("The WebSocket is not connected!", "warning");
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