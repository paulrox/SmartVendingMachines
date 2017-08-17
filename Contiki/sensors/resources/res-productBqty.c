/**
 * \file
 *      ProductB quantity Resource
 * \author
 *      Paolo Sassi
 * \author
 *      Matteo Rotundo
 */

#include "contiki.h"
#include <stdlib.h>
#include <string.h>
#include "rest-engine.h"
#include "vending_machine.h"

extern struct product productB;

static void productBqty_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void productBqty_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(productBqty, "title=\"ProductBqty\";rt=\"Text\"", 
  productBqty_get_handler, NULL, productBqty_put_handler, NULL);

static void productBqty_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload */
  char message[50];
  int length = 50;

  sprintf(message, "{'e':[{'n':'qty','v':'%d'}],'bu':'Pcs'}", 
    productB.remaining_qty);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

static void productBqty_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;
  
  len = REST.get_post_variable(request, "qty", &val);
     
  if (len > 0) {
     new_value = atoi(val);
     productB.remaining_qty = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}