package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student student(boolean active, List<String> taken) {
        return new Student("S1", "s1@must.edu.mn", active, taken);
    }

    // A1
    @Test
    void register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of("CS101")));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), anyString());
    }

    // A2
    @Test
    void register_unknownStudent_returnsNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent("S9")).thenReturn(null);

        String result = service.register("S9", "CS201");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3
    @Test
    void register_inactiveStudent_returnsInactiveAndNeverTouchesEmail() {
        when(repository.findStudent("S1")).thenReturn(student(false, List.of()));

        String result = service.register("S1", "CS201");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
    }

    // A4
    @Test
    void register_missingPrerequisite_returnsPrerequisitesAndDoesNotSave() {
        when(repository.findStudent("S1")).thenReturn(student(true, List.of()));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        String result = service.register("S1", "CS201");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }
}
