/*!
 * Smart Vending Machines - WebApp
 * 
 * Main Javascript file.
 * 
 * @author Paolo Sassi
 * @author Matteo Rotundo
 */



/** Global variables */
var page = "index";
var socket;
var socket_ok = false;
var current_page;
var update_req = JSON.stringify({"type": "R"});
var update_timer = null;
var page_timer = null;

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
            case "Manage":
                createManagePage();
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

/*===========================================================================*/
/*======================== WebSocket Functions ==============================*/
/*===========================================================================*/

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
                        if (prod_cnt.id != undefined) {
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
                                        svm[vm_index].products[prod_index]
                                            [prod_res] = prod_cnt[prod_res];
                                        break;
                                    case "price":
                                        svm[vm_index].products[prod_index]
                                            [prod_res] = 
                                            prod_cnt[prod_res].toFixed(2);
                                    default:
                                        break;
                                }
                                svm[vm_index].updated.products = true;
                                svm[vm_index].products[prod_index].
                                    updated[prod_res] = true;
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
                            svm[vm_index].loc[res] = vm_cnt[res].toFixed(4);
                            if (svm[vm_index].loc.lat != 0.0 &&
                                svm[vm_index].loc.lng != 0.0) {
                                /* Fire the findAddress function after
                                 * a random time ([0, 2.5] seconds) in
                                 * order to avoid Google Maps API
                                 * limitation */
                                var timeout = 500 + Math.random()*2500;
                                setTimeout(findAddress,
                                           timeout, svm[vm_index]);
                            }
                            break;
                        case "alarm":
                        case "status":
                            svm[vm_index][res].value = vm_cnt[res];
                        default:
                            break;        
                    }
                    svm[vm_index].updated[res] = true;
                }
            }
        }
    }
}

/**
 * Request a periodic update from the IN
 */
function requestUpdate() {
    socket.send(update_req);
}

/**
 * Send an updated resource to the IN
 *
 */
function sendUpdate(vm, res, value, prod) {
    var msg;
    prod = prod || "NO_PROD"; /* Default value */
    
    if (prod == "NO_PROD") {
        if (res == "loc") {
            msg = {"type": "W", "id": vm.id, "resource": res,
               "content": value};
        } else {
            /* Update a VM resource */
            msg = {"type": "W", "id": vm.id, "resource": res,
               "content": {}};
            msg.content[res] = value;
        }
    } else {
        /* Update a product resource */
        msg = {"type": "W", "id": vm.id, "resource":
               prod.id + res, "content": {}};
        msg.content[res] = value;
    }
    socket.send(JSON.stringify(msg));
}

/*===========================================================================*/
/*====================== Page Creation Functions ============================*/
/*===========================================================================*/

/**
 * Creates the index page contents.
 */
function createIndexPage() {
    current_page = "index";
    
    clearInterval(page_timer);
    page_timer = null;
    
    
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    
    
    /* Add the new content */
    $(".page-header").append("Overview");
    $("#main_cont").append("<div class=\"row\"><img id=\"ov_img\" src=\"img/overview.jpg\"" +
                           "alt=\"overview image\" class=\"img-rounded " +
                           "center-block\"></div>" +
                           '<div id="index-text" class="row"><p> This page is the user frontend of the Web' +
                           " app for the Internet of Things project." +
                           "The system administrator can use this page " +
                           "to manage remotely the vending machines and " +
                           "to plan technical staff routes. In particular:" + 
                           "<ul> <li>The \"<strong>City Map</strong>\" page " +
                           "shows machines locations on the city center map;" +
                           "</li> <li>The \"<strong>Manage</strong>\" " +
                           "page is used to monitor the actual machines " +
                           "status and the daily income;</li></ul></p></div>");
}


/**
 * Creates the City Map page contents.
 */
function createMapPage() {
    current_page = "map";
    
    clearInterval(page_timer);
    page_timer = null;

    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("City Map");
    $(".nav_link").eq(2).attr("class", "nav_link active");
    $("#main_cont").append('<div class="row"><div id="city_map" class="map">' +
                           '</div></div><div id="plan-row" class="row"><div>' +
                           '<span>Number of VMs to visit: </span>' +
                           '<input type="text"></input><button type="button" class="' +
                           'btn btn-primary">Plan Route</button></div></div>' +
                           '<div class="row"><div id="route-text"</div></div>');
    $("#main_cont").find(".btn").first().click(showRoute);
    
    /* Draw the map */
    cityMap();
}

/*===========================================================================*/
/*========================== Management Functions ===========================*/
/*===========================================================================*/

/**
 * Creates the Manage page contents.
 */
function createManagePage() {
    var i = 1;
    current_page = "Manage";
    
    /* Start the page refresh timer */
    if (page_timer == null)
        page_timer = setInterval(checkUpdates, 500);
        
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Manage");
    $(".nav_link").eq(3).attr("class", "nav_link active");
    $("#main_cont").append('<div class="row"></div>');
    for (vm in svm) {
        $("#main_cont").children(".row").last().append(getSVMPanel(vm));
        if (i % 2 == 0) {
            /* Add a row */
            $("#main_cont").append('<div class="row"></div>');
        }
        i++;
    }
    
    $(".svm-edit-res").mouseenter(showEditIcon);
    $(".svm-edit-res").mouseleave(hideEditIcon);
}

/**
 * Checks if any resource value has changed
 */
function checkUpdates() {
    var is_updated = false;
    var value;
    
    for (vm in svm) {
        for (res in svm[vm].updated) {
            if (svm[vm].updated[res]) {
                /* This resource has been modified and its displayed
                 * value must be updated. */
                if (res == "products") {
                    /* Check which products have been modified */
                    for (prod in svm[vm].products) {
                        for (prod_res in svm[vm].products[prod].updated) {
                            is_updated = svm[vm].products[prod].updated[prod_res];
                            if (is_updated && prod_res != "id") {
                                svm[vm].products[prod].updated[prod_res] = false;
                                changeDispValue(svm[vm], prod_res,
                                                svm[vm].products[prod][prod_res],
                                                svm[vm].products[prod]);
                            }
                        }
                    }
                } else {
                    /* Check which VM resource has been modified */
                    switch (res) {
                        case "status":
                        case "alarm":
                            value = svm[vm][res].toStr();
                            break;
                        case "loc":
                            value = svm[vm][res].lat + ", " + svm[vm][res].lng;
                            break;
                        default:
                            value = svm[vm][res];
                            break;
                    }
                    svm[vm].updated[res] = false;
                    changeDispValue(svm[vm], res, value);
                }
            }
        }
        /* All products have been updated */
        svm[vm].updated.products = false;
    }
}

function changeDispValue(vm, res, value, prod) {
    var id;
    
    prod = prod || "NO_PROD"; /* default value */
    
    if (prod != "NO_PROD") {
        id = "#" + vm.id.toLowerCase() + "-" + prod.id + "-" + res;
    } else {
        id = "#" + vm.id.toLowerCase() + "-" + res;
    }
    $(id).empty();
    $(id).text(value);
}

/**
 * Get the HTML code to show the selected VM (and its products) in a panel.
 * @param {STRING} index Index of the VM on the svm array
 * @returns {STRING} The HTML code to display the panel
 */
function getSVMPanel(index) {
    var ret = "";
    var event = "onmouseover";
    var prods = svm[index].products;
    var offset = (index % 2 == 0)? "col-md-offset-1" : "col-md-offset-2";
    
    ret = ret + '<div class="panel panel-primary svm-panel col-md-4 ' + offset +
        '"><div class="panel-heading"><h3 class="panel-title">' + svm[index].id +
        '</h3></div><div class="panel-body"><h4>General Info</h4>' +
        addVMRes(svm[index], "status") + addVMRes(svm[index], "loc") +
        addVMRes(svm[index], "tempsens") + addVMRes(svm[index], "tempdes") +
        addVMRes(svm[index], "alarm") + "<br><h4>Products</h4>";
    for (prod in prods) {
        ret = ret + '<div class="panel panel-default"><div class="panel-' +
            'heading"><strong>' + prods[prod].id + '</strong></div><div class="panel-body">' +
            addProdRes(svm[index], prods[prod], "qty") +
            addProdRes(svm[index], prods[prod], "price") + '</div></div>';
    }
    ret = ret + '</div></div>';
    return ret;
}

/**
 * Adds the HTML code needed for displaying a resource of a specific VM.
 * @param {VendingMachine} vm The VM who owns the resources
 * @param {STRING} res The name of the resource
 * @returns {STRING} The HTML code
 */
function addVMRes(vm, res) {
    var ret = "";
    var value;
    
    switch (res) {
        case "status":
            ret += '<strong>Status: </strong><span class="svm-edit-res" id="';
            value = vm.status.toStr();
            break;
        case "loc":
            ret += '<strong>Location: </strong><span class="svm-edit-res" id="';
            value = vm.loc.lat + ", " + vm.loc.lng;
            break;
        case "tempsens":
            ret += '<strong>Sensed Temp.: </strong><span id="';
            value = vm.tempsens;
            break;
        case "tempdes":
            ret += '<strong>Desidered Temp.: </strong><span ' +
                'class="svm-edit-res" id="';
            value = vm.tempdes;
            break;
        case "alarm":
            ret += '<strong>Alarm: </strong><span class="svm-edit-res" id="';
            value = vm.alarm.toStr();
            break;
        default:
            return ret;
            break;
    }  
    ret += vm.id.toLowerCase() + "-" + res + '">' + value + '</span><br>';
    return ret;
}

/**
 * Adds the HTML code needed for displaying a resource of a specific product.
 * @param {VendingMachine} vm The VM who owns the resources
 * @param {STRING} res The name of the resource
 * @returns {STRING} The HTML code
 */
function addProdRes(vm, prod, res) {
    var ret = "";
    var value;
    
    switch (res) {
        case "qty":
            ret += '<strong>Quantity: </strong><span class="svm-edit-res" id="';
            break;
        case "price":
            ret += '<strong>Price: </strong><span class="svm-edit-res" id="';
            break;
        default:
            return ret;
            break;
    }
    value = prod[res];
    ret += vm.id.toLowerCase() + "-" + prod.id + "-" + res + '">' + value +
        '</span><br>';
    return ret;
}

/**
 * Shows the edit icon next to the text.
 */
function showEditIcon() {
    $(this).append('<span class="glyphicon glyphicon-wrench edit"' +
                   'aria-hidden="true"></span>');
    $(this).children().last().click(editRes);
}

/**
 * Hides the edit icon next to the text.
 */
function hideEditIcon() {
    $(this).children().last().remove();
}

/**
 * Edits the selected resource.
 * @param {OBJECT} obj The DOM object who fired the event.
 */
function editRes() {
    var id = $(this).parent().attr("id");
    var type = id.split("-");
    type = type[type.length - 1];
    /* Store and remove the resource value */
    var value = $(this).parent().text();
    $(this).parent().text("");
    /* Add a form specific for the res type */
    switch (type) {
        case "status":
            $("#" + id).append('<select><option value=0>OFF</option>' +
                               '<option value=1>ON</option></select>');
            break;
        case "alarm":
            $("#" + id).append('<select><option value="N">No alarm</option>' +
                               '<option value="I">Intrusion</option>' +
                               '<option value="F">Fault</option></select>');
            break;
        case "loc":
             $("#" + id).append('<input type="text" value="' + value + '">' +
                               '</input>');
            break;
        case "tempdes":
        case "qty":
        case "price":
            $("#" + id).append('<input class="short-input" type="text"' +
                               'value="' + value + '"></input>');
            break;
        default:
            return;
    }
    
    $("#" + id).append('<span class="glyphicon glyphicon-ok edit"' +
                   'aria-hidden="true"></span>');
    $("#" + id).children().last().click(confirmEdit);
    $("#" + id).append('<span class="glyphicon glyphicon-remove edit"' +
                   'aria-hidden="true"></span>');
    $("#" + id).children().last().click(cancelEdit);
    $("#" + id).unbind("mouseenter", showEditIcon);
    $("#" + id).unbind("mouseleave", hideEditIcon);
}

/**
 * Confirms the edit of a value.
 */
function confirmEdit() {
    var id = $(this).parent().attr("id");
    var type = id.split("-");
    var index = findVM(id.split("-")[0].toUpperCase());
    var value =  $("#" + id).children().first().val();
    var disp_value;
    if (type.length > 2) {
        /* A product resource has been modified */
        var prod = findProduct(index, type[1]);
        type = type[2];
        /* Update the inner structure */
        svm[index].products[prod][type] = value;
        svm[index].products[prod].updated[type] = true;
        disp_value = value;
        /* Send the update message */
        sendUpdate(svm[index], type, value, svm[index].products[prod]);
    } else {
        /* A VM resource has been modified */
        type = type[1];
        /* Update the inner structure */
        switch (type) {
            case "loc":
                var lat = value.split(", ")[0];
                var lng = value.split(", ")[1];
                svm[index].loc = new Position(lat, lng);
                findAddress(svm[index]);
                value = {lat: lat, lng: lng};
                disp_value = lat + ", " + lng;
                break;
            case "status":
                svm[index][type] = new Status(value);
                disp_value = svm[index][type].toStr();
                break;
            case "alarm":
                svm[index][type] = new Alarm(value);
                disp_value = svm[index][type].toStr();
                break;
            default:
                svm[index][type] = value;
                disp_value = value;
                break;
        }
        svm[index].updated[type] = true;
        /* Send the update message */
        sendUpdate(svm[index], type, value);   
    }
    /* Update the displayed value */
    $("#" + id).empty();
    $("#" + id).text(disp_value);
    $("#" + id).mouseenter(showEditIcon);
    $("#" + id).mouseleave(hideEditIcon);
}

/**
 * Cancels the edit of a value
 */
function cancelEdit() {
    var id = $(this).parent().attr("id");
    var type = id.split("-");
    var index = findVM(id.split("-")[0].toUpperCase());
    var value;
    
    if (type.length > 2) {
        /* A product resource has been modified */
        var prod = findProduct(index, type[1]);
        type = type[2];
    } else {
        /* A VM resource has been modified */
        type = type[1];
    }
    
    switch (type) {
        case "status":
        case "alarm":
            value = svm[index][type].toStr();
            break;
        case "loc":
            value = svm[index][type].lat + ", " + svm[index][type].lng;
            break;
        case "qty":
        case "price":
            value = svm[index].products[prod][type];
            break;
        default:
            value = svm[index][type];
            break;
    }
    $("#" + id).empty();
    $("#" + id).text(value);
    $("#" + id).mouseenter(showEditIcon);
    $("#" + id).mouseleave(hideEditIcon);
}

/*===========================================================================*/
/*============================ Utility Functions ============================*/
/*===========================================================================*/

/**
 * Shows a non-invasive alert.
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