package org.example.nosql_lab1

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "keycloak.auth-server-url=http://localhost:8081",
        "keycloak.internal-auth-server-url=http://localhost:8081",
        "keycloak.realm=test-realm",
        "keycloak.client-id=test-client",
        "keycloak.client-secret=test-secret",
        "spring.datasource.url=jdbc:h2:mem:nosql_lab1;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ],
)
class NosqlLab1ApplicationTests {

    @Test
    fun contextLoads() {
    }

}
