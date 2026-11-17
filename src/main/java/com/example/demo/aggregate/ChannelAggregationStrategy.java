package com.example.demo.aggregate;

// Slide 31: Camel 3 import path (Camel 2 was org.apache.camel.processor.aggregate.AggregationStrategy)
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

/**
 * Used by <split strategyRef="channelAggregationStrategy"> (slide 15).
 *
 * Camel calls aggregate() once per split item:
 *   1st item: oldExchange == null
 *   next items: oldExchange = result so far, newExchange = this item's result
 */
public class ChannelAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        String part = newExchange.getIn().getBody(String.class);
        if (oldExchange == null) {
            newExchange.getIn().setBody(part);
            return newExchange;
        }
        String soFar = oldExchange.getIn().getBody(String.class);
        oldExchange.getIn().setBody(soFar + part);
        return oldExchange;
    }

    /** Called after the split to wrap all results in one root element. */
    public void bodyBrace(Exchange exchange) {
        String inner = exchange.getIn().getBody(String.class);
        // Empty <ChannelList/>: split had no items, body is still the original request
        if (inner == null || !inner.contains("<ChannelResult")) {
            inner = "";
        }
        exchange.getIn().setBody("<ChannelStatusResponse>" + inner + "</ChannelStatusResponse>");
    }
}