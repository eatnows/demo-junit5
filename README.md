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


