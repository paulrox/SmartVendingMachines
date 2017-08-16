/**
 * \file
 *      Status Resource
 * \author
 *      Paolo Sassi
 * \author
 *      Matteo Rotundo
 */

#include "contiki.h"
#include <stdlib.h>
#include <string.h>
#include "rest-engine.h"
#include "node-id.h"
#include "vending_machine.h"

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

extern int node_lat;
extern int node_long;

static void loc_get_handler(void *request, void *response, uint8_t *buffer,
                           uint16_t preferred_size, int32_t *offset);

RESOURCE(loc, "title=\"loc\";rt=\"Text\"", loc_get_handler, NULL, NULL,
         NULL);

static void loc_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload*/
  char message[50];
  int length = 50;

  sprintf(message, "{'lat':'%u.%u','long':'%u.%u'}", 
    PISA_LATITUDE, node_lat, 
    PISA_LONGITUDE, node_long);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}


