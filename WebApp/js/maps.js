/*!
 * Smart Vending Machines - WebApp
 * 
 * Javascript file containing the map functions.
 * 
 * Authors: Paolo Sassi
 *          Matteo Rotundo
 */

var map = null;

/**
 * Draws the city map.
 */
function cityMap() {
    var mapProp = {
        center: new google.maps.LatLng(43.720448, 10.392116),
        zoom: 16,
        disableDefaultUI: true,
    };
    map = new google.maps.Map(document.getElementById("city_map"),
                                  mapProp);
    
    addVmMarkers();
}

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
            content = createIwContent(this.vm);
            infowindow.setContent(content);
            infowindow.open(map, this);
        });
    }
}

function createIwContent(index) {
    var ret = "";
    var prods = svm[index].products;
    
    ret = '<h4>General Info</h4>' + "<strong>Status: </strong>" +
        svm[index].status.toStr() + "<br><strong>Address: </strong>" +
        svm[index].address + "<br><strong>Sensed Temp.: </strong>" +
        svm[index].tempsens + "<br>" + "<strong>Desidered Temp.: </strong>" +
        svm[index].tempdes + "<br>" + "<strong>Alarm: </strong>" +
        svm[index].alarm.toStr();
    return ret;
}
