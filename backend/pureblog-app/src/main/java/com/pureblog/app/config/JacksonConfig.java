package com.pureblog.app.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 全局 Jackson 配置:
 *  1. Long / long / BigInteger 一律序列化为字符串, 避免 JS Number 精度丢失 (Snowflake id 超出 2^53)
 *  2. 入参: Long 同时接受 JSON 数字和数字字符串
 *  3. LocalDate / LocalDateTime 统一 yyyy-MM-dd / yyyy-MM-dd HH:mm:ss, 入参同时兼容纯日期字符串
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            SimpleModule idModule = new SimpleModule("id-as-string");
            // 序列化: Long / long / BigInteger 全部输出为字符串, 防 JS 精度丢失
            idModule.addSerializer(Long.class, ToStringSerializer.instance);
            idModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            idModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
            // 反序列化: 兼容数字字符串 (前端以字符串回传 id)
            idModule.addDeserializer(Long.class, new NumericStringDeserializer());
            idModule.addDeserializer(Long.TYPE, new NumericStringDeserializer());

            JavaTimeModule timeModule = new JavaTimeModule();
            timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FMT));
            timeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
            timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FMT));
            timeModule.addDeserializer(LocalDate.class, new FlexibleLocalDateDeserializer());

            builder.modulesToInstall(idModule, timeModule);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    /** Long 既接受 JSON 数字也接受数字字符串. */
    static class NumericStringDeserializer extends JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                String s = p.getText();
                if (s == null || s.isEmpty()) return null;
                return Long.parseLong(s.trim());
            }
            return p.getLongValue();
        }
    }

    /** LocalDateTime: yyyy-MM-dd HH:mm:ss / yyyy-MM-dd / ISO 都能解析. */
    static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getValueAsString();
            if (s == null || s.isEmpty()) return null;
            try {
                return LocalDateTime.parse(s, DATETIME_FMT);
            } catch (java.time.format.DateTimeParseException ignored) { }
            try {
                return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (java.time.format.DateTimeParseException ignored) { }
            // 纯日期: 补 12:00:00 避免跨时区漂到前一天
            LocalDate d = LocalDate.parse(s, DATE_FMT);
            return LocalDateTime.of(d, java.time.LocalTime.NOON);
        }
    }

    /** LocalDate: yyyy-MM-dd / yyyy-MM-dd HH:mm:ss / ISO 都能解析. */
    static class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getValueAsString();
            if (s == null || s.isEmpty()) return null;
            try {
                return LocalDate.parse(s, DATE_FMT);
            } catch (java.time.format.DateTimeParseException ignored) { }
            try {
                return LocalDateTime.parse(s, DATETIME_FMT).toLocalDate();
            } catch (java.time.format.DateTimeParseException ignored) { }
            return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }
}
