package org.sunny.sunnyrpcdemoprovider;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.sunny.sunnyrpccore.test.TestZKServer;

@SpringBootTest
class SunnyrpcDemoProviderApplicationTests {
    static TestZKServer testZKServer = new TestZKServer();
    @BeforeAll
    static void init(){
        testZKServer.start();
    }
    @Test
    void contextLoads() {
        System.out.println("procider test end;");
    }

    @AfterAll
    static void destroy() throws InterruptedException {
        Thread.sleep(10000);
        testZKServer.stop();
    }
}
