package com.example.tracing.config;

import com.example.tracing.config.httpClients.HttpClientWithTracing;
import com.example.tracing.config.interceptors.TracingInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.UUID;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

@Configuration
public class TracingConfiguration implements WebMvcConfigurer {

    public static final String SERVICE_NAME_PARAM = "service.name";
    public static final String SERVICE_INSTANCE_ID_PARAM = "service.instance.id";
    private String apiKey;
    private String serviceName;
    private String instanceId;
    private OpenTelemetry openTelemetry;
    private TracingInterceptor interceptor;

    public  TracingConfiguration(){

        this("","Liran-server-app", UUID.randomUUID().toString());
    }

    public  TracingConfiguration( String apiKey, String serviceName, String instanceId ){

        //TODO: API key should come from secret store
        this.apiKey = apiKey;
        this.serviceName = serviceName;
        this.instanceId = instanceId;

        this.initTracing();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(this.interceptor)
                .addPathPatterns("/hello", "/call");
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    public void initTracing(){
        OtlpHttpSpanExporterBuilder spanExporterBuilder =
                OtlpHttpSpanExporter.builder()
                        .setEndpoint("https://otlp.eu01.nr-data.net:4318/v1/traces")
                        .addHeader("api-key", apiKey);

        // Configure resource
        Resource resource =
                Resource.getDefault()
                        .merge(
                                Resource.builder()
                                        .put(stringKey(SERVICE_NAME_PARAM), serviceName)
                                        .put(stringKey(SERVICE_INSTANCE_ID_PARAM), instanceId)
                                        .build());

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporterBuilder.build()).build())
                .build();

        openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();

        //TODO: move to DI somehow.
        this.interceptor = new TracingInterceptor(openTelemetry);
    }


    @Bean
    public OpenTelemetry getOt(){
        return this.openTelemetry;
    }

    @Bean
    public HttpClientWithTracing getHttpClient(){
        return new HttpClientWithTracing(openTelemetry);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
