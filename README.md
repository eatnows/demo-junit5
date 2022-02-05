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