package com.vinsguru.students.controller;

import com.vinsguru.students.dto.StudentRequest;
import com.vinsguru.students.dto.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class StudentControllerIntegrationTest {

    private static final String BASE_URL = "/api/students";

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUpRestTemplate() {
        restTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + port));
    }

    private StudentResponse createStudent(String name, Integer age, String studentClass) {
        StudentRequest request = new StudentRequest(name, age, studentClass);
        return restTemplate.postForEntity(BASE_URL, request, StudentResponse.class).getBody();
    }

    @Test
    @DisplayName("Should create a student and return 201 with the created resource")
    void shouldCreateStudent() {
        StudentRequest request = new StudentRequest("Alice", 20, "10A");

        ResponseEntity<StudentResponse> response = restTemplate.postForEntity(BASE_URL, request, StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Alice");
        assertThat(response.getBody().age()).isEqualTo(20);
        assertThat(response.getBody().studentClass()).isEqualTo("10A");
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when creating a student with invalid data")
    void shouldReturn400WhenCreatingInvalidStudent() {
        StudentRequest request = new StudentRequest("", null, "");

        ResponseEntity<ProblemDetail> response = restTemplate.postForEntity(BASE_URL, request, ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return all students")
    void shouldReturnAllStudents() {
        createStudent("Bob", 21, "11B");
        createStudent("Carol", 22, "12C");

        ResponseEntity<StudentResponse[]> response = restTemplate.getForEntity(BASE_URL, StudentResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return a student by id")
    void shouldReturnStudentById() {
        StudentResponse created = createStudent("Dave", 23, "9D");

        ResponseEntity<StudentResponse> response = restTemplate.getForEntity(BASE_URL + "/" + created.id(), StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Dave");
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when student id does not exist")
    void shouldReturn404WhenStudentNotFound() {
        ResponseEntity<ProblemDetail> response = restTemplate.getForEntity(BASE_URL + "/999999", ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should update an existing student")
    void shouldUpdateStudent() {
        StudentResponse created = createStudent("Eve", 24, "8E");
        StudentRequest updateRequest = new StudentRequest("Eve Updated", 25, "8F");

        ResponseEntity<StudentResponse> response = restTemplate.exchange(
                BASE_URL + "/" + created.id(),
                org.springframework.http.HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                StudentResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Eve Updated");
        assertThat(response.getBody().age()).isEqualTo(25);
        assertThat(response.getBody().studentClass()).isEqualTo("8F");
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when updating a non-existent student")
    void shouldReturn404WhenUpdatingNonExistentStudent() {
        StudentRequest updateRequest = new StudentRequest("Ghost", 30, "1G");

        ResponseEntity<ProblemDetail> response = restTemplate.exchange(
                BASE_URL + "/999999",
                org.springframework.http.HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should delete an existing student")
    void shouldDeleteStudent() {
        StudentResponse created = createStudent("Frank", 26, "7H");

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL + "/" + created.id(),
                org.springframework.http.HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ProblemDetail> getResponse = restTemplate.getForEntity(BASE_URL + "/" + created.id(), ProblemDetail.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when deleting a non-existent student")
    void shouldReturn404WhenDeletingNonExistentStudent() {
        ResponseEntity<ProblemDetail> response = restTemplate.exchange(
                BASE_URL + "/999999",
                org.springframework.http.HttpMethod.DELETE,
                null,
                ProblemDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
