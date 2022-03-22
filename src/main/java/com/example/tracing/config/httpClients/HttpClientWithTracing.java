package com.example.tracing.config.httpClients;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class HttpClientWithTracing extends DefaultHttpClient {

    OpenTelemetry openTelemetry;

    public  HttpClientWithTracing(OpenTelemetry openTelemetry){
        this.openTelemetry = openTelemetry;
    }

    public static TextMapSetter<HttpUriRequest> contextSetter = new TextMapSetter<HttpUriRequest>() {
        @Override
        public void set(HttpUriRequest carrier, String key, String value) {
            // Insert the context as Header
            carrier.addHeader(key, value);
        }
    };


    @Override
    public CloseableHttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        openTelemetry.getPropagators().getTextMapPropagator().inject(
                Context.current(), request, contextSetter);
        return super.execute(request);
    }


}
