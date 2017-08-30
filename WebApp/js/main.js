/*!
 * Smart Vending Machines - WebApp
 * 
 * Main Javascript file.
 * 
 * Authors: Paolo Sassi
 *          Matteo Rotundo
 */



/** Global variables */
var socket_ok = false;


/* Functions to be executed when the document has been loaded */


/**
 * Starts the WebSocket (if possible).
 */
function startWS() {
    var socket, host;
    if (!("WebSocket" in window)) {
        alert("Your browser doesn't support WebSockets, please " +
                          "use Google Chrome or Mozilla Firefox");
        return;
    }
    
    try {
        host = "ws://127.0.0.1:8000/";
        socket = new WebSocket(host);
        
        $(".ws_status").children().empty();
        
        socket.onopen = function() {
            socket_ok = true;    
        }
    } catch (exception) {}
    
    if (socket_ok) {
        $(".ws_status").children().first().attr("id", "ws_online");
        $(".ws_status").children().append("ONLINE");
    } else {
        $(".ws_status").find("span").attr("id", "ws_offline");
        $(".ws_status").children().append("OFFLINE");
    }
}


/**
 * Creates the index page contents.
 */
function createIndexPage() {
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
 * Creates the map page contents.
 */
function createMapPage() {
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    
    /* Add the new content */
    $(".page-header").append("City Map");
    $(".nav_link").eq(2).attr("class", "nav_link active");
    $("#main_cont").append("<div id=\"city_map\" class=\"map\"></div>");
    
    cityMap();
}

function createAnalyticsPage() {
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Analytics");
    $(".nav_link").eq(3).attr("class", "nav_link active");
}

function createRoutePage() {
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Plan Route");
    $(".nav_link").eq(4).attr("class", "nav_link active");
}

function createHelpPage() {
    /* Empty the old content */
    $("#main_cont").empty();
    $(".page-header").empty();
    $(".nav_link").attr("class", "nav_link");
    $(".nav_link").first().attr("class", "navbar-brand nav_link");
    
    /* Add the new content */
    $(".page-header").append("Help");
}

/**
 * This code is executed when the document is loaded.
 */
$(function(){
    
    createIndexPage();
    
    $(".nav_link").not(".navbar-brand").click(function() {
        if (!socket_ok && $(this).text() != "Help") {
            //$(this).attr("href", "");
            alert("The WebSocket is not started yet!");
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
    
    startWS();
});
