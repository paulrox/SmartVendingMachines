/**
 * \file
 *      Sensed Temperature Resource
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
#define TIME_SAMPLING 0.01*CLOCK_SECOND
#define STARTING_TEMPERATURE 0

extern float u_k;
float e_k = 0;
float e_k_1 = 0;
float e_k_2 = 0;
float temp_k = 0; 
float temp_k_1 = 0; 
float temp_k_2 = 0; 

static void sens_periodic_handler();
static void sens_get_handler(void *request, void *response, uint8_t *buffer,
                           uint16_t preferred_size, int32_t *offset);


PERIODIC_RESOURCE(sens, "title=\"sensTemp\";rt=\"Text\"", sens_get_handler, 
  NULL, NULL, NULL, TIME_SAMPLING, sens_periodic_handler);

static void sens_get_handler(void* request, void* response, 
  uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  /* Populat the buffer with the response payload */
  char message[50];
  int length;
  float tmp;

  tmp = (float)((float)temp_k - (int)temp_k);
  tmp = tmp * 100;
  sprintf(message, "{'temp':'%d.%d'}", (int)temp_k, (int)tmp);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.APPLICATION_JSON); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

/*
 * Implemented the following controlled systems
 * Fast version(settling time 5s)
 * temp_k = 1.9*temp_k_1 - 0.9*temp_k_2 + 0.0011488*e_k;  
 * 0.0011488 z^2
 * -------------
 * (z-1) (z-0.9)
 *
 * Slow version(settling time 30s)
 * temp_k = 1.9899*temp_k_1 - 0.9899*temp_k_2 + 0.000014941*e_k;
 *   1.4941e-05 (z^2 + 9.315e-14)
 * ----------------------------
 *   (z-0.9918) (z-0.9982)
 */

static void sens_periodic_handler()
{
  

  e_k = u_k - temp_k;
  
  temp_k = 1.9899*temp_k_1 - 0.9899*temp_k_2 + 0.000014941*e_k;
  //temp_k = 1.9*temp_k_1 - 0.9*temp_k_2 + 0.0011488*e_k; 
  
  /* To output
  float tmp;
  tmp = (float)((float)temp_k - (int)temp_k);
  tmp = tmp * 100;
  
  if (temp_k != temp_k_1)
    printf("Sensed temperature: %d.%d\n", (int)temp_k, (int)tmp);*/

  e_k_2 = e_k_1;
  e_k_1 = e_k;
  temp_k_2 = temp_k_1;
  temp_k_1 = temp_k; 
}

