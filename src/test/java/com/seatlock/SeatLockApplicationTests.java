package com.seatlock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
class SeatLockApplicationTests {

    @Test
    void contextLoads() {
    }
}
