# KeyForge Spring Boot Starter

KeyForge Spring Boot Starter integrates the [KeyForge](https://github.com/bgalek/keyforge) library
with Spring Security, enabling API key authentication via a simple filter and auto-configuration.

[![Build](https://github.com/bgalek/spring-boot-starter-keyforge/actions/workflows/build.yaml/badge.svg?branch=main)](https://github.com/bgalek/spring-boot-starter-keyforge/actions/workflows/build.yaml)
![Maven Central](https://img.shields.io/maven-central/v/com.github.bgalek/spring-boot-starter-keyforge?style=flat-square)

## Requirements

- Java 17 or higher
- Spring Boot 3.4.3 or higher

## Installation

```kotlin
dependencies {
    implementation("com.github.bgalek:spring-boot-starter-keyforge:1.0.0")
}
```

## Generating keys

Use the `keyforge` library directly to generate keys that you then store in configuration:

```java
KeyForge keyForge = new KeyForge();
ApiKey apiKey = keyForge.newKey()
        .withIdentifier("my-service")
        .build();
System.out.println(apiKey); // sk_my-service_MDE5MmVkOGFh...
```

For expiring keys use `ExpiringKeyForge`. See the [KeyForge README](https://github.com/bgalek/keyforge) for full details.

## Configuration

Add the generated keys to `application.yml`. Multiple keys are supported:

```yaml
keyforge:
  keys:
    - sk_my-service_MDE5MmVkOGFhZGMyNzRiZGJlYTk4M2E4ZDk3NGU4NTc
    - sk_other-service_MDk4ZjZiY2Q0NjIxMzM3MzhhZGU0ZTgzMjYyN2I0ZjY
```

## Usage

`KeyForgeAuthenticationFilter` is registered as a bean automatically but is **not added to the filter chain by default** — this gives you full control over where it sits in your security configuration.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            KeyForgeAuthenticationFilter keyForgeAuthenticationFilter
    ) throws Exception {
        http.addFilterBefore(keyForgeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
        );
        return http.build();
    }
}
```

Clients authenticate by sending the key in the `Authorization` header:

```http
GET /api/resource HTTP/1.1
Authorization: Token sk_my-service_MDE5MmVkOGFh...
```

On a successful match, Spring Security's `SecurityContext` is populated with the key's identifier as the principal name. Malformed or unrecognised tokens are silently ignored so downstream security rules handle the rejection.

## Custom Clock

If a `Clock` bean is present in the application context it will be injected into the filter automatically. This is useful for testing:

```java
@Bean
public Clock clock() {
    return Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
}
```
