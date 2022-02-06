package me.eatnows.demojunit5;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.*;

import java.lang.reflect.Executable;
import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@ExtendWith(FindSlowTestExtension.class)
class StudyTest {

    @RegisterExtension
    static FindSlowTestExtension findSlowTestExtension =
            new FindSlowTestExtension(1000L);

    @Order(2)
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

    @Order(1)
    @SlowTest
    @DisplayName("exception이 발생하는지를 테스트")
    void assertThrowsTest() throws InterruptedException {
        Thread.sleep(1000);
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

    @Order(3)
    @DisplayName("스터디 만들기")
    @RepeatedTest(value = 10, name = "{displayName}, {currentRepetition}/{totalRepetitions}") // 테스트에 출력되는 이름을 정해줄 수 있다.
    void repeatTest(RepetitionInfo repetitionInfo) { // RepetitionInfo 인자를 받을 수 잇다.
        System.out.println("test " + repetitionInfo.getCurrentRepetition() + "/"
                + repetitionInfo.getTotalRepetitions());
    }

    @DisplayName("스터디 만들기")
    @ParameterizedTest(name = "{index} {displayName} message = {0}") // 각각 파라미터에 대하여 테스트가 실행된다.
//    @ValueSource(strings = {"날씨가", "많이", "추워지고", "있습니다."}) // 파라미터에 값을 기입
    @ValueSource(ints = {10, 20, 40})
//    @NullAndEmptySource
    void parameterizedTest(@ConvertWith(StudyConverter.class) Study study) {
        System.out.println(study.getLimit());
    }
    // 하나의 아규먼트에 대한 것을 변환시켜준다.
    static class StudyConverter extends SimpleArgumentConverter {

        @Override
        protected Object convert(Object source, Class<?> targetType) throws ArgumentConversionException {
            assertEquals(Study.class, targetType, "Can only convert to Study");
            return new Study(Integer.parseInt(source.toString()));
        }
    }

    @DisplayName("스터디 만들기")
    @ParameterizedTest(name = "{index} {displayName} message = {0}") // 각각 파라미터에 대하여 테스트가 실행된다.
    @CsvSource({"10, '자바 스터디'", "20, 스프링"})
    void parameterizedTest2(ArgumentsAccessor argumentsAccessor) {
        Study study = new Study(argumentsAccessor.getInteger(0), argumentsAccessor.getString(1));
        System.out.println(study);
    }

    @DisplayName("스터디 만들기")
    @ParameterizedTest(name = "{index} {displayName} message = {0}") // 각각 파라미터에 대하여 테스트가 실행된다.
    @CsvSource({"10, '자바 스터디'", "20, 스프링"})
    void parameterizedTest3(@AggregateWith(StudyAggregator.class) Study study) {
        System.out.println(study);
    }

    // 반드시 static inner class 이거나 public class 이어야 한다.
    static class StudyAggregator implements ArgumentsAggregator {

        @Override
        public Object aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext) throws ArgumentsAggregationException {
            return new Study(argumentsAccessor.getInteger(0), argumentsAccessor.getString(1));
        }
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