package features;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes= AppConfig.class)
public class IntegrationTest {
    @Autowired
    protected Util util;

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate client;
}