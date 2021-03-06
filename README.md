# spring-cloud-gateway-sample
- Official Sample: https://spring.pleiades.io/guides/gs/gateway/

- 通常リクエスト
```
$ curl http://localhost:8080/get
```

- サーキットブレーカーを試すリクエスト
```
$ curl --dump-header - --header 'Host: www.circuitbreaker.com' http://localhost:8080/delay/3
```

- Wiremock は [11. Spring Cloud Contract WireMock](https://cloud.spring.io/spring-cloud-contract/2.0.x/multi/multi__spring_cloud_contract_wiremock.html) を利用

# TODO
- [x] setting route with yaml file / testing 
- [x] Retry and Retry Test
- [x] Custom filter (https://www.fixes.pub/program/199584.html)
- [x] Global filter: Pre / Post Request (https://www.javainuse.com/spring/cloud-gateway)
- [x] Custom Error Handler
- [ ] Challenge circuit breaker
- [ ] Analyze JFR
- [ ] Performance Test → Blocking Spring Boot vs Spring Cloud Gateway
