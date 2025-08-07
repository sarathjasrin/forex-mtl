# Forex Rates Proxy Service

A production-ready Scala service that proxies currency exchange rate requests to an external API, with in-memory caching and periodic cleanup.

## Features
- **REST API**: Handles `GET /rates?pair=USDJPY&pair=EURUSD` to fetch exchange rates.
- **Caching**: Stores rates in memory with a configurable TTL (default: 300 seconds).
- **Cache Cleanup**: Periodically removes expired entries to manage memory.
- **Query Validation**: Ensures valid currency pairs (e.g., USD, EUR) via query parameters.
- **Logging**: Uses Logback for detailed logging of cache hits/misses, errors, and cleanup.
- **Functional Design**: Leverages Cats Effect for IO handling and tagless final pattern.

## Prerequisites
- **Scala**: 2.13.12
- **SBT**: 1.9.0 or later
- **Java**: JDK 11 or later

## Setup
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/sarathjasrin/forex-mtl.git
   cd forex-rates-proxy
   ```
   
2. **Configuration:** \n
   Edit `src/main/resources/application.conf`:
   ```hocon
   http {
      "host" = "0.0.0.0"
      port = 3000
      timeout = 180 seconds
   }
   oneFrame {
      cache-ttl = 295 seconds
      url = "<external-api-url>"
      token = "<api-token>"
      cleanup-interval = 50 seconds
   }
   ```
3. **Install Dependencies:**
   Run `sbt update` to download dependencies listed in `build.sbt`.

## Build and Run

- **Compile:**
```bash
  sbt clean compile
```

- **Run:**
```bash
  sbt run
```

## Usage
**Endpoint:** GET /rates?pair=<FROM><TO>&pair=<FROM><TO>

**Example:** curl "http://localhost:3000/rates?pair=USDJPY&pair=EURUSD" 

**Response:**

```json
[
  {
    "from": "USD",
    "to": "JPY",
    "bid": 145.12,
    "ask": 145.15,
    "price": 145.135,
    "time_stamp": "2025-08-07T21:00:00Z"
  },
  {
    "from": "EUR",
    "to": "USD",
    "bid": 1.09,
    "ask": 1.10,
    "price": 1.095,
    "time_stamp": "2025-08-07T21:00:00Z"
  }
]
```

**Cache Behavior:**

Since `One-Frame API` supports up to 10k per day. We could utilize in-memory cache to return results up to 5mins.
 
- Rates are cached for `295 seconds` (configurable via `oneFrame.cache-ttl`).
- Subsequent requests within TTL return cached rates (logged as cache hits).
- Expired entries are removed every 50 seconds (configurable via `oneFrame.cleanup-interval`).
- Currently, cache is stored in in-memory, but it could be extended to use In-memory data-stores like `redis`