package com.example.tracing.config.interceptors;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TracingInterceptor implements HandlerInterceptor {

    OpenTelemetry openTelemetry;
    Tracer tracer;

    public TracingInterceptor(OpenTelemetry openTelemetry){
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("TracingInterceptor");
    }

    private final TextMapGetter<HttpServletRequest> contextGetter =
            new TextMapGetter<>() {
                @Override
                public String get(HttpServletRequest carrier, String key) {
                    if (carrier.getHeader(key) != null) {
                        return carrier.getHeader(key);
                    }
                    return null;
                }

                @Override
                public Iterable<String> keys(HttpServletRequest carrier) {
                    return (Iterable<String>) carrier.getHeaderNames().asIterator();
                }
            };


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        // Extract the SpanContext and other elements from the request.
        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), request, contextGetter);
        //try (Scope scope = extractedContext.makeCurrent()) {
        // Automatically use the extracted SpanContext as parent.
        Span span = tracer.spanBuilder(String.format("%s %s", request.getMethod(), path))
                .setParent(extractedContext)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        span.makeCurrent();

            return HandlerInterceptor.super.preHandle(request, response, handler);
        }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Span span = Span.current();
                span.end();
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
