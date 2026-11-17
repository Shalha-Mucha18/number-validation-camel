<?xml version="1.0" encoding="UTF-8"?>
<!-- Common error response used by direct:LogOrError. Values come from headers/properties. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

    <xsl:param name="referenceId"/>
    <xsl:param name="traceId"/>
    <xsl:param name="errorCode"/>
    <xsl:param name="errorDesc"/>

    <xsl:template match="/">
        <mnpValidationResponse>
            <referenceId><xsl:value-of select="$referenceId"/></referenceId>
            <traceId><xsl:value-of select="$traceId"/></traceId>
            <status>FAILED</status>
            <errorCode><xsl:value-of select="$errorCode"/></errorCode>
            <errorDesc><xsl:value-of select="$errorDesc"/></errorDesc>
        </mnpValidationResponse>
    </xsl:template>
</xsl:stylesheet>