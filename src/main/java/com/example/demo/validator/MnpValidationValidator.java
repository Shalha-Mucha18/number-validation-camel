package com.example.demo.validator;

import com.example.demo.exception.ValidationException;
import org.apache.camel.Exchange;
import org.w3c.dom.Document;

/**
 * Project validator, like BiometricMnpValidationValidate.java (slide 11).
 *
 * Output of validation goes into the Exchange:
 *   - referenceId -> HEADER   (metadata that travels with the message)
 *   - msisdn, simNo, bsCode -> PROPERTIES (variables for later steps)
 */
public class MnpValidationValidator extends BaseValidator {

    /** Bangladesh mobile number with country code: 8801 + 9 digits (operator digit 3-9). */
    private static final String MSISDN_PATTERN = "^8801[3-9]\\d{8}$";

    @Override
    protected void doValidateOnProcess(Exchange exchange, Document doc) throws Exception {
        String referenceId = readField(doc, "referenceId");
        String msisdn = readField(doc, "msisdn");
        String simNo = readField(doc, "simNo");
        String bsCode = readField(doc, "bsCode");

        if (isBlank(referenceId)) {
            throw new ValidationException("referenceId is mandatory");
        }
        if (isBlank(msisdn) || !msisdn.matches(MSISDN_PATTERN)) {
            throw new ValidationException("msisdn must be in format 8801XXXXXXXXX");
        }
        if (isBlank(bsCode)) {
            throw new ValidationException("bsCode is mandatory");
        }

        // Header: travels with the message (and is sent to backends as an HTTP header)
        exchange.getIn().setHeader("referenceId", referenceId);

        // Properties: internal variables for this Exchange only
        exchange.setProperty("msisdn", msisdn);
        exchange.setProperty("bsCode", bsCode);
        // simNo is NOT checked here on purpose: the route's <choice> handles it (slide 14).
        // Store "" instead of null so the Simple expression  == ''  works.
        exchange.setProperty("simNo", simNo == null ? "" : simNo);
    }
}