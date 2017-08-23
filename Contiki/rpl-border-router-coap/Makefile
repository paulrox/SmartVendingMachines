CONTIKI_PROJECT=border-router
all: $(CONTIKI_PROJECT) 

CONTIKI=/home/user/contiki-3.0

#RPL Border Router with CoAP Server to get the motes IP addresses.

#linker optimizations
SMALL=1

CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"
PROJECT_SOURCEFILES += slip-bridge.c

ifeq ($(PREFIX),)
 PREFIX = aaaa::1/64
endif

# REST Engine shall use Erbium CoAP implementation
APPS += er-coap
APPS += rest-engine

CONTIKI_WITH_IPV6 = 1
include $(CONTIKI)/Makefile.include

$(CONTIKI)/tools/tunslip6:	$(CONTIKI)/tools/tunslip6.c
	(cd $(CONTIKI)/tools && $(MAKE) tunslip6)

connect-router:	$(CONTIKI)/tools/tunslip6
	sudo $(CONTIKI)/tools/tunslip6 $(PREFIX)

connect-router-cooja:	$(CONTIKI)/tools/tunslip6
	sudo $(CONTIKI)/tools/tunslip6 -a 127.0.0.1 $(PREFIX)
