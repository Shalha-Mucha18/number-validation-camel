#  MNP Validation

A small Apache Camel 3 + Spring Boot 2.7 project that mirrors the structure of a typical integration service. It exposes two XML REST APIs, validates requests in Java, calls a (mock) RMS backend, transforms messages with XSLT, and writes a transaction log through SEDA or Kafka.

## Tech stack

| Part | Version / choice |
|---|---|
| Java | 1.8 target (runs on 8, 11, 17) |
| Spring Boot | 2.7.14 |
| Apache Camel | 3.14.10 (Spring XML DSL) |
| HTTP server | Camel Jetty (no Spring Web) |
| Transformations | XSLT 1.0 (`xslt:`) and XSLT 2.0 (`xslt-saxon:`) |
| Transaction log | SEDA (default) or Kafka (optional profile) |
| Build | Maven |

## Project structure

```
camel-demo/
├── pom.xml
├── test-all.sh                         # sends every sample request
├── requests/                           # sample request files
└── src/main/
    ├── java/com/example/demo/
    │   ├── Application.java            # Spring Boot entry, imports the XML files
    │   ├── exception/ValidationException.java
    │   ├── validator/BaseValidator.java            # common checks (template method)
    │   ├── validator/MnpValidationValidator.java   # MNP-specific checks
    │   ├── aggregate/ChannelAggregationStrategy.java
    │   └── support/TraceIdProcessor.java
    └── resources/
        ├── application.properties          # common settings, active profile
        ├── application-dev.properties      # ports, backend URL, SEDA URIs
        ├── application-kafka.properties    # Kafka URIs (optional profile)
        ├── spring/
        │   ├── beans.xml                   # Java beans used by routes
        │   ├── camel-context.xml           # THE CamelContext: errors, REST, main routes
        │   ├── common-routes.xml           # error route + transaction log routes
        │   └── mock-backend-routes.xml     # fake RMS backend
        └── customResources/transformation/xslt/
            ├── Xform_RMS_Request.xsl       # XSLT 2.0 (Saxon)
            ├── Xform_FinalResponse.xsl
            └── Xform_ErrorResponse.xsl
```

**Reading order:** `Application.java` → `camel-context.xml` (onException → rest → main route → sub-routes) → Java beans and XSLT files.

## Run

```bash
# Default (dev profile, SEDA transaction log)
mvn spring-boot:run

# With a real Kafka broker on localhost:9092
mvn spring-boot:run -Dspring-boot.run.profiles=dev,kafka
```

| Port | Purpose |
|---|---|
| 9091 | REST API (`rest.port`) |
| 9093 | Mock RMS backend (`mock.rms.port`) |

Then, in another terminal:

```bash
./test-all.sh
```

## APIs

Base URL: `http://localhost:9091/cxf/api/v1/demo`

### `POST /mnpValidation`

- Content type: `application/xml`
- Required header: `channel` (for example `MYAPP`)
- Optional header: `X-Trace-Id` (generated if missing)

```xml
<requestBody>
  <referenceId>REF-1001</referenceId>
  <msisdn>8801700000001</msisdn>
  <simNo>8988031234567890</simNo>
  <bsCode>DEMO-BS</bsCode>
</requestBody>
```

Success response:

```xml
<mnpValidationResponse>
  <referenceId>REF-1001</referenceId>
  <traceId>...</traceId>
  <status>SUCCESS</status>
  <errorCode>000</errorCode>
  <errorDesc>MNP validation successful</errorDesc>
  <msisdn>8801700000001</msisdn>
  <simNo>8988031234567890</simNo>
  <rmsStatus>ACTIVE</rmsStatus>
</mnpValidationResponse>
```

Errors use the same shape with `<status>FAILED</status>`. The HTTP status stays **200**; check `errorCode`.

### `POST /channelStatus`

Splits a channel list, checks each channel, and aggregates the results.

```xml
<ChannelList>
  <Channel>MYAPP</Channel>
  <Channel>USSD</Channel>
  <Channel>WEB</Channel>
</ChannelList>
```

Response: `<ChannelStatusResponse>` with one `<ChannelResult name="..." status="UP|DOWN"/>` per channel. `USSD` is always `DOWN` in the mock.

## Error codes

| Code | Meaning | Raised by |
|---|---|---|
| 039 | SIM number is mandatory | `<choice>` in main route |
| 040 | SIM number is blocked (`simNo` starts with `0000`) | `<choice>` in main route |
| 041 | Number status in RMS is not `ACTIVE` | `<choice>` after the RMS call |
| RMS500 | RMS backend not available | `doCatch` in `RmsInfo_Route` |
| EX002 | Validation failed (bad XML, missing field or `channel` header, bad msisdn) | `ValidationException` |
| EX999 | Any other unexpected error | global `onException` |

## Test scenarios

| # | Request file | Expected result |
|---|---|---|
| 1 | `01-success.xml` | Success |
| 2 | `02-empty-sim.xml` | 039 |
| 3 | `03-blocked-sim.xml` | 040 |
| 4 | `04-missing-bscode.xml` | EX002 |
| 5 | `05-inactive-number.xml` (msisdn ends `999`) | 041 |
| 6 | `06-rms-down.xml` (msisdn ends `500`) | RMS500 |
| 7 | `01-success.xml` without the `channel` header | EX002 |
| 8 | `07-channel-list.xml` to `/channelStatus` | Split + aggregate |

## How a request flows

```
REST POST → direct:mnpValidationStart
  1. TraceIdProcessor           add / keep X-Trace-Id
  2. MnpValidationValidator     Java validation, set header + properties
  3. save request body          property "requestBody"
  4. <choice>                   simNo empty → 039, simNo 0000… → 040
  5. direct:RmsInfo             XSLT 2.0 → HTTP POST to mock RMS → read status
  6. rmsStatus != ACTIVE        → 041
  7. restore request body
  8. wireTap → direct:SendTxnLog   async log to SEDA / Kafka
  9. direct:FinalResponse       XSLT 1.0 → success XML
Errors anywhere → onException / direct:LogOrError → error XML
```

Key ideas:

- **Headers** travel with the message; **properties** are internal variables for one Exchange.
- The body is replaced by each backend call, so the original request is saved and restored.
- `direct:` endpoints are in-app calls to another route.
- `{{placeholders}}` in routes are filled from the properties files.

## Configuration

| Property | Where | Meaning |
|---|---|---|
| `rest.port` | dev | REST entry port |
| `mock.rms.port` | dev | Mock RMS port |
| `rmsURL` | dev | Backend URL used by `<to uri="{{rmsURL}}"/>` |
| `txnLog.producerUri` | dev / kafka | Where the transaction log is sent |
| `txnLog.consumerUri` | dev / kafka | Where the log consumer reads from |

## Mock RMS behaviour

Defined in `mock-backend-routes.xml`:

| Request contains | Response |
|---|---|
| `1700000500` | HTTP 500 |
| `1700000999` | `<status>INACTIVE</status>` |
| anything else | `<status>ACTIVE</status>` |
