<?xml version="1.0" encoding="UTF-8"?>
<!--
  Builds the RMS backend request from the API request (slide 13).
  version="2.0" + replace() are XSLT 2.0 features -> needs Saxon -> called with xslt-saxon: (slide 35).

  Camel passes Exchange headers and properties as XSLT parameters,
  so $referenceId below is the "referenceId" header set by the Java validator.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

    <xsl:param name="referenceId"/>

    <xsl:template match="/">
        <xsl:variable name="rawMsisdn" select="normalize-space(/requestBody/msisdn)"/>
        <rmsRequest>
            <referenceId><xsl:value-of select="$referenceId"/></referenceId>
            <customRef>
                <idType>BS_CODE</idType>
                <idValue><xsl:value-of select="/requestBody/bsCode"/></idValue>
            </customRef>
            <!-- Number-length rule: 8801700000000 -> 1700000000 (remove country code 880) -->
            <msisdn><xsl:value-of select="replace($rawMsisdn, '^880', '')"/></msisdn>
        </rmsRequest>
    </xsl:template>
</xsl:stylesheet>