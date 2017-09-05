/**
 * \file
 *      Food machine source code.
 * \author
 *      Paolo Sassi
 * \author
 *      Matteo Rotundo
 */

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

static struct ctimer ct;

static struct coordinate locations[] = { 
  {43.7210, 10.3898},
  {43.7178, 10.3827},
  {43.7229, 10.3965},
  {43.7093, 10.3985},
  {43.7165, 10.4022},
  {43.7111, 10.4105},
  {43.7195, 10.3981},
  {43.7192, 10.4240}
};

float u_k = 0;
unsigned int qty_tick = 0, alarm_tick = 0;
int machine_id, machine_status;
int machine_type;
char alarm_type;
float node_lat, node_lng;
struct product productA, productB;

extern resource_t id, productAqty, productBqty, 
  productAprice, productBprice, 
  status_m, loc, sens, des, alarm;

/**
 * @brief Function to initialize the vending machine
 *
 */

void init_vending_machine()
{ 
  machine_id = node_id;
  machine_status = 0;
  machine_type = 0;
  
  u_k = 18; // Default temperature
  node_lat = locations[node_id - 2].latitude;
  node_lng = locations[node_id - 2].longitude;

  alarm_type = 'N';
  productA.remaining_qty = MAX_PRODUCT_AVAILABILITY;
  productA.price = 1.5;

  productB.remaining_qty = MAX_PRODUCT_AVAILABILITY;
  productB.price = 2.5;
}

static void ctimer_callback(void *ptr)
{

  if (machine_status == 1) {
    qty_tick++;
    alarm_tick++;
    unsigned short ret;

    if (qty_tick == QTY_DELAY) {
      qty_tick = 0;
      ret = random_rand() % 4;
      if ((productA.remaining_qty - (int)ret) >= 0) 
        productA.remaining_qty -= ret;

        ret = random_rand() % 4;
      if ((productB.remaining_qty - (int)ret) >= 0)
         productB.remaining_qty -= ret;
    }

    if (alarm_tick == ALARM_DELAY) {
      ret = random_rand() % 3;
      switch(ret) {
      case 0:
        if (alarm_type == 'N')
          alarm_type = 'I';
        break;
      case 1:
        if (alarm_type == 'N')
          alarm_type = 'F';
        break;
      case 2:
        alarm_type = 'N';
        break;
      default:
        alarm_type = 'N';
        break;
    }
      alarm_tick = 0;
    }
  }
  ctimer_restart(&ct);
}

PROCESS(server, "food_vending_machine");
AUTOSTART_PROCESSES(&server);

PROCESS_THREAD(server, ev, data)
{
  PROCESS_BEGIN();

  PROCESS_PAUSE();

  init_vending_machine();
  rest_init_engine();

  rest_activate_resource(&id, "id");
  rest_activate_resource(&loc, "loc");
  rest_activate_resource(&status_m, "status");
  rest_activate_resource(&sens, "temp/sens");
  rest_activate_resource(&des, "temp/des");
  rest_activate_resource(&alarm, "alarm");
  rest_activate_resource(&productAqty, "ProductA/qty");
  rest_activate_resource(&productAprice, "ProductA/price");
  rest_activate_resource(&productBqty, "ProductB/qty");
  rest_activate_resource(&productBprice, "ProductB/price");

  ctimer_set(&ct, CLOCK_SECOND, ctimer_callback, NULL);
  while(1) {
    PROCESS_WAIT_EVENT();
  }
  
  PROCESS_END();
}
