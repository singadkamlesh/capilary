import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public class Foo {
    private final CircuitBreaker circuitBreaker;
    private final Service service;

    public Foo(Service service) {
        this.service = service;
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        this.circuitBreaker = registry.circuitBreaker("foo-cb");
    }

    public String makeRequest() {
        try {
            return circuitBreaker.executeSupplier(() -> service.makeRequest());
        } catch (CircuitBreakerOpenException e) {
            System.out.println("Circuit Breaker is open, requests are blocked");
            return null;
        }
    }

    public CircuitBreaker.State getState() {
        return circuitBreaker.getState();
    }
}

public interface Service {
    String makeRequest();
}

public class Driver {
    public static void main(String[] args) {
        Service service = new ServiceImpl();
        Foo foo = new Foo(service);

        for (int i = 0; i < 10; i++) {
            System.out.println("Foo state: " + foo.getState());
            String result = foo.makeRequest();
            if (result == null) {
                System.out.println("Request blocked by Circuit Breaker");
            } else {
                System.out.println("Service returned: " + result);
            }
        }
    }
}