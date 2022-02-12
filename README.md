# JUnit 5
JUni 5는 스프링 2.2 이후 버전부터는 스프링 기본 테스트 프레임워크로 내장되어 있기 때문에
따로 의존성을 추가하지 않아도 된다.

JUnit 5는 리플렉션을 사용하여 테스트를 실행하기 때문에 class나 method에 public과 같은 접근제어자를 생략해도 된다.

## 기본적인 Junit 5 애너테이션
- `@Test`: 테스트를 실행할 메서드임을 알리는 애너테이션
- `@BeforeAll`: 모든 테스트를 실행하기전에 딱 한번 실행되는 메서드로 반드시 static 메서드를 사용해야한다.
- `@AfterAll`: 모든 테스트를 실행한 후 딱 한번 실행되는 메서드로 반드시 static 메서드를 사용해야한다.
- `@BeforeEach`: 모든 테스트를 실행할 때 각각의 테스트를 실행하기 이전에 실행되는 메서드
- `@AfterEach`: 모든 테스트를 실행할 때 각각의 테스트를 실행한 후 실행되는 메서드
- `@Disabled`: 모든 테스트를 실행할 때 해당 메서드는 테스트 실행에서 제외시키는 애너테이션 (해당 테스트를 실행할 때는 실행된다.)
- `@DisplayNameGeneration`: class와 method에 사용이 가능하고 테스트 이름 표기 전략을 설정할 수 있다. 기본 구현체로 ReplaceUndersocres를 제공하는데 예로 클래스위에 `@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)`를 사용할 경우 method 이름에 `_`를 공백으로 치환해준다.
- `@DisplayName`: 개별적으로 테스트의 출력 이름을 커스텀하게 설정할 수 있는 애너테이션으로 클래스내 글로벌하게 설정할 수 있는 `@DisplayNameGeneration`보다 우선순위가 높다. 

## Assert
테스트에서 검증하고자 하는 값을 확인하는 JUnit이 제공해주는 메서드이다.
JUnit 5가 지원해주는 메서드들 말고 AssertJ, Hamcrest, Truth 등 다른 라이브러리들을 사용할 수도 있다.

JUnit 5의 assert로 시작하는 많은 메서드를 제공하지만 대표적인것을 알아보자

### assertEquals, assertAll
assertEquals는 실제 값과 기대하는 값이 일치하는지를 비교하는 메서드이다.
```java
public static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
    AssertEquals.assertEquals(expected, actual, messageSupplier);
}
```
첫번째 인자로 기대하는 값을 두번째 인자로 실제값을 넣어주면 된다. 
<br> 3번째 인자로 해당 메서드가 실패했을 경우 출력되는 메시지를 기입할 수 있는데
단순한 `String` 값을 사용할수도 있지만 `Supplier`가 함수형 인터페이스여서 람다식을 사용할 수 있다.
<br> 람다식을 사용하여 메시지를 출력할 경우 테스트가 실패하여 해당 메시지를 출력해야할 때 코드가 실행되어, 하드한 String 메시지를 출력할 때 보다 테스트 실행 성능 이점을 얻을 수 있다고 한다.

테스트를 해야하는 항목이 많을 경우 하나의 테스트가 실패했을 때 (assertEquals 가 실패했을 때 등)
테스트가 끝나버려 그 밑에 하위 Assert 메서드들은 확인할 수가 없다.
<br> 이럴 경우 `assertAll`로 묶어서 테스트하면 실패한 다음 assert 메서드 이후 메서드들도 확인할 수 있다.
```java
assertAll(
    () -> assertNotNull(study),
    () -> assertEquals(StudyStatus.DRAFT, study.getStatus(), () -> "스터디를 처음 만들면 DRAFT 상태다."),
    () -> assertTrue(study.getLimit() > 0, "스터디 최대 참석 가능 인원은 0보다 커야한다.")
);
```
각각 확인 구문 메서드는 람다식으로 실행하여야 한다.


### assertThrows
`assertThrows()`를 사용하면 해당 코드에 `exception`이 발생하는 지 확인할 수 있다.
```java
void assertThrowsTest() {
    IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> new Study(-10));

    assertEquals("limit은 0보다 커야 한다.", exception.getMessage());
}
```


### assertTimeout
`assertTimeout()`를 이용하여 코드가 시간안에 완료되는지를 확인할 수 있다.
```java
assertTimeout(Duration.ofMillis(100), () -> {
    new Study(10);
    Thread.sleep(300);
});
```
해당 구문 확인은 100ms가 넘어가면 실패하지만 `Thread.sleep(300)`으로 300ms 이후에 테스트가 끝나는데,
만약 assert 메서드가 실패했을때 바로 테스트를 종료하고 싶으면 `assertTimeout()` 말고 `assertTimeoutPreemptively()`를 사용하면 된다.
<br> 하지만 해당 메서드는 사용할때 주의하여야 하는데 `ThreadLocal`를 사용하는 코드가 있으면 예상하지 못하는 문제가 발생할 수 있다. 예로 Spring Transaction의 경우 테스트에서 스프링이 제공하는 트랜잭션 설정이 제대로 적용되지 않을 수 있다. 
<br> 트랜잭션 설정을 가지고 있는 쓰레드와 별개의 쓰레드로 코드를 실행하기 때문에 주의해야한다. (롤백이 되지 않고 DB 반영이 되는 문제 등)



### EnableOnOs, DisabledOnOs
테스트를 실행하는 환경이 특정 OS인 경우에 테스트 메소드를 활성화할지 비활성화할지를 정하는 애너테이션으로
테스트 클래스 전체를 실행할 경우 적용되고, 특정 메서드를 실행할 경우에는 적용되지 않는다.
```java
@Test
@DisplayName("exception이 발생하는지를 테스트")
@DisabledOnOs({OS.MAC})
void assertThrowsTest() {
    IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> new Study(-10));

    assertEquals("limit은 0보다 커야 한다.", exception.getMessage());
}
```

### EnableOnJre, DisableOnJre
OS와 마찬가지로 특정 자바버전을 선택해서 활성화할 수 있게 한다.

### EnabledIfEnvironmentVariable, DisabledIfEnvironmentVariable
환경변수가 매치될경우 (비)활성화할 수 있는 애너테이션으로
`assumingThat()` 메서드와 비슷하게 작동한다. 
<br> 대소문자를 구별하기 때문에 주의해야한다.
```java
// 해당 조건을 만족하면 {} 코드를 실행
assumingThat("local".equalsIgnoreCase(test_env), () -> {
    System.out.println("local");
    Study study = new Study(100);
    assertThat(study.getLimit()).isGreaterThan(0);
});
```
 
```java
@Test
@DisplayName("시간이내에 완료되는지 테스트")
@EnabledIfEnvironmentVariable(named = "TEST_ENV", matches = "local")
void assertTimeoutTest() {
    assertTimeoutPreemptively(Duration.ofMillis(100), () -> {
        new Study(10);
        Thread.sleep(300);
    });
}
```

### Tag
테스트 메서드들을 그룹화하여 실행하고싶을때 `@Tag`를 사용할 수 있다.
```java
@Test
@DisplayName("exception이 발생하는지를 테스트")
@Tag("slow")
void assertThrowsTest() {
    IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> new Study(-10));

    assertEquals("limit은 0보다 커야 한다.", exception.getMessage());
}
```
인텔리제이에서는 테스트 설정에서 테스트할 설정을 Tag로 변경하면 된다. (기본값은 Class) <br>
커맨드라인으로 실행하고 싶다면 메이븐의 경우 pom.xml에 값을 추가해야한다.
```xml
<profiles>
    <profile>
        <id>default</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <groups>
                            fast
                        </groups>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    <profile>
        <id>ci</id>
        <build>
            <plugins>
                <plugin>
                    <artifactId>maven-surefire-plugin</artifactId>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```
```text
./mvnw test

./mvnw test -p ci
```
이렇게 xml의 추가를하고 메이븐 테스트를 실행하면 fast의 태그를 가진 메서드만 실행이 된다. <br>
ci의 경우 특정 값을 적지 않았기 때문에 모든 메서드가 실행된다.

Junit5에서 제공하는 애너테이션은 메타 애너테이션으로 커스텀 애너테이션을 사용하여 커스텀 태그를 만들 수 있다.
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test           
@Tag("fast")    
public @interface FastTest {  
    
}
```


### RepeatedTest
테스트 환경에서 반복하여 메서드를 실행할 필요가 있을때 사용하면 좋은 애너테이션이다.
```java
@DisplayName("스터디 만들기")
@RepeatedTest(value = 10, name = "{displayName}, {currentRepetition}/{totalRepetitions}") // 테스트에 출력되는 이름을 정해줄 수 있다.
void repeatTest(RepetitionInfo repetitionInfo) { // RepetitionInfo 인자를 받을 수 잇다.
    System.out.println("test " + repetitionInfo.getCurrentRepetition() + "/"
            + repetitionInfo.getTotalRepetitions());
}
```
기본적으로 애너테이션 파라미터로 value값을 넣어줄수있고, 테스트 결과창에 이름을 설정할수도 있다. 

### ParameterizedTest 
```java
@DisplayName("스터디 만들기")
@ParameterizedTest(name = "{index} {displayName} message = {0}") // 각각 파라미터에 대하여 테스트가 실행된다.
@ValueSource(strings = {"날씨가", "많이", "추워지고", "있습니다."}) // 파라미터에 값을 기입
void parameterizedTest(String message) {
    System.out.println("message = " + message);
}
```
`@ParameterizedTest`를 사용하면 각기 다른 파라미터로 테스트 메서드를 실행할 수 있다. <br> 
파라미터의 값을 주입하는 방법은 여러개가 있지만 해당 코드에서는 `@ValueSource`를 사용하여 값을 주입하여 테스트 메서드가 4번 실행된다.

#### 인자값으로 쓸 수 있는 애너테이션
- `@ValueSource`
- `@NullSource`  null인 값을 추가 
- `@EmptySource` 비어있는 문자열을 하나 추가
- `@NullAndEmptySource` `@NullSource`와 `@EmptySource`를 각각 추가 
- `@EnumSource`
- `@MethodSource`
- `@CsvSource`
- `@CsvFileSource`
- `@ArgumentSource`



### 테스트 인스턴스
기본적으로 JUnit이 테스트를 실행할때 메서드를 실행해야하는데,
테스트 메서드마다 테스트 인스턴스를 새로 만든다. (테스트 메서드를 독립적으로 실행하기 위함)

Junit5에서는 이 기본 전략을 변경할 수 있다. <br> 
테스트 클래스 위의 애너테이션을 추가한다. `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` 클래스마다 인스턴스를 생성하기 때문에 메서드가 모두 하나의 인스턴스를 공유하게 된다. <br>
인스턴스를 클래스당 한 번만 만들게 되면 `@BeforeAll`과 `@AfterAll`을 사용할때도 `static` 메서드가 아니여도 사용이 가능하다.

이 전략을 사용하면 테스트의 순서에서 이점을 얻을수도 있다.
```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudyTest {
    // ...
}
```

### 테스트 순서 
테스트 메서드의 실행순서는 내부적으로 정해져있어서 항상 그 순서대로 실행이 되기는 하지만
테스트 순서의 의존해서는 안 된다. 제대로된 유닛 테스트라면 다른 테스트와 독립적으로 실행이 되어야한다. (테스트간의 의존성이 없어야 한다.)
<br> 하지만 때로는 내가 정한 순서로 테스트해야할 때가 있다. (통합테스트, 시나리오 테스트 등)
어떤 시나리오에서 상태값을 유지하여 테스트 메서드간 의존성이 있어야할 경우 테스트 인스턴스를 매번 만드는것이 아니라 클래스당 인스턴스를 한 번 만들어 상태를 유지하게 만들면 유용하다.

테스트의 순서를 정하고 싶을때는 먼저 테스트 클래스의 `@TestMethodOrder()`를 추가한다. `()`안에는 `MethodOrderer`의 구현체를 넣어줄 수 있다.
- Alphanumeric
- OrderAnnotation
- Random

OrderAnnotation 구현체를 사용하여 설정하면 `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`<br>
테스트 메서드위에 Junit 패키지의 `@Order()` 애너테이션을 추가하여 사용할 수 있다. ()안에는 양수를 넣어줄 수 있고, 낮은 숫자일수록 높은 우선순위를 가진다.
```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudyTest {

    @Order(2)
    @DisplayName("스터디 만들기 fast")
    void create_new_study() {
        // ...
    }

    @Order(1)
    @DisplayName("exception이 발생하는지를 테스트")
    void assertThrowsTest() {
        // ...
    }
}
```


### Junit 설정파일
junit5는 테스트 환경에 필요한 설정들을 할 수 있는 설정파일을 만들 수 있다.<br>
`test` 디렉토리 밑에 `resources` 디텍토리를 생성한 후 그 하위에 `junit-platform.properties`파일을 생성하여 
테스트에 필요한 설정값들을 적용할 수 있다.


### [JUnit 확장 모델](https://junit.org/junit5/docs/current/user-guide/#extensions)
JUnit 4에서는 확장 모델이 `@RunWith(Runner)`, `TestRule`, `MethodRule`로 나뉘어져 있었는데 <br>
JUnit 5에서는 `Extension`이라는 모델하나로 통합되었다.

확장모델 클래스를 먼저 만들고 테스트 클래스에 등록을 해줄때 3가지 방법이 있다.
- 클래스에서 선언하는 방법
- 코드내부에 등록하는 방법
- 자동 등록 자바 ServiceLoader를 이용하는 방법

클래스에 애너테이션을 이용하여 등록하는 방법으로는 
```java
@ExtendWith(FindSlowTestExtension.class)
class StudyTest {
    
}
```

이렇게 테스트 클래스위에 해당 확장모델 클래스를 등록하는 방법이 있다.
이 방법은 확장모델을 디폴트 생성자로 자동으로 생성하기 때문에 확장모델 클래스 생성시 제어가 불가능하다.
<br> 만약 생성할때 디폴트 생성자를 이용하는것이 아니라면 테스트 클래스 코드 내부에 등록하는 방법을 사용해야한다.

```java
class StudyTest {

    @RegisterExtension
    static FindSlowTestExtension findSlowTestExtension =
            new FindSlowTestExtension(1000L);
}
```

이런식으로 테스크 클래스 내부에 직접 확장모델 클래스를 생성하여 사용할 수가 있다.

마지막으로 [ServiceLoader](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html) 를 이용하여 자동 등록하는 방법이 있는데, 자동 등록의 디폴트 값이 false이기 때문에 Junit 설정 파일에서 true로 변경해주어야한다.
`junit.jupiter.extensions.autodetection.enabled = true`
그리고 확장모델을 어떠한 형식에 맞게 작성해주어야 자동등록이 된다. 하지만 이 방법은 내가 원하지 않는 테스트 코드에도 확장 모델일 적용될 가능성이 있어 추천하지 않는다.



### JUnit 5 마이그레이션
JUnit3 혹은 4로 작성된 테스트 코드를 JUnit5에서 실행하거나 마이그레이션 할 수가 있다.
마이그레이션을 하려면 Junit vintage engine 모듈이 있어야 가능하다.
```xml
<dependency>
    <groupId>org.junit.vintage</groupId>
    <artifactId>junit-vintage-engine</artifactId>
    <scope>test</scope>
</dependency>
```
기본적인 테스트 코드는 전부 실행이 된다. 하지만 `@Rule`을 JUnit5에서는 지원하지 않기때문에 junit-jupiter-migrationsupport 모듈이 제공하는 
`@EnableRuleMigrationSupport`를 이용하면 다음 타입의 Rule을 지원한다.
- ExternalResource
- Verifier
- ExpectedException
하지만 공식적으로 지원해주는것이 아니기 때문에 100% 지원한다고는 볼 수 없다.




# Mockito
Mock 이란 진짜 객체와 비슷하게 동작하지만 그 동작을 사용자가 직접 컨트롤할 수 있는 객체를 부른다. 
Mockito 는 이러한 Mock 객체를 생성, 관리, 검증할 수 있는 프레임워크이다. (다른 프레임워크로는 EasyMock, JMock 등이 있다.)

스프링 부트 2.2 버전 이상부터는 spring-boot-starter-test 의존성에 자동으로 Mockito가 추가되어있다.<br>
만약 Mockito가 추가되어 있지 않다면 의존성을 추가해주어야 한다.
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>3.1.0</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>3.1.0</version>
    <scope>test</scope>
</dependency>
```
mockito-junit-jupiter 는 JUnit 테스트에서 Mockito를 연동해서 사용할 수 있는 MockitoExtension을 제공해주는 라이브러리이다.

### Mockito 객체 만들기
만약 StudyService라는 클래스를 테스트하고 싶은데 StudentService와 StudyRepository 객체를 의존하고 있다면 
Mockito를 이용해서 StudentService와 StudyRepository의 Mock객체를 만들어 주입해서 StudyService를 생성할 수 있다.
```java
class StudyServiceTest {
    
    @Test
    void studying() {
        StudentService studentService = Mockito.mock(StudentService.class);
        StudyRepository studyRepository = Mockito.mock(StudyRepository.class);
        
        StudyService studyService = new StudyService(studentService, studyRepository);
    }
}
```
위 코드와 같이 `Mockito.mock()`을 이용해서 StudentService와 StudyRepository 의 객체를 Mock객체로 생성하여 의존성 주입을 할 수 있다. (Mockito.mock() 메서드는 static import 하여 `Mock()`으로 사용할 수 있다.) 
<br> 만약 생성한 Mock 객체가 다른 테스트 메서드에서도 쓰인다면 `@Mock` 애너테이션을 이용하여 필드에서 생성할 수도 있다.

```java
@ExtendWith(MockitoExtension.class)
class StudyServiceTest {
    
    @Mock
    StudentService studentService;
    @Mock
    StudyRepository studyRepository;
    
    @Test
    void studying() {
        StudyService studyService = new StudyService(studentService, studyRepository);
    }
}
```
`@Mock` 애너테이션을 이용하여 객체를 Mock 객체로 생성할 수 있지만 그렇다고 바로 주입되는 것은 아니다.<br>
테스트 클래스위에 `@ExtendWith(MockitoExtension.class)`를 추가해주어야 생성된 Mock객체를 사용하여 의존성 주입을 할 수 있다.

```java
@ExtendWith(MockitoExtension.class)
class StudyServiceTest {

    @Test
    void studying(@Mock StudentService studentService,
                  @Mock StudyRepository studyRepository) {
        StudyService studyService = new StudyService(studentService, studyRepository);
    }
}
```
위 코드처럼 테스트 메서드 파라미터에 선언하게 되면 Mock 객체를 전역이 아닌 메서드 내에서만 생성하여 사용할 수 있다.


# [TestContainers](https://www.testcontainers.org/)
테스트 환경에서 도커 컨테이너를 활용하여 테스트를 실행할 수 있게 도와주는 라이브러리이다.
<br> 테스트에서는 In-memory db를 많이 사용하는데 TestContainers를 이용하면 실제 운영되는 환경과 가까운 테스트를 만들 수 있다.


TestContainers를 사용하려면 의존성을 추가해야한다.
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.16.3</version>
    <scope>test</scope>
</dependency>
```
해당 의존성을 추가하면 `@TestContainers`를 사용할 수 있다. `@TestContainers`는 JUnit 5 Extension으로 
테스트 클래스에 `@Container`를 사용한 필드를 찾아서 컨테이너 라이프사이클 관련 메서드를 실행해준다.

`@Container`는 인스턴스 필드에 사용하면 모든 테스트 마다 컨테이너를 재시작하고, <br>
static 필드에 사용하면 클래스 내부 모든 테스트에서 동일한 컨테이너를 재사용한다.

TestContainers는 여러 모듈을 지원하는데 필요한 모듈에 맞는 의존성을 추가해야주어야 한다.

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.16.3</version>
    <scope>test</scope>
</dependency>
```
위와같이 필요한 모듈의 의존성을 추가해주어야 해당 모듈의 컨테이너 코드를 사용할 수 있다.

```java
class Study {
    // static이 아닐경우 테스트 메서드마다 컨테이너를 재시작한다.
    private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.close();
    }
}
```
기본적으로 해당 모듈의 컨테이너 클래스를 생성하여 사용할 수 있고 `start()` 메서드로 컨테이너를 시작, `close()`로 컨테이너를 종료해주어야한다.
<br> `application.properties`에 database url을 설정했어도 이렇게 사용하면 TestContainers에서는 해당값을 참조하지 못해 랜덤한 값으로 생성하게 된다.<br>
database url에 tc라는 키워드로 TestContainers가 참조할 수 있게 해야한다.
```properties
#1
spring.datasource.url=jdbc:tc:postgresql//localhost:15432/studytest

#2
spring.datasource.url=jdbc:tc:postgresql///studytest
```
이런식으로 tc키워드를 jdbc와 db사이에 입력을 하면 된다. tc를 사용할 경우 host와 port는 중요하지 않기 때문에 생략해도 된다.
```properties
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
```
반드시 driver-class-name을 추가해주어야하는데 tc 키워드가 붙어잇는 url 정보에 해당하는 driver로 testContainers가 제공하는 driver를 사용하게 된다.

```java
@Testcontainers
class Study {
    
    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer()
            .withDatabaseName("studytest"); // databaseName을 설정할 수도 있다.
}
```
직접 container의 라이프사이클을 관리하였지만 junit-jupiter TestContainers 라이브러리에 애너테이션을 사용하면 자동으로 라이프 사이클을 관리해준다.



### 컨테이너 정보를 스프링에서 참조하기
1. TestContainers를 이용하여 컨테이너 생성
2. ApplicationContextInitializer를 구현하여, 생성된 컨테이너에서 정보를 추출, Environment에 넣어준다.
3. @ContextConfiguration을 사용해서 ApplicationContextInlitializer 구현체를 등록한다.
4. 테스트 코드에서 Environment, @Value, @ConfigurationProperties 등 방법으로 해당 프로퍼티를 사용한다.


`@ContextConfiguration` 은 스프링이 제공하는 애너테이션으로 스프링 테스트 컨텍스트가 사용할 설정 파일 또는 컨텍스트를 커스터마이징 할 수 있는 방법을 제공해준다.
<br>`ApplicationContextInlitializer`는 스프링 ApplicationContext를 프로그래밍으로 초기화할 때 사용할 수 있는 콜백 인터페이스로,
특정 프로파일을 활성화하거나 프로퍼티 소스를 추가하는 작업을 할 수 있다.
<br> `TestPropertyValues`는 테스트용 프로퍼티 소스를 정의할 때 사용한다.
`Environment`는 스프링 핵심 API로 프로퍼티와 프로파일을 담당한다.

```java
@Testcontainers
@ContextConfiguration(initializers = StudyTest.ContainerPropertyInitializer.class)
class StudyTest {

    @Container
    static GenericContainer postgreSQLContainer = new GenericContainer()
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "studytest");
    
    // 1. Environment 를 이용하여 값을 사용하는 방법
    @Autowired
    Environment environment;
    
    // 2. @Value 를 이용하여 값을 사용하는 방법
    @Value("${container.port}")
    int port;
    
    // ApplicationContextInitializer를 구현하여 컨테이너의 정보를 Environment에 넣어준다. 
    static class ContainerPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            // =을 기준으로 key value를 적어준다.
            TestPropertyValues.of("container.port=" + postgreSQLContainer.getMappedPort(5432))
                    .applyTo(applicationContext.getEnvironment());

        }
    }
}

```


