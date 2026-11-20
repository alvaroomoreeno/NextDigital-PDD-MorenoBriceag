package com.breadhardit.travelagencykata.infrastructure.rest.mapper;

import com.breadhardit.travelagencykata.domain.Customer;
import com.breadhardit.travelagencykata.infrastructure.rest.dto.GetCustomerDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerRestMapperTest {

    private final CustomerRestMapper mapper = new CustomerRestMapper();

    @Test
    void shouldMapCustomerFieldsToResponseDto() {
        LocalDate birthDate = LocalDate.of(1990, 5, 20);
        Customer customer = Customer.builder()
                .id("customer-123")
                .name("Ada")
                .surnames("Lovelace")
                .birthDate(birthDate)
                .passportNumber("P123456")
                .build();

        GetCustomerDTO result = mapper.toDto(customer);

        assertAll(
                () -> assertEquals("Ada", result.getName()),
                () -> assertEquals("Lovelace", result.getSurnames()),
                () -> assertEquals(birthDate, result.getBirthDate()),
                () -> assertEquals("P123456", result.getPassportNumber())
        );
    }
}
