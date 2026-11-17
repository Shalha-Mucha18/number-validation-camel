package com.example.demo.support;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Simplified version of util / TraceIdSupport.captureOrCreate (slide 42).
 * Keep the caller's X-Trace-Id if present, otherwise generate one.
 * Every log line in the routes prints it, so you can follow one request.
 */
public class TraceIdProcessor implements Processor {

    public static final String HEADER = "X-Trace-Id";

    @Override
    public void process(Exchange exchange) {
        String traceId = exchange.getIn().getHeader(HEADER, String.class);
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        exchange.getIn().setHeader(HEADER, traceId);
        exchange.setProperty("traceId", traceId);
    }
}