/*!
 * Smart Vending Machines - WebApp
 * 
 * Javascript file containing the map functions.
 * 
 * Authors: Paolo Sassi
 *          Matteo Rotundo
 */

var map = null;
var route_planned = false;
var last_content;
var directionsDisplay = new google.maps.DirectionsRenderer();
var directionsService = new google.maps.DirectionsService();

/**
 * Draws the city map.
 */
function cityMap() {
    var mapProp = {
        center: new google.maps.LatLng(43.720448, 10.392116),
        zoom: 14,
        disableDefaultUI: true
    };
    map = new google.maps.Map(document.getElementById("city_map"),
                                  mapProp);
    route_planned = false;
    last_content = Array(svm.length)
    addVmMarkers();
}

/**
 * Finds the Address of the VM.
 * @param {VengingMachine} vm The Vending Machine
 */
function findAddress(vm) {
    var geocoder = new google.maps.Geocoder;
    geocoder.geocode({'location': {lat: parseFloat(vm.loc.lat),
                                   lng: parseFloat(vm.loc.lng)}},
                     function(results, status) {
        if (status === 'OK') {    
            if (results[0]) {
                vm.address = results[0].formatted_address;
            } else {
                showAlert('Geocoder: No results found', "warning");
            }
        } else {
            showAlert('Geocoder failed due to: ' + status, "danger");
        }
    })
}

/**
 * Adds a VM marker on the map
 */
function addVmMarkers() {
    for (vm in svm) {
        var latLng = new google.maps.LatLng(svm[vm].loc.lat, svm[vm].loc.lng);
        var marker = new google.maps.Marker({
            position: latLng,
            label: svm[vm].id.substr(4, svm[vm].id.length-1),
            map: map,
            /* Additional property needed to map a specific VM to
             * a specific marker */
            vm: vm
        });
        /* Prepare the InfoWindow box */
        var infowindow = new google.maps.InfoWindow();
        google.maps.event.addListener(marker, 'click', function() {
            if (!route_planned)
                content = createIwContent(this.vm);
            else
                content = last_content[this.vm];
            infowindow.setContent(content);
            infowindow.open(map, this);
        });
    }
}

/**
 * Creates the content of the info window.
 */
function createIwContent(index) {
    var ret = "";
    var prods = svm[index].products;
    
    ret = '<h4>' + svm[index].id + '<h5>General Info</h5></h4>' +
        "<strong>Status: </strong>" +
        svm[index].status.toStr() + "<br><strong>Address: </strong>" +
        svm[index].address + "<br><strong>Sensed Temp.: </strong>" +
        svm[index].tempsens + "<br>" + "<strong>Desidered Temp.: </strong>" +
        svm[index].tempdes + "<br>" + "<strong>Alarm: </strong>" +
        svm[index].alarm.toStr() + '<br><strong>Products Left: </strong>' +
        svm[index].getProdsLeft() + '<br><strong>Priority: </strong>' +
        svm[index].getPriority();
    return ret;
}

/**
 * Shows the route for visiting the VMs
 */
function showRoute() {
    var num_visit = $(this).prev().prev().val();
    if (num_visit <= 1 || num_visit > svm.length) {
        showAlert("Invalid number of visits!", "danger");
        return;
    }
    var num_waypoints = num_visit - 2;
    var waypts = [];
    var dirRendOpt = {
        //suppressMarkers: true
    };
    
    /* Clear the text div */
    $("#route-text").empty();
    
    /* Update the information window content */
    for (vm in svm) {
        last_content[vm] = createIwContent(vm); 
    }
    
    /* Sort the VMs by descending priorities */
    sortVMByPrio(svm);
    
    directionsDisplay.setMap(map);
    directionsDisplay.setOptions(dirRendOpt);
    directionsDisplay.setPanel(document.getElementById("route-text"));
    
    /* Using the exact positon */
    var start = new google.maps.LatLng(svm[0].loc.lat, svm[0].loc.lng);
    var end = new google.maps.LatLng(svm[num_visit - 1].loc.lat, 
        svm[num_visit - 1].loc.lng);
    
    /* Using the address */
    //var start = svm[0].address;
    //var end = svm[num_visit - 1].address;
    
    /* Add waypoints, if any */
    for (var i = 0; i < num_waypoints; i++) {
        waypts.push({
            location: svm[i + 1].address,
            stopover: true
        });
    }
    
    /* Get the checkbox value */
    var opt = $('#opt-route').is(':checked')? true : false;
    
    var request = {
        origin: start,
        destination: end,
        optimizeWaypoints: opt,
        waypoints: waypts,
        travelMode: 'DRIVING'
    };
    /* Compute the path */
    
    directionsService.route(request, function(result, status) {
    if (status == 'OK') {
        route_planned = true;
        directionsDisplay.setDirections(result);
        /* Reset the VMs */
        for (var i = 0; i < num_visit; i++) {
            resetVM(svm[i]);
        }
    } else {
        showAlert("Directions request failed due to: " + status, "danger");
    }
  });
}
