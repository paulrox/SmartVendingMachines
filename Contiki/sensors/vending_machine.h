#define MAX_PRODUCT_AVAILABILITY 5
#define PISA_LATITUDE 43
#define PISA_LONGITUDE 10
#define QTY_DELAY 30
#define ALARM_DELAY 60
#define STATUS_DELAY 120

struct product {
	uint8_t remaining_qty;
	float price;
};

struct coordinate {
	uint32_t latitude;
	uint32_t longitude;
};