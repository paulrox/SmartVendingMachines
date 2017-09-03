#ifndef _VENDING_MACHINE_H
#define _VENDING_MACHINE_H

#define MAX_PRODUCT_AVAILABILITY 40
#define PISA_LATITUDE 43
#define PISA_LONGITUDE 10
#define QTY_DELAY 5
#define ALARM_DELAY 30
#define STATUS_DELAY 60
#define DIM_BUFFER 64

struct product {
	int remaining_qty;
	float price;
};

struct coordinate {
	float latitude;
	float longitude;
};


#endif