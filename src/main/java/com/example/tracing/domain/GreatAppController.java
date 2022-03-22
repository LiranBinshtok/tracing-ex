package com.example.tracing.domain;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.tracing.config.httpClients.HttpClientWithTracing;

import java.io.IOException;

@RestController
public class GreatAppController {

    HttpClientWithTracing client;

    @Autowired
    public  GreatAppController(HttpClientWithTracing client){
        this.client = client;
    }


    @GetMapping("/hello")
    //@WithSpan
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) throws InterruptedException {

           // Thread.sleep(1500);
        return String.format("Hello %s!", name);
    }


    @GetMapping("/hi")
    //@WithSpan
    public String hello(@RequestParam(value = "myName", defaultValue = "World") String name) throws InterruptedException {

        return String.format("Hello %s!", name);
    }

    @GetMapping("/call")
    //@WithSpan
    public String call() throws InterruptedException, IOException {
        Thread.sleep(300);
        HttpGet req = new HttpGet("http://localhost:8081/hello");
        HttpResponse a = client.execute(req);
        Thread.sleep(500);
        return String.format("Hello %s!","World");
    }
}
