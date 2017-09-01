/*!
 * Smart Vending Machines - WebApp
 * 
 * Definitions of the SVM objects.
 * 
 * @author Paolo Sassi
 * @author Matteo Rotundo
 */

/* Global Variables */
var svm;
var PISA_LAT = 43;
var PISA_LNG = 10;

/**
 * Prototype of the position object.
 * @param {FLOAT} lat Latitude of the VM
 * @param {FLOAT} lng Longitue of the VM
 */
function Position(lat, lng) {
    this.lat = lat;
    this.lng = lng;
}

/**
 * Prototype of the product object.
 * @param {STRING} id Id of the product
 */
function Product(id) {
    this.id = id;
    
    /* Initialize all the properties with default values */
    this.qty = 0;
    this.price = 0.0;
}
/**
 * Prototype of the vending machine object.
 * @param {STRING} id Id and type of the vending machine
 */
function VendingMachine(id) {
    this.id = id;
    this.type = id.substr(4, 5);
    /* Initialize all the properties with default values */
    this.pos = new Position(0.0, 0.0);
    this.products = Array(0);
    this.status = 0;
    this.tempsens = 0.0;
    this.tempdes = 0.0
    this.alarm = "N";
    
}


/**
 * Check if a given VM is already present in the svm array.
 * @param {STRING} id Id of the VM we are looking for
 * @returns {INTEGER} The VM index if it is present, -1 otherwise
 */
function findVM(id) {
    var ret = -1;
    var i = 0;
    
    for (vm in svm) {
        if (svm.id == id)
            ret = i;
        i++;
    }  
    return ret;
}


/**
 * Check if a given product is already present in the VM.
 * @param {STRING} vm_index Index of the VM in the svm array
 * @param {STRING} prod_id Id of the product we are looking for
 * @returns {INTEGER} The product index if it is present, -1 otherwise
 */
function findProduct(vm_index, prod_id) {
    var ret = -1;
    var i = 0;
    
    for (prod in svm[vm_index].products) {
        if (prod.id == prod_id)
            ret = i;
        i++;
    }
    return ret;
}

function printSVM() {
    var ret = "";
    
    for (vm in svm) {
        var vm_cnt = svm[vm]; 
        ret = ret + "********* " + vm_cnt.id + " *********<br>" + "Status: " + 
            vm_cnt.status +"<br>" + "Position: " + vm_cnt.pos + "<br>" +
            "Sensed Temp: " + vm_cnt.tempsens + "<br>" + "Desidered Temp: " +
            vm_cnt.tempdes + "<br>" + "Alarm: " + vm_cnt.alarm + "<br>" + 
            "**************************<br>";
    }
    return ret;
}

/**
 * Get the HTML code to show the selected VM (and its products) in a panel.
 * @param {STRING} index Index of the VM on the svm array
 * @returns {STRING} The HTML code to display the panel
 */
function getSVMPanel(index) {
    var ret = "";
    var prods = svm[index].products;
    var offset = (index % 2 == 0)? "col-md-offset-1" : "col-md-offset-2";
    
    ret = ret + '<div class="panel panel-primary svm-panel col-md-4 ' + offset +
        '"><div class="panel-heading"><h3 class="panel-title">' + svm[index].id +
        '</h3></div><div class="panel-body"><h4>General Info</h4>' + "<strong>Status: </strong>" + svm[index].status +
        "<br><strong>Position: </strong>" + svm[index].pos.lat + ", " + svm[index].pos.lng +
        "<br>" + "<strong>Sensed Temp.: </strong>" + svm[index].tempsens + "<br>" +
        "<strong>Desidered Temp.: </strong>" + svm[index].tempdes + "<br>" + "<strong>Alarm: </strong>" +
        svm[index].alarm + "<br><h4>Products</h4>";
    for (prod in prods) {
        ret = ret + '<div class="panel panel-default"><div class="panel-' +
            'heading"><strong>' + prods[prod].id + '</strong></div><div class="panel-body">' +
            "<strong>Quantity: </strong>" +prods[prod].qty + "<br><strong>Price: </strong>" +
            prods[prod].price + '</div></div>';
    }
    ret = ret + '</div></div>';
    return ret;
}