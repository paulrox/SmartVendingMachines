function cityMap() {
    var mapProp = {
        center: new google.maps.LatLng(43.720448, 10.392116),
        zoom: 16,
        disableDefaultUI: true,
    };
    var map = new google.maps.Map(document.getElementById("city_map"),
                                  mapProp);
}

$(document).ready(function(){
    cityMap();
});

