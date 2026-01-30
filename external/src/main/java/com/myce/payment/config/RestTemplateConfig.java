package com.myce.payment.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//   - RestTemplate = “코드 안에서 쓰는 HTTP 클라이언트”
//    Postman/브라우저 대신, 자바 코드로 외부 API(포트원) 호출할 때 쓰는 도구야.
// restclient 쓰도록 수정 -> 해보자 :todo 우선 순위 low


@Configuration
public class RestTemplateConfig {
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
