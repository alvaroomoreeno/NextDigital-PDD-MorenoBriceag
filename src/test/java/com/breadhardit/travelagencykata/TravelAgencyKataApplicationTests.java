package com.breadhardit.travelagencykata;

import com.breadhardit.travelagencykata.infrastructure.persistence.repository.CustomersJPARepository;
import com.breadhardit.travelagencykata.infrastructure.rest.CustomersController;
import com.breadhardit.travelagencykata.infrastructure.rest.dto.PutCustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.Objects;

@SpringBootTest
@Slf4j
@DirtiesContext
class TravelAgencyKataApplicationTests {
    @Autowired
    CustomersController customersController;

    @Autowired
    CustomersJPARepository customersJPARepository;

    @BeforeEach
    public void beforeEach() {
    }

    @Test
    void contextLoads() {
        log.info("Context Loaded");
    }

    @Test
    void givenAUserWhenCreatedThenOK() {
        var putCustomerResponse = customersController.putCustomer(
                PutCustomerDTO.builder().name("Pepe").surnames("Perez")
                        .birthDate(LocalDate.now()).passportNumber("123").build()
        );

        Assertions.assertEquals(HttpStatus.CREATED, putCustomerResponse.getStatusCode());
        Assertions.assertTrue(putCustomerResponse.getHeaders().containsKey(HttpHeaders.LOCATION));

        String location = Objects.requireNonNull(putCustomerResponse.getHeaders().getLocation()).getPath();
        String id = location.substring(location.lastIndexOf("/") + 1);

        Assertions.assertTrue(customersJPARepository.findById(id).isPresent());

        var getCustomerResponse = customersController.getCustomer(id);
        Assertions.assertEquals(HttpStatus.OK, getCustomerResponse.getStatusCode());
        Assertions.assertTrue(getCustomerResponse.hasBody());

        var getCustomerByPassportResponse = customersController.getCustomers("123");
        Assertions.assertEquals(HttpStatus.OK, getCustomerByPassportResponse.getStatusCode());
        Assertions.assertTrue(getCustomerByPassportResponse.hasBody());
    }

    @Test
    void givenNonExistingUserThen404() {
        var getCustomerResponse = customersController.getCustomer("POTATO");
        Assertions.assertEquals(HttpStatus.NO_CONTENT, getCustomerResponse.getStatusCode());
    }
}
