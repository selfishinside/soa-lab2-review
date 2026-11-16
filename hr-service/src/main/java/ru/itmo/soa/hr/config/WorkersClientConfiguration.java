package ru.itmo.soa.hr.config;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.itmo.soa.hr.client.WorkersClient;

@Configuration
public class WorkersClientConfiguration {

    @Bean
    public RestClient workersRestClient() throws Exception {
        HttpClient.Builder httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER);

        configureTrustStore(httpClient);

        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient.build()))
                .build();
    }

    @Bean
    public WorkersClient workersClient(RestClient workersRestClient) {
        URI baseUri = URI.create(setting(
                "workers.base.url",
                "WORKERS_BASE_URL",
                "https://localhost:8181/api/v1"
        ));

        if (!"https".equals(baseUri.getScheme())) {
            throw new IllegalArgumentException("workers.base.url must use HTTPS");
        }

        return new WorkersClient(workersRestClient, baseUri);
    }

    private static void configureTrustStore(HttpClient.Builder httpClient) throws Exception {
        String trustStorePath = System.getProperty("workers.truststore", "");
        if (trustStorePath.isBlank()) {
            return;
        }

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        char[] password = System.getProperty(
                "workers.truststore.password",
                ""
        ).toCharArray();

        try (InputStream input = Files.newInputStream(Path.of(trustStorePath))) {
            trustStore.load(input, password);
        } finally {
            Arrays.fill(password, '\0');
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
        );
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        httpClient.sslContext(sslContext);
    }

    private static String setting(
            String property,
            String environment,
            String fallback
    ) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environment);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
