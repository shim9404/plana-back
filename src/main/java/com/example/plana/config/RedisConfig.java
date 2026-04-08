package com.example.plana.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 이 클래스는 Spring Boot가 Redis를 쓰려면 내부적으로 어디 Redis서버에 붙을지 알아야 함.
 * 그 역할을 하는 것이 redisConnectionFactory() 입니다.
 * 이 파일과 RedisCacheConfig 차이
 * RedisConfig는 Redis서버 연결 설정함.
 * 또한 어디(host/port) 붙을지 결정함.
 * RedisCacheConfig는 Redis를 캐시로 사용할 때의 동작 설정.
 * key/value 직렬화, TTL, CacheManager설정
 *
 *
 * 구조를 보는 흐름은
 * application.yml -> RedisConfig -> LettuceConnectionFactory 생성 -> RedisCacheConfig
 * Redis 서버 연결 설정 클래스
 *
 * LettuceConnectionFactory는 Redis 서버와의 연결을 관리하는 팩토리 클래스입니다.
 * Redis 서버에 연결을 생성하고, 연결을 관리하고
 * 다른 Redis관련 기능이 사용할 수 있도록 제공하는 객체 임.
 *
 *
 * 주요 역할:
 * 1. application.yml(application.properties)에 정의된 Redis 접속 정보(host, port)를 읽는다.
 * 2. Redis 단일 서버(Standalone) 설정 객체를 만든다.
 * 3. LettuceConnectionFactory Bean을 스프링 컨테이너에 등록한다.
 *
 * 이 Bean은 이후 RedisTemplate, CacheManager, Spring Cache, 세션 저장 등
 * Redis를 사용하는 다른 기능들이 공통으로 사용하게 된다.
 */
@Configuration
public class RedisConfig {

    /**
     * application.yml의 spring.data.redis.host 값을 주입받는다.
     * 예: localhost, 127.0.0.1, redis(도커 컴포즈 서비스명)
     */
    @Value("${spring.data.redis.host}")
    private String host;

    /**
     * application.yml의 spring.data.redis.port 값을 주입받는다.
     * 예: 6379
     */
    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Redis 연결 팩토리 Bean 생성
     *
     * 역할:
     * - Lettuce 클라이언트를 사용하여 Redis 서버와의 연결을 생성/관리한다.
     * - host, port 정보를 기반으로 Redis 서버 접속 정보를 설정한다.
     * - 이후 RedisCacheManager, RedisTemplate 등이 이 연결 팩토리를 사용한다.
     *
     * @return Redis 연결을 담당하는 LettuceConnectionFactory 객체
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        // 단일 Redis 서버(Standalone) 접속 정보를 설정하는 객체 생성
        // host: Redis 서버 주소
        // port: Redis 서버 포트 번호
        RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration(host, port);

        // Lettuce 기반 Redis 연결 팩토리 생성 후 반환
        // 스프링 컨테이너에 Bean으로 등록되어 Redis 관련 기능들이 재사용한다.
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
}