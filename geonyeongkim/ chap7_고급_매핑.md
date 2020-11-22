# 고급 매핑

## 7.3 복합 키와 식별 관계 매핑

### 1. 식별관계 vs 비식별 관계

식별관계와 비식별관계는 `외래키가 기본키에 포함되는지 여부`에 따라 구분되어 집니다.

<b>식별 관계</b>

부모 테이블의 기본키를 내려받아 자식 테이블의 기본키 + 외래키로 사용하는 관계

<b>비식별 관계</b>

부모 테이블의 기본 키를 받아 자식 테이블의 외래키로만 사용하는 관계

비식별 관계에서는 또 외래키에 NULL 허용 유무에 따라 `필수적 비식별 관계와 선택적 비식별 관계`로 나누어 집니다.

- 필수적 비식별 관계 : 외래키에 NULL 허용 X
- 선택적 비식별 관계 : 외래키에 NULL 허용 O

### 2. 복합 키: 비식별 관계 매핑

기본 키 구성이 하나라면 아래와 같이 단순 매핑이 가능합니다.

```java
@Entity
public class Hello {
    @Id
    private String id;

}
```

그렇다면, 복합키가 기본키의 경우에는 어떻게 해야할까요?
JPA에서는 이런경우 `식별자 클래스`를 별도로 두어 사용해야 합니다.

JPA에서는 식별자 클래스를 기본키로 사용하기 위해 `@IdClass, @EmbeddedId`를 제공합니다.

아래는 @IdClass의 예제 코드입니다.

```java
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ParentId implements Serializable {
    
    private String id1;
    private String id2;
}

```

```java
@Entity
@IdClass(ParentId.class)
public class Parent {
    
    @Id
    @Column(name = "PARENT_ID1")
    private String id1;

    @Id
    @Column(name = "PARENT_ID2")
    private String id2;
    
    
    private String name;
}

```

예제 코드에서 보시는것과 같이 @IdClass를 사용할때는 아래와 같은 조건을 만족해야 합니다.

1. 식별자 클래스의 속성명과 엔티티에서 사용하는 식별자의 속성명이 같아야 합니다. ( ex -> Parent.id1 , ParentId.id1 )
2. Serializable 인터페이스를 구현 O
3. equals, hashCode 구현 O
4. 기본 생성자 O
5. 식별자 클래스 public O


다대일에서 다(N) 테이블에서는 일을 참조하기 위해 @JoinColumn을 사용했었습니다.
그럼, 일이 복합키인 경우에는 어떻게 해야할까요?
이 경우 아래와 같이 @JoinColumns를 사용하시면 됩니다.


```java
@Entity
public class Child {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "PARENT_ID1", referencedColumnName = "PARENT_ID1"),
            @JoinColumn(name = "PARENT_ID2", referencedColumnName = "PARENT_ID2")
    })
    private Parent parent;
}
```

이번에는 @EmbeddedId에 대해서 알아보겠습니다.

이 방법이 @IdClass에 비해서 좀 더 객체지향적인 방법인 방법입니다.

아래는 @EmbeddedId의 예제 코드입니다.

```java
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ParentId implements Serializable {

    @Column(name = "PARENT_ID1")
    private String id1;

    @Column(name = "PARENT_ID2")
    private String id2;
}
```

```java
@Entity
public class Parent {

    @EmbeddedId
    private ParentId parentId;
    private String name;
}
```

예제 코드에서 보시는것과 같이 @EmbeddedId를 사용할때는 아래와 같은 조건을 만족해야 합니다.

1. @Embeddable 어노테이션을 붙여야 합니다.
2. Serializable 인터페이스를 구현 O
3. equals, hashCode 구현 O
4. 기본 생성자 O
5. 식별자 클래스 public O


@IdClass 와 @EmbeddedId 방법 모두 equals, hashCode 구현을 해야합니다.
`이유는 복합키의 경우 영속성 컨텍스트에 저장할 uniqueKey를 기본키 클래스의 equals, hashCode로 사용하기 때문입니다.`

사용하는 부분은 개발자의 취향에 맞게 둘 중 선택해서 사용하면 됩니다.

> 추가로 복합키에는 @GenerateValue를 사용할 수 없습니다.

### 3. 복합 키: 식별 관계 매핑

아래 그림은 `복합 키 + 식별관계`의 한 예입니다.

![image](https://user-images.githubusercontent.com/31622350/97150331-4e957500-17b1-11eb-8748-a8d741754117.png)

CHILD 테이블에서 부모의 PARENT_ID를 자신의 기본키로, GRANDCHILD 테이블에서는 PAREN_ID, CHILD_ID를 자신의 기본키로 사용하고 있습니다.

이러한 경우 방금 살펴본 JPA의 @IdClass, @EmbeddedId 모두 사용할 수 있습니다.

<b>@IdClass와 식별 관계</b>

아래는 @IdClass를 사용하여 식별관계를 매핑한 예제 코드입니다.

```java
@Entity
public class Parent {
    @Id
    @Column(name = "PARENT_ID")
    private String id;

    private String name;
}
```

```java
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChildId implements Serializable {
    private String parent;
    private String childId;
}
```

```java
@Entity
@IdClass(ChildId.class)
public class Child {
    @Id @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Parent parent;

    @Id @Column(name = "CHILD_ID")
    private String childId;

    private String name;
}
```

```java
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GrandChildId implements Serializable {
    private ChildId childId;
    private String id;
}
```

```java
@Entity
@IdClass(GrandChildId.class)
public class GrandChild {
    @Id @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "PARENT_ID"),
            @JoinColumn(name = "CHILD_ID")})
    private Child child;

    @Id @Column(name = "GRANDCHILD_ID")
    private String id;

    private String name;
}
```

예제 코드에서 보듯이 식별관계는 부모의 외래키가 자신의 기본키이기 때문에 `@Id와 연관관계 매핑인 @ManyToOne을 같이 사용`해야 합니다.


<b>@EmbeddedId 식별 관계</b>

@EmbeddedId로 식별관계를 구성할 시에는 @MapsId를 사용해야 합니다.

아래는 @EmbeddedId + @MapsId 를 사용한 예제 코드입니다.

```java
@Entity
public class Parent {
    @Id @Column(name = "PARENT_ID")
    private String id;

    private String name;
}
```

```java
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ChildId implements Serializable {
    private String parentId; // @MapsId("parentId) 사용

    @Column(name = "CHILD_ID")
    private String childId;
}
```

```java
@Entity
public class Child {

    @EmbeddedId
    private ChildId id;

    @MapsId("parentId")
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Parent parent;

    private String name;
}
```

```java
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class GrandChildId implements Serializable {
    private ChildId childId; // @MapsId("childId")

    @Column(name = "GRANDCHILD_ID")
    private String id;
}
```

```java
@Entity
public class GrandChild {

    @EmbeddedId
    private GrandChildId childId;

    @MapsId("childId")
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "PARENT_ID"),
            @JoinColumn(name = "CHILD_ID")})
    private Child child;

    private String name;
}
```

@EmbeddedId의 경우는 예제 코드에서 보는것과 같이 @MapsId를 사용하면 됩니다.
이 어노테이션은 외래키와 매핑한 연관관계를 기본키에도 매핑하겠다는 의미이며, 속성값은 @EmbeddedId를 사용한 식별자 클래스의 필드를 지정하면 됩니다.


### 4. 비식별 관계로 구현

위 Parent, Child, GrandChild의 예제를 비식별 관계로 변경한다면 아래와 같은 테이블 관계도와 예제 코드가 나옵니다.

![image](https://user-images.githubusercontent.com/31622350/97153294-99b18700-17b5-11eb-8e07-f78392766b31.png)

```java
@Entity
public class Parent {
    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    private long id;

    private String name;
}
```

```java
@Entity
public class Child {

    @Id @GeneratedValue
    @Column(name = "CHILD_ID")
    private long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Parent parent;
}
```

```java
@Entity
public class GrandChild {

    @Id @GeneratedValue
    @Column(name = "GRANDCHILD_ID")
    private long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "CHILD_ID")
    private Child child;
}
```

코드에서 보는것과 같이 비식별관계로 설계한다면 식별관계에 비해서 코드가 더욱 간결해지는 장점을 얻을 수 있습니다.

### 5. 일대일 식별 관계

아래는 일대일 식별 관계의 예입니다.

![image](https://user-images.githubusercontent.com/31622350/97153925-99fe5200-17b6-11eb-9b5c-a9ca81de5997.png)

그림과 같이 일대일이기 때문에 부모의 기본키가 복합키가 아니라면 자식 또한 복합키로 할 이유가 없습니다.

JPA를 통하여 위 매핑을 구현하게 되면 아래와 같습니다.

```java
// 부모
@Entity
public class Board {

    @Id @GeneratedValue
    @Column(name = "BOARD_ID")
    private long id;

    private String title;

    @OneToOne(mappedBy = "board")
    private BoardDetail boardDetail;
}
```

```java
// 자식
@Entity
public class BoardDetail {

    @Id
    private long boardId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "BOARD_ID")
    private Board board;

    private String content;
}
```

식별자가 하나라면 예제와 같이 @MapsId 를 사용하고 속성은 비우면 됩니다.

### 6. 식별, 비식별 관계의 장단점

<b> 데이터베이스 설계 관점에서 보면 다음과 같은 이유로 비식별 관계를 선호 </b>

- 식별관계는 부모 테이블의 기본 키를 자식테이블로 전파하면서 자식 테이블의 기본 키 컬럼이 점점 늘어남.
    - 조인 시 SQL이 복잡, 기본 키 인덱스가 불필요하게 커짐.
- 식별관계의경우 2개 이상의 컬럼으로 기본 키를 복합키로 사용하는 경우가 많습니다.
- 식별관계의 기본 키는 주로 비즈니스의미가 있는 컬럼을 조합해서 사용하게 되는데, 비즈니스는 시간이 지남에 따라 바뀌기 때문에 향후 변경하는데에 골치가 아픔.
- 식별 관계는 부모 테이블의 기본키를 자식 테이블의 기본 키로 사용하므로 비식별 관계보다 테이블 구조가 유연 X

<b> 객체 관계 매핑의 관점에서 보면 다음과 같은 이유로 비식별 관계를 선호 </b>

- 복합 관계의 경우 복합 키 클래스를 만들어야하는 번거로움이 생김.
- 비식별 관계의 기본 키는 주로 대리 키를 사용하며, 이 경우 JPA는 @GenerateValue처럼 대리키를 생성하는 방법 제공


`책에서는 처음 테이블 설계 시 비식별 관계로 만드는것을 추천하며, 대리 키의 경우는 long 값을 사용하여 혹시 모를 int의 값을 넘는것을 방지하라고 합니다.
또한, 비식별 관계 역시 선택적 보다는 필수적으로 만들라고 하며, 이유는 선택적의 경우 NULL이 허용되기 때문에 JOIN시 INNER가 아닌 OUTER로 하기 때문입니다.`

## 7.4 조인 테이블

RDB에서 테이블 연관관계를 설계하는 방법은 크게 2가지가 있습니다.

- 조인 컬럼( = 외래 키 )
- 조인 테이블

조인 컬럼은 앞에서 계속 살펴본 외래 키를 뜻합니다.

그럼 조인 테이블은 어떤 걸까요?
말 그대로, 테이블 간에 연관관계를 컬럼이 아닌 테이블로 관리하는것을 뜻합니다.

조인 테이블은 결국 테이블을 더 생성해야하므로, 조회 시 조인 연산이 들어가게 됩니다.
이는 사실상 성능이 나빠지는 원인이 되어 기본은 조인 컬럼의 방식을 사용하는 것이 옳바른 방법입니다.

JPA에서는 @JoinTable 어노테이션을 제공하고 있으며 해당 어노테이션 속성은 아래와 같습니다.

- name: 매핑할 조인 테이블 이름
- joinColumns: 현재 엔티티를 참조하는 외래 키
- inversJoinColumns: 반대방향 엔티티를 참조하는 외래 키

### 1. 일대일 조인 테이블

일대일 관계를 만들려면 조인 테이블의 외래키 컬럼 각각에 총 2개의 유니크 제약조건을 걸어야 합니다.

![image](https://user-images.githubusercontent.com/31622350/97156496-20686300-17ba-11eb-9e62-bbe749fc6b94.png)

```java
//부모
@Entity
public class Parent {
  @Id @GeneratedValue
  @Column(name = "PARENT_ID")
  private Long id;

  private String name;
  
  @OneToOne
  @JoinTable(name = "PARENT_CHILD",
    joinColumns = @JoinColumn(name = "PARENT_ID"),
    inverseJoinColumns = @JoinColumn(name = "CHILD_ID")
  )
  private Child child;
  ...
}

//자식
@Entity
public class ChiId {
  @Id @GeneratedValue
  @Column(name = "CHILD_ID")
  private Long id;

  private String name;

  @OneToOne(mappedBy = "child")
  private Parent parent;
  ...
}
```

### 2. 일대다 조인 테이블

일대다 관계의 경우에는 조인 테이블의 컬럼 중 다(N)와 관련된 컬럼인 CHILD_ID에 유니크 제약조건을 걸어야 합니다.

![image](https://user-images.githubusercontent.com/31622350/97156680-66bdc200-17ba-11eb-88fe-0b8da2552aff.png)

```java
//부모
@Entity
public class Parent {
  @Id @GeneratedValue
  @Column(name = "PARENT_ID")
  private Long id;

  private String name;
  
  @OneToMany
  @JoinTable(name = "PARENT_CHILD",
    joinColumns = @JoinColumn(name = "PARENT_ID"),
    inverseJoinColumns = @JoinColumn(name = "CHILD_ID")
  )
  private List<Child> child = new ArrayList<>();
}

//자식
@Entity
public class ChiId {
  @Id @GeneratedValue
  @Column(name = "CHILD_ID")
  private Long id;
  private String name;

  @ManyToOne(mappedBy = "child")
  private Parent parent;
}
```

### 3. 다대일 조인 테이블

```java
//부모
@Entity
public class Parent {
  @Id @GeneratedValue
  @Column(name = "PARENT_ID")
  private Long id;
  private String name;
  
  @OneToMany(mappedBy = "parent")
  private List<Child> child = new ArrayList<>();
}

//자식
@Entity
public class ChiId {
  @Id @GeneratedValue
  @Column(name = "CHILD_ID")
  private Long id;
  private String name;

  @ManyToOne(optional = false)
  @JoinTable(name = "PARENT_CHILD",
    joinColumns = @JoinColumn(name = "CHILD_ID"),
    inverseJoinColumns = @JoinColumn(name = "PARENT_ID")
  )
  private Parent parent;
}
```

### 4. 다대다 조인 테이블

다대다 관계의 경우 조인 테이블의 두 컬럼을 합해서 하나의 복합 유니크 제약조건을 걸어야 합니다

```java
//부모
@Entity
public class Parent {
  @Id @GeneratedValue
  @Column(name = "PARENT_ID")
  private Long id;
  private String name;
  
  @ManyToMany
  @JoinTable(name = "PARENT_CHILD",
    joinColumns = @JoinColumn(name = "CHILD_ID"),
    inverseJoinColumns = @JoinColumn(name = "PARENT_ID")
  )
  private List<Child> child = new ArrayList<>();
}

//자식
@Entity
public class ChiId {
  @Id @GeneratedValue
  @Column(name = "CHILD_ID")
  private Long id;
  private String name;
}
```

`조인 테이블의 컬럼이 추가되는 케이스에는 @JoinTable을 사용하지 못하며, 별도로 새로운 엔티티를 사용해서 매핑해야 합니다.`

## 7.5 엔티티 하나의 여러 테이블 매핑

JPA는 한 엔티티에 여러 테이블을 매핑할 수 있는 @SecondaryTable 이라는 기능도 제공하고 있습니다.

예로 아래 같은 테이블 설계가 있습니다.

![image](https://user-images.githubusercontent.com/31622350/97157986-31b26f00-17bc-11eb-97b3-04d170a98594.png)

이를 JPA 에서는 아래와 같이 매핑할 수 있습니다.

```java
@Entity
@Table(name = "BOARD")
@SecondaryTable(name = "BOARD_DETAIL", pkJoinColumns = @PrimaryKeyJoinColumn(name = "BOARD_DETAIL_ID"))
public class Board {

    @Id @GeneratedValue
    @Column(name = "BOARD_ID")
    private long id;

    private String title;

    @Column(table = "BOARD_DETAIL")
    private String content;
}
```

@SecondaryTable 을 통해서 BOARD_DETAIL 테이블을 추가 매핑한걸 볼 수 있습니다.

@SecondaryTable의 속성은 아래와 같습니다.

- name: 매핑할 다른 테이블의 이름
- pkJoinColumns: 매핑할 다른 테이블의 기본 키 컬럼 속성

SecondaryTable 에 매핑하고 싶은 컬럼의 경우 예제와 같이 @Column 속성 중 table에 SecondaryTable 명을 지정해 주면 됩니다.

> 더 많은 테이블을 매핑할 땐, @SecondaryTables 를 사용하시면 됩니다.