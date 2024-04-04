package com.sunny.sunnyrpcdemoconsumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.sunny.sunnyrpccore.test.TestZKServer;
import org.sunny.sunnyrpcdemoprovider.SunnyrpcDemoProviderApplication;

@SpringBootTest(classes = {SunnyrpcDemoConsumerApplication.class})
class SunnyrpcDemoConsumerApplicationTests {
    
    static ApplicationContext context1;
    static ApplicationContext context2;
    
    static TestZKServer zkServer = new TestZKServer();
    
    @BeforeAll
    static void init() {
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============     ZK2182    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        zkServer.start();
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8094    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context1 = SpringApplication.run(SunnyrpcDemoProviderApplication.class,
                "--server.port=8094",
                "--kkrpc.zk.server=localhost:2182",
                "--kkrpc.app.env=test",
                "--logging.level.cn.kimmking.kkrpc=info",
                "--kkrpc.provider.metas.dc=bj",
                "--kkrpc.provider.metas.gray=false",
                "--kkrpc.provider.metas.unit=B001"
        );
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8095    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context2 = SpringApplication.run(SunnyrpcDemoProviderApplication.class,
                "--server.port=8095",
                "--kkrpc.zk.server=localhost:2182",
                "--kkrpc.app.env=test",
                "--logging.level.cn.kimmking.kkrpc=info",
                "--kkrpc.provider.metas.dc=bj",
                "--kkrpc.provider.metas.gray=false",
                "--kkrpc.provider.metas.unit=B002"
        );
    }
    
    @Test
    void contextLoads() {
        System.out.println(" ===> aaaa  .... ");
    }
    
    @AfterAll
    static void destory() {
        SpringApplication.exit(context1, () -> 1);
        SpringApplication.exit(context2, () -> 1);
        zkServer.stop();
    }
    
}
