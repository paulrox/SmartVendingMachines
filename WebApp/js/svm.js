/*!
 * Smart Vending Machines - WebApp
 * 
 * Definitions of the SVM objects.
 * 
 * @author Paolo Sassi
 * @author Matteo Rotundo
 */

/* Global Variables */
var svm = Array(0);
var PISA_LAT = 43;
var PISA_LNG = 10;

/**
 * Prototype of the product object.
 * @param {STRING} id Id of the product
 */
function Product(id) {
    this.id = id;
    
    /* Initialize all the properties with default values */
    this.qty = 0;
    this.price = 0;
}
/**
 * Prototype of the vending machine object.
 * @param {STRING} id Id and type of the vending machine
 */
function VendingMachine(id) {
    this.id = id;
    
    /**
     * Set the geographical position of the VM.
     * @param {STRING} lat Latitude of the VM
     * @param {STRING} lng Longitude of the VM
     * @returns {ARRAY} Array containing the latitude and longitude as
     *                  floats.
     */
    this.setPosition = function(lat, lng) {
        var lat_num = PISA_LAT + "." + lat;
        var lng_num = PISA_LNG + "." + lng;
        return [parseFloat(lat_num), parseFloat(lng_num)];
    }
    /* Initialize all the properties with default values */
    this.pos = this.setPosition("0", "0");
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
 * @param {STRING} vm_id Id of the VM
 * @param {STRING} prod_id Id of the product we are looking for
 * @returns {INTEGER} The product index if it is present, -1 otherwise
 */
function findProduct(vm_id, prod_id) {
    var ret = -1;
    var i = 0;
    
    for (prod in svm[vm_id].products) {
        if (prod.id == prod_id)
            ret = i;
        i++;
    }
    return ret;
}

function printSVM() {
    var ret = "";
    
    for (vm in svm) {
        ret = ret + " " + vm.id + " " + vm.status + " " + vm.pos + " " +
            vm.tempsens + " " + vm.tempdes + " " + vm.alarm + "<br>";
    }
    return ret;
}