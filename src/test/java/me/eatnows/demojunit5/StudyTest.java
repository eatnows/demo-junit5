package me.eatnows.demojunit5;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class StudyTest {

    @Test
    void create() {
        Study study = new Study();
        assertNotNull(study);
        System.out.println("StudyTest.create");
    }

    @Test
    @Disabled // 테스트를 실행하고 싶지 않다.
    void create1() {
        System.out.println("StudyTest.create1");
    }

    @BeforeAll // 테스트를 실행하기전에 딱 한번 실행됨, 반드시 static 메서드를 사용해야한다. (private은 안됨)
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