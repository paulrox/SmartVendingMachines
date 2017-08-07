
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "rest-engine.h"
#include "vending_machine.h"

/* Necessary to get node_id */
#include "node-id.h"

#define DEBUG 1
#if DEBUG
#define PRINTF(...) printf(__VA_ARGS__)
#define PRINT6ADDR(addr) PRINTF("[%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x]", ((uint8_t *)addr)[0], ((uint8_t *)addr)[1], ((uint8_t *)addr)[2], ((uint8_t *)addr)[3], ((uint8_t *)addr)[4], ((uint8_t *)addr)[5], ((uint8_t *)addr)[6], ((uint8_t *)addr)[7], ((uint8_t *)addr)[8], ((uint8_t *)addr)[9], ((uint8_t *)addr)[10], ((uint8_t *)addr)[11], ((uint8_t *)addr)[12], ((uint8_t *)addr)[13], ((uint8_t *)addr)[14], ((uint8_t *)addr)[15])
#define PRINTLLADDR(lladdr) PRINTF("[%02x:%02x:%02x:%02x:%02x:%02x]",(lladdr)->addr[0], (lladdr)->addr[1], (lladdr)->addr[2], (lladdr)->addr[3],(lladdr)->addr[4], (lladdr)->addr[5])
#else
#define PRINTF(...)
#define PRINT6ADDR(addr)
#define PRINTLLADDR(addr)
#endif

static int machine_id;
static struct product productA, productB;

/* Function to initialize the vending machine
 *
 */

void init_vending_machine()
{
  
  machine_id = node_id;
  
  productA.remaining_qt = MAX_PRODUCT_AVAILABILITY;
  productA.price = 1;

  productB.remaining_qt = MAX_PRODUCT_AVAILABILITY;
  productB.price = 2;
}

void id_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload*/
  char message[50];
  int length = 50;

  sprintf(message, "{'id':'%d','type':'F'}", machine_id);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

void id_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;
  
  printf("Put\n");
  len = REST.get_post_variable(request, "value", &val);
     
  if (len > 0) {
     new_value = atoi(val);
     PRINTF("new value %u\n", new_value);
     machine_id = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

void productAqty_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload*/
  char message[50];
  int length = 50;

  sprintf(message, "{'e':[{'n':'qty','v':'%d'}],'bu':'Pcs'}", 
    productA.remaining_qt);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

void productAprice_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload*/
  char message[50];
  int length = 50;

  sprintf(message, "{'e':[{'n':'price','v':'%d'}],'bu':'Euro'}", 
    productA.price);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

void productAprice_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;
  
  len = REST.get_post_variable(request, "price", &val);
     
  if (len > 0) {
     new_value = atoi(val);
     productA.price = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

void productBqty_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload*/
  char message[50];
  int length = 50;

  sprintf(message, "{'e':[{'n':'qty','v':'%d'}],'bu':'Pcs'}", 
    productB.remaining_qt);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

void productBprice_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload*/
  char message[50];
  int length = 50;

  sprintf(message, "{'e':[{'n':'price','v':'%d'}],'bu':'Euro'}", 
    productB.price);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

void productBprice_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;
  
  len = REST.get_post_variable(request, "price", &val);
     
  if (len > 0) {
     new_value = atoi(val);
     productB.price = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}

RESOURCE(id, "title=\"id\";rt=\"Text\"", 
  id_get_handler, NULL, id_put_handler, NULL);
RESOURCE(ProductAqty, "title=\"ProductAqty\";rt=\"Text\"", 
  productAqty_get_handler, NULL, NULL, NULL);
RESOURCE(ProductAprice, "title=\"ProductAprice\";rt=\"Text\"", 
  productAprice_get_handler, NULL, productAprice_put_handler, NULL);
RESOURCE(ProductBqty, "title=\"ProductBqty\";rt=\"Text\"", 
  productBqty_get_handler, NULL, NULL, NULL);
RESOURCE(ProductBprice, "title=\"ProductAprice\";rt=\"Text\"", 
  productBprice_get_handler, NULL, productBprice_put_handler, NULL);

PROCESS(server, "CoAP Server");
AUTOSTART_PROCESSES(&server);

PROCESS_THREAD(server, ev, data)
{
  PROCESS_BEGIN();

  PROCESS_PAUSE();

  init_vending_machine();
  rest_init_engine();

  rest_activate_resource(&id, "id");
  rest_activate_resource(&ProductAqty, "ProductA/qty");
  rest_activate_resource(&ProductAprice, "ProductA/price");
  rest_activate_resource(&ProductBqty, "ProductB/qty");
  rest_activate_resource(&ProductBprice, "ProductB/price");
  while(1) {
    PROCESS_WAIT_EVENT();
  }
  
  PROCESS_END();
}
