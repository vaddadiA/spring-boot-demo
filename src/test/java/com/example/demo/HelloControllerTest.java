package com.example.demo;

import com.example.demo.controller.HelloController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloControllerTest {
    @Test
    void testHello() {
        HelloController controller = new HelloController();
        assertEquals("Hello from Spring Boot!", controller.hello());
    }
}
