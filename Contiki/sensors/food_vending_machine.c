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
  {7210, 3898},
  {7229, 3965},
  {7206, 3832},
  {7205, 3831},
  {7203, 3837}
};

float u_k = 0;
unsigned int qty_tick = 0, alarm_tick = 0, status_tick = 0;
int machine_id, machine_status;
int machine_type;
int node_lat, node_long;
struct product productA, productB;

extern char alarm_type;
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
  machine_status = 1;
  machine_type = 0;
  
  u_k = 18; // Default temperature
  node_lat = locations[node_id - 2].latitude;
  node_long = locations[node_id - 2].longitude;

  alarm_type = 'N';
  productA.remaining_qty = MAX_PRODUCT_AVAILABILITY;
  productA.price = 1.5;

  productB.remaining_qty = MAX_PRODUCT_AVAILABILITY;
  productB.price = 2.1;
}

static void ctimer_callback(void *ptr)
{
  qty_tick++;
  alarm_tick++;
  status_tick++;

  if (qty_tick == QTY_DELAY) {
    qty_tick = 0;
    if (productA.remaining_qty > 0)
      productA.remaining_qty--;
    if (productB.remaining_qty > 0)
      productB.remaining_qty--;
  }

  if (alarm_tick == ALARM_DELAY) {
    alarm_tick = 0;
    alarm_type = 'I';
  }

  if (status_tick == STATUS_DELAY) 
    machine_status = 0;

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
