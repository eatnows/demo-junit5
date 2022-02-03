package me.eatnows.demojunit5;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;

import java.lang.reflect.Executable;
import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StudyTest {

    @FastTest
    @DisplayName("스터디 만들기 fast")
    void create_new_study() {
        String test_env = System.getenv("TEST_ENV");
        System.out.println("test_env = " + test_env);
        assumeTrue("LOCAL".equalsIgnoreCase(test_env));

        // 해당 조건을 만족하면 {} 코드를 실행
        assumingThat("local".equalsIgnoreCase(test_env), () -> {
            System.out.println("local");
            Study study = new Study(100);
            assertThat(study.getLimit()).isGreaterThan(0);
        });
        assumingThat("dev".equalsIgnoreCase(test_env), () -> {
            System.out.println("dev");
            Study study = new Study(10);
            assertThat(study.getLimit()).isGreaterThan(0);
        });

//        Study study = new Study(10);
//        assertAll(
//                () -> assertNotNull(study),
//                () -> assertEquals(StudyStatus.DRAFT, study.getStatus(), () -> "스터디를 처음 만들면 DRAFT 상태다."),
//                () -> assertTrue(study.getLimit() > 0, "스터디 최대 참석 가능 인원은 0보다 커야한다.")
//        );
    }

    @SlowTest
    @DisplayName("exception이 발생하는지를 테스트")
    void assertThrowsTest() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new Study(-10));

        assertEquals("limit은 0보다 커야 한다.", exception.getMessage());
    }
    
    @Test
    @DisplayName("시간이내에 완료되는지 테스트")
    @EnabledIfEnvironmentVariable(named = "TEST_ENV", matches = "local")
    void assertTimeoutTest() {
        assertTimeoutPreemptively(Duration.ofMillis(100), () -> {
            new Study(10);
            Thread.sleep(300);
        });
        // TODO ThreadLocal
    }

    @Test
    @DisplayName("스터디 만들기2")
    void create_new_study_again() {
        System.out.println("StudyTest.create1");
    }

    @BeforeAll // 테스트를 실행하기전에 딱 한번 실행됨, 반드시 static 메서드를 사용해야한다.
    static void beforeAll() {
        System.out.println("before all");
    }


    @AfterAll // 모든 테스트가 실행된 이후 딱 한번 실행됨 static 메서드만 가능
    static void afterAll() {
        System.out.println("after all");
    }

    @BeforeEach // 모든 테스트를 실행할 때 각각의 테스트를 실행하기 이전에 실행됨.
    void beforeEach() {
        System.out.println("Before each");
    }

    @AfterEach // 모든 테스트를 실행할 때 각각의 테스트를 실행한 후 실행됨.
    void afterEach() {
        System.out.println("After each");
    }
}