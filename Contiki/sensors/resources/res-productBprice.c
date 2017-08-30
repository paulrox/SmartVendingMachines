/**
 * \file
 *      ProductB price Resource
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

static void productBprice_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void productBprice_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(productBprice, "title=\"ProductAprice\";rt=\"Text\"", 
  productBprice_get_handler, NULL, productBprice_put_handler, NULL);

static void productBprice_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload */
  char message[50];
  int length = 50;
  float tmp;

  tmp = (float)((float)productB.price - (int)productB.price);
  tmp = tmp * 100;
  sprintf(message, "{'price':'%d.%d'}", 
    (int)productB.price, (int) tmp);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

float stof(const char* s){
  float rez = 0, fact = 1;
  int point_seen = 0;
  if (*s == '-'){
    s++;
    fact = -1;
  };
  for (point_seen = 0; *s; s++){
    if (*s == '.'){
      point_seen = 1; 
      continue;
    };
    int d = *s - '0';
    if (d >= 0 && d <= 9){
      if (point_seen) fact /= 10.00f;
      rez = rez * 10.00f + (float)d;
    };
  };
  return rez * fact;
}

static void productBprice_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int len;
  const char *val = NULL;
  float new_value;

  len = REST.get_post_variable(request, "value", &val);
     
  if (len > 0) {
     new_value = stof(val);
     productB.price = new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}