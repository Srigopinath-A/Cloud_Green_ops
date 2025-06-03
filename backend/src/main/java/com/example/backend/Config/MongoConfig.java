package com.example.backend.Config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.core.convert.converter.Converter;

import com.google.gson.Gson;
import org.bson.Document;

public class MongoConfig {
    private final Gson gson;

    public MongoConfig(Gson gson) {
        this.gson = gson;
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoMappingContext context) {
        MappingMongoConverter converter = new MappingMongoConverter(
                NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(customConversions());
        return converter;
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new GsonReader(gson),
            new GsonWriter(gson)
        ));
    }

    // Custom Gson reader/writer
    private static class GsonReader implements Converter<String, Document> {
        private final Gson gson;

        GsonReader(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Document convert(String source) {
            return gson.fromJson(source, Document.class);
        }
    }

    private static class GsonWriter implements Converter<Document, String> {
        private final Gson gson;

        GsonWriter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public String convert(Document source) {
            return gson.toJson(source);
        }
    }
}
