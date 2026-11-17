package com.example.demo.validator;

import com.example.demo.exception.ValidationException;
import org.apache.camel.Exchange;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Same idea as util / BaseValidator.java in larger projects (slide 11).
 *
 * XML calls:   <bean ref="MnpValidationValidate" method="doValidate"/>
 * doValidate() lives HERE in the parent class: it runs common checks,
 * then calls doValidateOnProcess() which each project implements.
 */
public abstract class BaseValidator {

    /** Entry method called from the route. */
    public void doValidate(Exchange exchange) throws Exception {
        Document doc;
        try {
            // Camel type converter: String body -> DOM Document
            doc = exchange.getIn().getBody(Document.class);
        } catch (RuntimeException e) {
            throw new ValidationException("Request body is not valid XML");
        }
        if (doc == null || doc.getDocumentElement() == null) {
            throw new ValidationException("Request body is empty");
        }

        checkCommonHeaders(exchange);
        doValidateOnProcess(exchange, doc);
    }

    /** Common request-header checks shared by every API. */
    protected void checkCommonHeaders(Exchange exchange) throws ValidationException {
        String channel = exchange.getIn().getHeader("channel", String.class);
        if (isBlank(channel)) {
            throw new ValidationException("Missing mandatory header: channel");
        }
    }

    /** Project-specific validation. */
    protected abstract void doValidateOnProcess(Exchange exchange, Document doc) throws Exception;

    protected String readField(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }

    protected boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}