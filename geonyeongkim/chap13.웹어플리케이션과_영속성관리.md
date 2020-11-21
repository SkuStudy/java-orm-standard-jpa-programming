# 웹 어플리케이션과 영속성 관리

이번 장에서는 스프링 컨테이너 환경에서 JPA가 동작하는 내부 동작 방식에 대해 알아보겠습니다.

## 13.1. 트랜잭션 범위의 영속성 컨텍스트

### 1. 스프링 컨테이너의 기본 전략

스프링 컨테이너는 `트랜잭션 범위의 영속성 컨텍스트` 전략을 기본으로 사용합니다.

아래 그림과 같이 영속성 컨텍스트의 생명주기를 트랜잭션과 동일하게 하는 것입니다.

![image](https://user-images.githubusercontent.com/31622350/99870963-426acf00-2c1a-11eb-9144-52e1a6a4895c.png)


때문에, EntityManager가 다르더라도 동일 트랜잭션을 사용중이라면 같은 영속성 컨텍스트를 사용하게 됩니다.

![image](https://user-images.githubusercontent.com/31622350/99871000-95448680-2c1a-11eb-929c-036bfa671c21.png)


> 스프링에서 제공하는 트랜잭션 기능은 쓰레드별로 생기게 제공하고 있어 멀티 쓰레드 환경에서 동일한 영속성 컨텍스트 접근에대한 문제는 생기지 않습니다.

## 13.2. 준영속 상태와 지연로딩

일반적으로 스프링 개발 시 트랜잭션은 서비스 코드에서부터 발생합니다.
때문에, 프레젠테이션 단에서 지연로딩을 사용하려고 하면 에러가 발생합니다.

이 문제를 해결하는 방법은 2가지가 있습니다.

1. 미리 로딩하는 방법
2. OSIV를 통해 영속성 컨텍스트 생명주기 변경 방법


1번인 미리 로딩하는 방법은 추가로 3가지 방법이 있습니다.

- 글로벌 페치 전략 수정
- JPQL 페치 조인
- 강제로 초기화


### 1. 글로벌 페치 전략 수정

말그래도 엔티티 설계 시 FetchType을 EAGER로 하는 방법입니다.

가장 간단하지만 아래와 같은 문제점이 있습니다.

- 사용하지 않은 엔티티를 로딩
- JPQL 사용 시 N + 1 문제 발생


### 2. JPQL 페치 조인

위 N + 1 문제는 `JPQL 페치 조인` 을 통해 해결할 수 있습니다.

JPQL은 JPA의 글로벌 페치 전략을 참고하지 않기 때문에 명시적으로 JPQL 사용 시 fetch 구문을 사용하는 것입니다.

다만, JPQL 페치 조인의 단점은 무분별하게 메서드가 늘어나는 점입니다.

예를 들어, (A, B) 엔티티가 모두 필요한 경우 A 엔티티만 필요한 경우들이 있어
요구사항에 따라 엄청나게 많은 메서드가 생성될 수 있습니다.


### 3. 강제로 초기화

아래와 같이 트랜잭션이 끝나기 전에 미리 프록시 객체를 초기화하는 방법입니다.


```java
class OrderService {
    
    @Transactional
    public Order findOrder(long id) {
        Order order = orderRepository.findOrder(id);
        order.getMember().getName();
        return order;
    }
}
```

하지만, 이 역시 메서드가 늘어나는 단점은 존재하고 서비스 계층 코드가 
비즈니스 처리뿐만이 아닌 프레젠테이션을 위한 일까지 담당하게 됩니다.


### 4. FACADE 계층 추가

이런 부분을 해결하기 위해 일반적인 `controller - service - repository` 구성에서 
중간에 FACADE 계층을 추가하여 `controller - facade - service - repository` 로 만들 수 있습니다.

![image](https://user-images.githubusercontent.com/31622350/99871516-d048b900-2c1e-11eb-9c4c-51f68780931a.png)


FACADE 계층의 역할은 아래와 같습니다.

- 프레젠테이션 계층과 도메인 모델 계층 간의 논리적 의존성 분리
- 프레젠테이션 계층에서 필요한 프록시 객체 초기화
- 서비스 계층을 호출해서 비즈니스 로직 실행
- 리포지토리를 직접 호출해서 뷰가 요구하는 엔티티를 찾음.

하지만, 이 역시 서비스 단에서 프레젠테이션을 위한 로직을 분리한 것 뿐이지
메서드가 무분별하게 늘어나는 단점은 그대로 가지게 됩니다.


## 13.3. OSIV

OSIV는 Open Session In View 로 영속성 컨텍스트를 뷰까지 열어둔다는 의미입니다.

### 1. 요청 당 트랜잭션

영속성 컨텍스트의 생명주기가 늘어나 프레젠테이션 영역에서도 지연로딩을 활용할 수 있게됩니다.

다만, 반대로 변경감지 또한 가능해지기에 노출단에서 엔티티를 통해 DB에 적재하는 부분까지 영향이 갈 수 있다는 단점이 있습니다.

단점을 해소하기 위한 방법으로는 아래와 같이 3가지가 있습니다.

<b>엔티티를 읽기 전용 인터페이스로 제공</b>

```java
interface MemberView {
    public String getName();
}

@Entity 
class Member implements MemberView {

}

class MemberService {
    
    public MemberView getMember(long id) {
        return memberRepository.findById(id);
    }
}
```

<b> 엔티티 레핑 </b>

```java
class MemberWrapper {
    private Member member;

    public MemberWrapper(Member member) {
        this.member = member;
    }

    public String getName();
}
```

<b> DTO만 반환 </b>

```java
class MemberDTO {
    private String name;

    // Getter, Setter
}
```

이 방법들은 단순 반복적인 코드량이 증가한다는 단점이 있습니다.

스프링에서는 이러한 부분을 해소하여 제공하고 있습니다.


### 2. 스프링 OSIV: 비즈니스 계층 트랜잭션

스프링에서는 다양한 OSIV 클래스를 제공하고 있습니다.
> spring-orm.jar 에 있습니다.

스프링에서 제공하는 OSIV는 아래와 같습니다.

![image](https://user-images.githubusercontent.com/31622350/99871838-e9eb0000-2c20-11eb-8c7f-fb9307a0aa29.png)


그림과 같이, 영속성 컨텍스트의 생명주기는 View단 까지지만, 수정의 영역은 트랜잭션 범위로 한정하는 것입니다.

지연 로딩과 같은 기법은 조회용도이기 때문에, 트랜잭션이 필요 없습니다.

때문에, 프레젠테이션단에서 엔티티 값 수정을 통해 데이터가 망가지지 않고
지연로딩과 같은 jpa의 기능을 활용할 수 있습니다.

다만, 스프링 OSIV를 사용하는데에 주의할 점도 있습니다.

아래와 같은 경우에는 데이터가 수정이 가능하게 되기 때문입니다.

```java
class MemberController {

    public String viewMember(long id) {
        Member member = memberService.getMember(id);
        member.setName("asd");
        
        memberService.biz();
        return "view";
    }
}

class MemberService {
    @Transactional
    public void biz(){
        
    }
}
```

![image](https://user-images.githubusercontent.com/31622350/99871985-be1c4a00-2c21-11eb-9949-1c1579939d6c.png)


때문에, 값 변경 전에 미리 트랜잭션 비즈니스 로직을 수행하여야 합니다.

## 13.4. 너무 엄격한 계층

스프링 OSIV를 통해 영속성 컨텍스트 생명이 길어졌기 때문에,

controller에서는 service - repository 접근에서 아래와 같이 다양하게 계층별로 DI를 할수 있습니다.

- controller - service - repository
- controller - repository