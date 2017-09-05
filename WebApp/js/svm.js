/*!
 * Smart Vending Machines - WebApp
 * 
 * Definitions of the SVM objects.
 * 
 * @author Paolo Sassi
 * @author Matteo Rotundo
 */

/* Global Variables */
var svm; /* Array containing all the VMs */
var max_prods = 80; 
var max_prod_qty = 40;

/**
 * Prototype of the status object.
 * @param {INTEGER} value Numeric value of the status
 */
function Status(value) {
    this.value = value;
    this.toStr = function() {
        if (this.value == 0) {
            return "OFF";
        } else {
            return "ON";
        }
    }
}

/**
 * Prototype of the alarm object.
 * @param {INTEGER} value Numeric value of the status
 */
function Alarm(value) {
    this.value = value;
    this.toStr = function() {
        switch (this.value) {
            case "N":
                return "No alarm";
                break;
            case "I":
                return "Intrusion detected";
                break;
            case "F":
                return "Fault detected";
                break;
            default:
                return "Unknown";
                break;
        }
    }
}

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
    this.updated = {qty: false, price: false};
}

/**
 * Prototype of the vending machine object.
 * @param {STRING} id Id and type of the vending machine
 */
function VendingMachine(id) {
    this.id = id;
    this.type = id.substr(4, 5);
    /* Initialize all the properties with default values */
    this.loc = new Position(0.0, 0.0);
    this.products = Array(0);
    this.status = new Status(0);
    this.tempsens = 0.0;
    this.tempdes = 0.0;
    this.alarm = new Alarm("N");
    this.updated = {loc: false, products: false, status: false,
                    tempsens: false, tempdes: false, alarm: false};
    
    this.getPriority = function() {
        var prio = 0;
        
        /* VMs with less products have higher priority */
        prio -= this.getProdsLeft();
        
        /* Alarms give more priority to VMs */
        if (this.alarm.value == "F") {
            prio += 20;
        }
        if (this.alarm.value == "I") {
            prio += 30;
        }
        return prio;
    }
    
    this.getProdsLeft = function() {
        var prods_left = 0;
        for (prod in this.products) {
            prods_left += this.products[prod].qty;
        }
        prods_left = max_prods - prods_left;
        return prods_left;
    } 
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
        if (svm[vm].id == id)
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
    var prods = svm[vm_index].products;
    
    for (prod in prods) {
        if (prods[prod].id == prod_id)
            ret = i;
        i++;
    }
    return ret;
}

function sortVMByPrio(vm_array) {
    vm_array.sort( function(a, b) {return b.getPriority() - a.getPriority(); });
}

function resetVM(vm) {
    vm.alarm.value = "N";
    sendUpdate(vm, "alarm", "N");
    
    for (prod in vm.products) {
        vm.products[prod].qty = max_prod_qty;
        sendUpdate(vm, "qty", max_prod_qty, vm.products[prod]);
    }
}