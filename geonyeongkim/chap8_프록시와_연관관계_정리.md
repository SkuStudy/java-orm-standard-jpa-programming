# 프록시와 연관관계 정리

이번 장에서는 `프록시, 즉시 로딩, 지연 로딩, 영속성 전이, 고아 객체`에 대해 알아봅니다.

## 8.1. 프록시

엔티티를 조회할 때 연관된 엔티티들이 항상 사용되는것은 아닙니다.
때문에, JPA는 연관 엔티티를 즉시 로딩하는 것이 아니라 실제 사용될때 DB에서 조회하도록 `지연로딩`이라는 기능을 제공합니다.

그럼, 프록시 기술은 언제 사용하는 걸까요?
JPA는 이 지연로딩을 위해 실제 엔티티가 아닌 가짜 엔티티를 반환하며 이 가짜 엔티티가 프록시 기술을 적용한 엔티티입니다.
> 프록시란, 실제 로직을 handling하기 위한 기법입니다.
> spring의 인터셉트가 대표적인 예입니다.

### 1. 프록시 기초

엔티티를 실제 사용하는 시점까지 DB 조회를 미루고 싶은 경우 `EntityManager.getReference()` 를 사용하면 됩니다.

이 메소드를 통해 반환되는 엔티티 객체는 `프록시 객체` 입니다.
이 프록시 객체는 실제 객체에 대한 참조를 보관하고 있어 프록시 객체의 메소드를 호출하면 실제 객체의 메소드를 대신 호출하여 반환해주는 구조입니다.

<b> 프록시 객체의 초기화 </b>
실제 엔티티가 사용될 떄, DB를 조회하여 실제 엔티티 객체를 생성하는 작업을 `프록시 객체의 초기화`라고 합니다.

아래는 프록시 초기화 작업의 예상 코드와 그림입니다.

```java
@Getter
public class Member {
    private String name;
}
```

```java
public class MemberProxy extends Member {

    Member target = null;

    public String getName() {
        
        if (target == null) this.target = //DB 조회
        return this.target.getName();
    }

}
```

```java
Member member = em.getReference(Member.class, "id1");
member.getName();
```
<br>

![image](https://user-images.githubusercontent.com/31622350/97247413-f8711200-1842-11eb-8e18-981fbd7f9345.png)


프록시는 아래와 같은 특징을 가지고 있습니다.

- 프록시 객체는 처음 사용할때 한번만 초기화.
- 프록시 객체를 초기화한다고 프록시 객체가 실제 엔티티로 바뀌는것은 아님.
- 프록시 객체는 원본 엔티티를 상속받은 구조이기에 타입 체크 시 주의
- 영속성 컨텍스트에 find할 엔티티가 있다면, getReference 메서드 호출하더라도 프록시가 아닌 원본 엔티티를 반환
- 초기화는 영속성 컨텍스트의 도움을 받아야 가능
    - 때문에 준영속 상태의 프록시를 초기화하려면 예외 발생.


아래는 준영속 상태의 프록시 객체를 초기화하는 코드입니다.

```java
Member member = em.getReference(Member.class, "id1");
transaction.commit();
em.close();

member.getName(); // org.hibernate.LazyInitializationException 발생
```

<br>

### 2. 프록시와 식별자

프록시 객체는 식별자 값을 보관합니다.

때문에, 아래와 같은 코드에서는 초기화를 하지 않습니다.

```java
Team team = em.getReference(Team.class, "team1");
team.getId(); // 초기화 X
```

다만, 엔티티 접근방식을 `FIELD`인 경우에는 JPA가 getId 메서드에 대해 모르니 초기화하게 됩니다.ㄴ
> 추가로, 연관관계를 설정할때는 접근방식이 FIELD 여도 프록시 객체를 초기화하지 않습니다.


### 3. 프록시 확인

프록시 객체의 초기화 여부를 확인하려면 `PersistenceUnitUtil.isLoaded(Object entity)`를 사용하면 됩니다.
> 프록시 객체인지 진짜 엔티티인지 확인하려면 디버깅을 통해 확인하면 됩니다.


## 8.2 즉시 로딩과 지연 로딩

JPA 는 성능 최적화를 위해 개발자에게 2가지 로딩 기법을 제공합니다.

- 즉시 로딩: 엔티티를 조회할 때 연관된 엔티티도 함께 조회
    - 사용법 : @ManyToOne(fetch = FetchType.EAGER)
- 지연 로딩: 연관된 엔티티를 실제 사용할 때 조회
    - 사용법 : @ManyToOne(fetch = FetchType.LAZY)

### 1. 즉시 로딩

즉시 로딩은 말그대로 엔티티 조회 시 연관 엔티티들까지 같이 즉시 조회해서 반환하는것을 말합니다.

JPA 구현체는 대부분 `많은 sql이 날라가는것을 방지하고자 가능하면 조인쿼리`를 사용합니다.

<b> NULL 제약조건과 JPA 조인 전략 </b>

JPA는 외래키 설정이 nullable하면 outer join을 수행하며 nullable하지 않으면 inner join을 수행합니다.

db는 당연하게도 inner join이 성능이 우수합니다. 
때문에, 비즈니스상 외래키가 null이 될 수 없다면 명시적으로 아래와 같이 JPA에게 nullable 정보를 주어야 합니다.

```java
@Entity
public class Member {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TEAM_ID", nullable = false)
    private Team team;
}
```

> default가 true니 주의하세요.

### 2. 지연 로딩

지연로딩은 실제 연관 엔티티를 사용할 시점에 DB에서 조회하기 때문에 지연로딩이라고 합니다.

## 8.3 지연 로딩 활용

아래와 같은 모델 구조와 분석이 있다고 가정하겠습니다.

![image](https://user-images.githubusercontent.com/31622350/97249624-9ff04380-1847-11eb-9f93-6ef5e3ac6d89.png)

- Member와 연관된 Team은 자주 함께 사용됨
- Member와 연관된 Order는 가끔 사용됨

해당 구조를 JPA로 매핑하게 되면 아래와 같은 구조가 됩니다.

```java
@Getter @Setter
@Entity
public class Member {
    
    @Id
    private String id;
    private String username;
    private int age;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Team team;
    
    @OneToMany(fetch = FetchType.LAZY)
    private List<Order> orderList;
}
```

### 1. 프록시와 컬렉션 래퍼

이 때, 유의해서 볼 부분은 `페치 전략을 LAZY로 한 List<Order>` 입니다.

JPA의 구현체인 하이버네이트는 엔티티를 영속화할 때 컬렉션이 있으면, 이를 추적 및 관리하기 위해 
하이버 네이트 내장 컬렉션으로 변경하며, 이를 컬렉션 래퍼라고 합니다.

이 컬렉션 레퍼는 FetchType.LAZY 시 지연 로딩 기능을 제공합니다.

### 2. JPA 기본 페치 전략

JPA의 기본 페치 속성값은 아래와 같습니다.

- @ManyToOne, @OneToOne: 즉시 로딩
- @OneToMany, @ManyToMany: 지연 로딩

책에서 추천하는 방법은 `모든 연관관계를 지연로딩, 필요한 곳만 즉시로딩으로 최적화` 입니다.

### 3. 컬렉션에 FetchType.EAGER 사용 시 주의점

컬렉션에 FetchType.EAGER 사용 시 주의점은 아래와 같습니다.

- 컬렉션을 하나 이상 즉시 로딩하는것은 권장하지 않음
    - 일대다 조인의 경우 결과 데이터가 무수히 많을 수 있기 때문입니다.

- 컬렉션 즉시 로딩은 항상 외부 조인을 사용


## 8.4 영속성 전이: CASCADE

영속성 전이란 `특정 엔티티를 영속 상태로 만들 떄 연관된 엔티티도 함께 영속 상태로 만드는 것입니다.`
JPA에서는 이 영속성 전이를 `CASCADE` 옵션으로 제공합니다.

### 1. 영속성 전이: 저장

아래는 영속성 전이를 활용해 저장하는 예제 코드입니다.

```java
@Entity
public class Parent {
    
    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    private long id;

    private String name;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    private List<Child> childList = new ArrayList<>();
}
```

```java
private static void saveWithCascade(EntityManager em) {
    Child child1 = new Child();
    Child child2 = new Child();
    
    Parent parent = new Parent();
    child1.setParent(parent);
    child2.setParent(parent);
    
    parent.getChildList().add(child1);
    parent.getChildList().add(child2);
    
    em.persist(parent);
}
```

위 예제 코드에서 보시는것과 같이 `CascadeType.PERSIST`로 하면 
부모 엔티티만 persist 하더라도 연관된 자식 엔티티들도 같이 persist하게 됩니다.

이 영속성 전이는 연관관계 매핑과는 아무 관련이 없으며, 
단지 `엔티티를 영속화할때 연관된 엔티티도 같이 영속화할 수 있는 편리함을 제공`합니다.

### 2. 영속성 전이: 삭제

엔티티를 영속상태로 만드는거 외에도 영속성 전이를 사용하여 삭제상태로도 만들 수 있습니다.

이런 경우에는 `CascadeType.REMOVE` 로 하면 됩니다.

### 3. CASCADE의 종류

CASCADE는 아래와 같은 종류들이 있습니다.

- ALL
- PERSIST
- MERGE
- REMOVE
- REFRESH
- DETACH

이런 CASCADE 속성은 아래와 같이 여러개를 같이 사용할 수도 있습니다.

```java
cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
```

## 8.5 고아 객체

JPA는 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제하는 기능을 제공하며, 이를 `고아 객체 제거`라고 합니다.

아래는 고아 객체 제거 기능의 한 예제 코드입니다.

```java
@Entity
public class Parent {

    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    private long id;

    private String name;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    private List<Child> childList = new ArrayList<>();
}
```
```java
Parent parent1 = em.find(Parent.class, id);
parent1.getChildList().remove(0);
```

```sql
DELETE FROM CHILD WHERE ID = ?
```

예제 코드에서 보듯이 `orphanRemoval = true` 로 설정으로 인해
컬렉션에서 자식 엔티티를 제거함을 DB에도 삭제되도록 쿼리가 생성된것을 볼 수 있습니다.

이 고아 객체 제거 기능은 영속성 컨텍스트를 `플러시할때` 적용됩니다.

이 고아객체 제거는 참조하는곳이 하나일 때만 사용해야 하며, 다른 곳에서도 참조하는 부분이 있다면 문제가 될 수 있습니다.


## 8.6 영속성 전이 + 고아 객체, 생명주기

`CascadeType.ALL + orphanRemoval = true`를 동시에 사용하면 
부모 엔티티에 자식엔티티만 추가 및 삭제하면 자동으로 영속화 및 삭제 기능이 동작됩니다.