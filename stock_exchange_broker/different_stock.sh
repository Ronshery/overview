#!/bin/zsh

( { echo "client6" ; sleep 3 ; echo "buy LIDL 10" ; sleep 3 ; echo "list" ; sleep 3 ; echo "quit" } | java -cp activemq-all-5.16.0/activemq-all-5.16.0.jar:./out/production/verteilte-systeme-2020-hausaufgabe-3-gruppe-21/ de.tu_berlin.cit.vs.jms.client.JmsBrokerClient ) &
( { echo "client7" ; sleep 3 ; echo "buy ADIDAS 10" ; sleep 3 ; echo "list" ; sleep 3 ; echo "quit" } | java -cp activemq-all-5.16.0/activemq-all-5.16.0.jar:./out/production/verteilte-systeme-2020-hausaufgabe-3-gruppe-21/ de.tu_berlin.cit.vs.jms.client.JmsBrokerClient )