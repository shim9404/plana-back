package com.example.plana.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis를 Spring Cache 저장소로 사용하기 위한 설정 클래스
 *
 * 주요 역할:
 * 1. Spring Cache 기능 활성화
 * 2. Redis 기반 CacheManager 등록
 * 3. Redis Key / Value 직렬화 방식 설정
 * 4. 캐시 데이터 TTL(만료시간) 설정
 */
@Configuration
@EnableCaching // @Cacheable, @CachePut, @CacheEvict 등의 캐시 기능 활성화
public class RedisCacheConfig {

    /**
     * Redis 기반 CacheManager Bean 등록
     *
     * @param redisConnectionFactory Redis 서버와 연결하기 위한 팩토리 객체
     * @return Redis를 사용하는 CacheManager
     */
    @Bean
    public CacheManager boardCacheManager(RedisConnectionFactory redisConnectionFactory) {

        // Redis 캐시의 기본 동작 방식을 설정
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration
                .defaultCacheConfig()

                // Redis Key 저장 방식 설정
                // 캐시 이름 + 키 값이 문자열 형태로 저장되도록 지정
                // 예: board::1
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )

                // Redis Value 저장 방식 설정
                // Java 객체를 JSON 형태로 변환해서 저장
                // 예: {"id":1,"title":"게시글 제목","content":"내용"}
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<Object>(Object.class)
                        )
                )

                // 캐시 데이터 유효 시간(TTL: Time To Live) 설정
                // 현재는 5분 후 자동 만료
                .entryTtl(Duration.ofMinutes(5L));

        // 위에서 만든 설정을 바탕으로 RedisCacheManager 생성
        return RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}

/*
이 파일은 Spring Boot에서 Redis를 캐시 저장소로 사용하도록 설정하는 클래스입니다.
핵심 역할은 다음 4가지입니다.

캐시 기능 활성화
@EnableCaching으로 @Cacheable, @CachePut, @CacheEvict 같은 스프링 캐시 기능을 사용할 수 있게 합니다.
Redis를 CacheManager로 등록
스프링이 기본 메모리 캐시가 아니라 Redis 기반 캐시를 사용하도록 만듭니다.
Redis에 저장되는 데이터 직렬화 방식 설정
Key는 문자열(String)
Value는 JSON
캐시 만료시간(TTL) 설정
현재 설정은 5분
캐시 데이터는 Redis에 저장된 뒤 5분 후 자동 삭제됩니다
*/
