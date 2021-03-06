package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.consultation.Consultation;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.users.User;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@SpringBootTest
@Slf4j
class ConsultationControllerTest {


    @InjectMocks
    ConsultationController consultationController;

    @Mock
    TestRequestUpdateService testRequestUpdateService;

    @Mock
    TestRequestRepository testRequestRepository;


    @Autowired
    TestRequestQueryService testRequestQueryService;

    @Mock
    UserLoggedInService userLoggedInService;


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status_1(){

        // Arrange
        String emailUser = "email-test@domain.com";
        Long id = 1L;
        User loggedUser = new User();
        loggedUser.setEmail(emailUser);

        TestRequest expectedResponseTestRequest = new TestRequest();
        expectedResponseTestRequest.setRequestId(id);
        expectedResponseTestRequest.setStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        expectedResponseTestRequest.setEmail(emailUser);

        // Returns my logged user
        when(userLoggedInService.getLoggedInUser()).thenReturn(loggedUser);

        // Force returns the expected Response
        when(testRequestUpdateService.assignForConsultation(id, loggedUser)).thenReturn(expectedResponseTestRequest);


        // Act
        TestRequest response = this.consultationController.assignForConsultation(id);


        // Assert
        assertThat(response).isNotNull();
        assertThat((String) response.getEmail()).isEqualTo(emailUser);
        assertThat(response.getRequestId()).isEqualTo(id);
        assertThat(response.getStatus()).isEqualTo(RequestStatus.DIAGNOSIS_IN_PROCESS);

    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status_2(){

        // Arrange
        String emailUser = "email-test@domain.com";
        Long id = 1L;
        User loggedUser = new User();
        loggedUser.setEmail(emailUser);

        when(userLoggedInService.getLoggedInUser()).thenReturn(loggedUser);

        // Act
        this.consultationController.assignForConsultation(id);

        // Assert
        verify(testRequestUpdateService,times(1)).assignForConsultation(id, loggedUser);

    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception(){

        // Arrange
        Long InvalidRequestId= -34L;
        String emailUser = "email-test@domain.com";

        User loggedUser = new User();
        loggedUser.setEmail(emailUser);

        TestRequest expectedResponseTestRequest = new TestRequest();
        expectedResponseTestRequest.setRequestId(InvalidRequestId);
        expectedResponseTestRequest.setStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        expectedResponseTestRequest.setEmail(emailUser);

        // Returns my logged user
        when(userLoggedInService.getLoggedInUser()).thenReturn(loggedUser);

        when(testRequestUpdateService.assignForConsultation(InvalidRequestId, loggedUser))
                .thenThrow(new AppException("Invalid ID or State"));

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            this.consultationController.assignForConsultation(InvalidRequestId);
        });

        // Assert
        assertThat(exception.getMessage()).contains("Invalid ID");

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details(){

// Arrange
        String emailUser = "email-test@domain.com";
        Long id = 1L;
        User loggedUser = new User();
        loggedUser.setEmail(emailUser);

        when(userLoggedInService.getLoggedInUser()).thenReturn(loggedUser);

        TestRequest testRequestExpectedResult = new TestRequest();
        testRequestExpectedResult.setRequestId(id);
        testRequestExpectedResult.setStatus(RequestStatus.COMPLETED);

        Consultation consultation = new Consultation();
        DoctorSuggestion expectedSuggestion = DoctorSuggestion.HOME_QUARANTINE;

        consultation.setSuggestion(expectedSuggestion);
        testRequestExpectedResult.setConsultation(consultation);

        CreateConsultationRequest consultationRequest = new CreateConsultationRequest();

        when(testRequestUpdateService.updateConsultation(id, consultationRequest, loggedUser)).thenReturn(testRequestExpectedResult);

        // Act
        TestRequest testRequestResult = consultationController.updateConsultation(id, consultationRequest);


        // Assert
        assertThat(testRequestResult.getRequestId()).isEqualTo(testRequestExpectedResult.getRequestId());
        assertThat(testRequestResult.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(testRequestResult.getConsultation().getSuggestion()).isEqualTo(expectedSuggestion);
    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception(){

        // Arrange
        String emailUser = "email-test@domain.com";
        Long id = -100L;
        User loggedUser = new User();
        loggedUser.setEmail(emailUser);

        when(userLoggedInService.getLoggedInUser()).thenReturn(loggedUser);

        TestRequest testRequestExpectedResult = new TestRequest();
        testRequestExpectedResult.setRequestId(id);
        testRequestExpectedResult.setStatus(RequestStatus.COMPLETED);

        Consultation consultation = new Consultation();
        DoctorSuggestion expectedSuggestion = DoctorSuggestion.HOME_QUARANTINE;

        consultation.setSuggestion(expectedSuggestion);
        testRequestExpectedResult.setConsultation(consultation);

        CreateConsultationRequest consultationRequest = new CreateConsultationRequest();

        when(testRequestUpdateService.updateConsultation(id, consultationRequest, loggedUser))
                .thenThrow(new AppException("Invalid ID or State"));

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            this.consultationController.updateConsultation(id, consultationRequest);
        });

        // Assert
        assertThat(exception.getMessage()).contains("Invalid ID");
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        // Arrange
        String emailUser = "email-test@domain.com";
        Long id = 100L;
        User loggedUser = new User();
        loggedUser.setEmail(emailUser);

        when(userLoggedInService.getLoggedInUser()).thenReturn(loggedUser);

        TestRequest testRequestInput = new TestRequest();

        TestRequest testRequestExpectedResult = new TestRequest();
        testRequestExpectedResult.setRequestId(id);
        testRequestExpectedResult.setStatus(RequestStatus.COMPLETED);

        Consultation consultation = new Consultation();
        DoctorSuggestion expectedSuggestion = DoctorSuggestion.HOME_QUARANTINE;

        consultation.setSuggestion(expectedSuggestion);
        testRequestExpectedResult.setConsultation(consultation);

        CreateConsultationRequest consultationRequest = getCreateConsultationRequest(testRequestInput);
        consultationRequest.setSuggestion(null);

        when(testRequestUpdateService.updateConsultation(id, consultationRequest, loggedUser))
                .thenThrow(new AppException("Invalid ID or State"));

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            this.consultationController.updateConsultation(id, consultationRequest);
        });

        // Assert
        assertThat(exception.getMessage()).contains("Invalid ID");

    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

        CreateConsultationRequest consultationRequest = new CreateConsultationRequest();

        if (testRequest.getStatus().equals(TestStatus.POSITIVE)) {
            consultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
            consultationRequest.setComments("Patient should do quarantine.");
        } else {
            consultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
            consultationRequest.setComments("Ok.");
        }

        return consultationRequest;

    }

}