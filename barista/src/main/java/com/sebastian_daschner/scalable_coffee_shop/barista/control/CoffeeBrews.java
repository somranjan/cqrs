package com.sebastian_daschner.scalable_coffee_shop.barista.control;

import com.sebastian_daschner.scalable_coffee_shop.events.entity.CoffeeBrewFinished;
import com.sebastian_daschner.scalable_coffee_shop.events.entity.CoffeeBrewStarted;
import com.sebastian_daschner.scalable_coffee_shop.events.entity.CoffeeDelivered;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.util.Collections.unmodifiableCollection;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class CoffeeBrews {

    private final Set<UUID> unfinishedBrews = new ConcurrentSkipListSet<>();
    private final Set<UUID> undeliveredOrders = new ConcurrentSkipListSet<>();

    public Collection<UUID> getUnfinishedBrews() {
        return unmodifiableCollection(unfinishedBrews);
    }

    public Collection<UUID> getUndeliveredOrders() {
        return unmodifiableCollection(undeliveredOrders);
    }

    public void apply(@Observes CoffeeBrewStarted event) {
        unfinishedBrews.add(event.getOrderInfo().getOrderId());
    }

    public void apply(@Observes CoffeeBrewFinished event) {
        final Iterator<UUID> iterator = unfinishedBrews.iterator();
        while (iterator.hasNext()) {
            final UUID orderId = iterator.next();
            if (orderId.equals(event.getOrderId())) {
                iterator.remove();
                undeliveredOrders.add(orderId);
            }
        }
    }

    public void apply(@Observes CoffeeDelivered event) {
        undeliveredOrders.removeIf(i -> i.equals(event.getOrderId()));
    }

}
