<?xml version="1.0" encoding="UTF-8"?>
<!--
  Success response. Plain XSLT 1.0 -> works with the normal xslt: component (slide 35).
  Input body: the ORIGINAL request, restored from the requestBody property (slide 12).
  Parameters come from Exchange headers/properties: referenceId (header), rmsStatus, traceId (properties).
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

    <xsl:param name="referenceId"/>
    <xsl:param name="rmsStatus"/>
    <xsl:param name="traceId"/>

    <xsl:template match="/">
        <mnpValidationResponse>
            <referenceId><xsl:value-of select="$referenceId"/></referenceId>
            <traceId><xsl:value-of select="$traceId"/></traceId>
            <status>SUCCESS</status>
            <errorCode>000</errorCode>
            <errorDesc>MNP validation successful</errorDesc>
            <msisdn><xsl:value-of select="/requestBody/msisdn"/></msisdn>
            <simNo><xsl:value-of select="/requestBody/simNo"/></simNo>
            <rmsStatus><xsl:value-of select="$rmsStatus"/></rmsStatus>
        </mnpValidationResponse>
    </xsl:template>
</xsl:stylesheet>