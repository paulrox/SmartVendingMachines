#define MAX_PRODUCT_AVAILABILITY 5
#define PISA_LATITUDE 43
#define PISA_LONGITUDE 10

struct product {
	uint8_t remaining_qty;
	uint8_t price;
};

struct coordinate {
	uint32_t latitude;
	uint32_t longitude;
};