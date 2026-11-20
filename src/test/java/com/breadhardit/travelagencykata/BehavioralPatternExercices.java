package com.breadhardit.travelagencykata;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BehavioralPatternExercices {

    /*
     * EXERCISE 1 — Strategy
     *
     * A trip requires an identity document for domestic travel, a passport
     * for travel within the Schengen area, and a visa otherwise.
     */
    enum TravelDocument {
        IDENTITY_DOCUMENT,
        PASSPORT,
        VISA
    }

    record Travel(String id, String name, String origin, String destination) {
    }

    interface TravelDocumentationStrategy {
        boolean supports(Travel travel);

        TravelDocument requiredDocument();
    }

    static class SameCountryStrategy implements TravelDocumentationStrategy {
        @Override
        public boolean supports(Travel travel) {
            return travel.origin().equalsIgnoreCase(travel.destination());
        }

        @Override
        public TravelDocument requiredDocument() {
            return TravelDocument.IDENTITY_DOCUMENT;
        }
    }

    static class SchengenTravelStrategy implements TravelDocumentationStrategy {
        private static final Set<String> SCHENGEN_COUNTRIES =
                Set.of("Spain", "France", "Iceland", "Italy", "Portugal");

        @Override
        public boolean supports(Travel travel) {
            return !travel.origin().equalsIgnoreCase(travel.destination())
                    && SCHENGEN_COUNTRIES.contains(travel.origin())
                    && SCHENGEN_COUNTRIES.contains(travel.destination());
        }

        @Override
        public TravelDocument requiredDocument() {
            return TravelDocument.PASSPORT;
        }
    }

    static class VisaRequiredStrategy implements TravelDocumentationStrategy {
        @Override
        public boolean supports(Travel travel) {
            return true;
        }

        @Override
        public TravelDocument requiredDocument() {
            return TravelDocument.VISA;
        }
    }

    static class TravelDocumentationResolver {
        private final List<TravelDocumentationStrategy> strategies;

        TravelDocumentationResolver() {
            // Order matters: the fallback visa strategy must be evaluated last.
            this.strategies = List.of(
                    new SameCountryStrategy(),
                    new SchengenTravelStrategy(),
                    new VisaRequiredStrategy()
            );
        }

        TravelDocument resolve(Travel travel) {
            return strategies.stream()
                    .filter(strategy -> strategy.supports(travel))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No documentation strategy found"))
                    .requiredDocument();
        }
    }

    @Test
    void selectsTheRequiredDocumentForEachTravelType() {
        TravelDocumentationResolver resolver = new TravelDocumentationResolver();

        assertEquals(TravelDocument.VISA,
                resolver.resolve(new Travel("1", "Pyramids tour", "Spain", "Egypt")));
        assertEquals(TravelDocument.PASSPORT,
                resolver.resolve(new Travel("2", "Lisboa tour", "Spain", "Portugal")));
        assertEquals(TravelDocument.IDENTITY_DOCUMENT,
                resolver.resolve(new Travel("3", "Domestic tour", "Portugal", "Portugal")));
    }

    /*
     * EXERCISE 2 — Observer
     *
     * Employee enrollment publishes an event. Interested observers are
     * notified immediately, avoiding a background thread that polls storage.
     */
    record Employee(String id, String name, String email) {
    }

    @FunctionalInterface
    interface EmployeeObserver {
        void onEmployeeEnrolled(Employee employee);
    }

    @FunctionalInterface
    interface GreetingSender {
        void sendGreeting(Employee employee);
    }

    static class EmployeesRepository {
        private final ConcurrentHashMap<String, Employee> employees = new ConcurrentHashMap<>();
        private final CopyOnWriteArrayList<EmployeeObserver> observers = new CopyOnWriteArrayList<>();

        void subscribe(EmployeeObserver observer) {
            observers.add(observer);
        }

        void addEmployee(Employee employee) {
            Employee existing = employees.putIfAbsent(employee.id(), employee);
            if (existing == null) {
                observers.forEach(observer -> observer.onEmployeeEnrolled(employee));
            }
        }
    }

    record GreetingsNotificator(GreetingSender greetingSender) implements EmployeeObserver {
        @Override
        public void onEmployeeEnrolled(Employee employee) {
            greetingSender.sendGreeting(employee);
        }
    }

    @Test
    void notifiesObserversWhenAnEmployeeIsEnrolledWithoutPolling() {
        EmployeesRepository repository = new EmployeesRepository();
        List<String> greetedEmployees = new CopyOnWriteArrayList<>();
        GreetingsNotificator notificator =
                new GreetingsNotificator(employee -> greetedEmployees.add(employee.email()));
        repository.subscribe(notificator);

        Employee pepe = new Employee("1", "Pepe", "pepe@example.com");
        Employee juan = new Employee("2", "Juan", "juan@example.com");
        repository.addEmployee(pepe);
        repository.addEmployee(juan);
        repository.addEmployee(pepe); // Re-inserting the same id must not emit a second event.

        assertEquals(List.of("pepe@example.com", "juan@example.com"), greetedEmployees);
    }
}
