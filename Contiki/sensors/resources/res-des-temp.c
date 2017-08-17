/**
 * \file
 *      Desired Temperature Resource
 * \author
 *      Paolo Sassi
 * \author
 *      Matteo Rotundo
 */

#include "contiki.h"
#include <stdlib.h>
#include <string.h>
#include "rest-engine.h"

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


extern float u_k;

static void des_put_handler(void *request, void *response, uint8_t *buffer,
                           uint16_t preferred_size, int32_t *offset);

RESOURCE(des, "title=\"id\";rt=\"Text\"", NULL, NULL, des_put_handler,
         NULL);

static void des_put_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int new_value, len;
  const char *val = NULL;

  printf("Put\n");
  len = REST.get_post_variable(request, "value", &val);
     
  if (len > 0) {
     new_value = atoi(val);
     PRINTF("new value %u\n", new_value);
     u_k = (float)new_value;
     REST.set_response_status(response, REST.status.CREATED);
  } else {
     REST.set_response_status(response, REST.status.BAD_REQUEST);
  }
}