# 객체지향 쿼리 언어

## 10.2 JPQL
엔티티 객체를 조회하는 객체지향 쿼리   

<b>특징</b>   
1. JPQL은 객체지향 쿼리 언어로 테이블이 아닌 엔티티 객체를 대상으로 쿼리한다.
2. SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않는다.
3. 작성한 JPQL도 역시 SQL로 변환된다.

### 1. 기본 문법과 쿼리 API

JPQL도 SQL처럼 SELECT, UPDATE, DELETE문을 사용할 수 있다.
저장을 할때는 EntityManager.persist() 메서드 사용하기 때문에 INSERT문은 없다.

JPQL에서 UPDATE, DELETE 문은 `벌크 연산`이라고 한다.

벌크 연산  
특정 데이터들 또는 전체 데이터들을 한번에 쿼리로 업데이트 해주는 것을 의미한다.   
엔티티를 수정하려면 영속성 컨텍스트의 변경 감지 기능이나 병합을 사용하고 삭제하려면 EntityManager.remove() 메서드를 사용한다.   
하지만 수많은 데이터들을 하나씩 처리하기에는 시간이 너무 오래 걸리기 때문에 executeUpdate 메서드를 이용해서 여러 건을
한 번에 수정하거나 삭제한다.   
```java
public class 벌크연산{
    public void update(){
        String updateString = 
            "update Product p " +
            "set p.price = p.price * 1.1 " +
            "where p.stockAmount < :stockAmount";
    
        int resultCount = em.createQuery(updateString)
                        .setParameter("stockAmount", 10)
                        .executeUpdate();
    }
}

```
단, 벌크 연산을 사용할 때는 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리한다는 점을 주의해야 한다.   
데이터를 SELECT문을 통해서 영속성 컨텍스트에 보관하고 있는 상태에서 벌크 연산을 하면 바로 DB로 쿼리가 날라가기 때문에 영속성 컨텍스트에는 반영이 안된다.   
이 경우 벌크 연산 후 영속성 컨텍스트 초기화를 해주어야 한다.


<b>SELECT 문</b>
```jpaql
SELECT m FROM Member AS m where m.username = 'Hello'
```

- 엔티티와 속성은 대소문자를 구분한다. JPQL 키워드는 대소문자를 구분하지 않는다.
- 위에서 사용한 Member는 클래스 이름이 아니라 엔티티 이름이다. @Entity(name="XXX")로 지정할 수 있고 지정하지 않으면 클래스명을 디폴트로 사용한다.
- 위에서 Member에 m이라는 별칭을 주었는데 JPQL은 별칭을 필수로 사용해야 한다. AS는 생략할 수 있다.

<b>TypedQuery, Query</b>   
작성한 JPQL을 실행하려면 쿼리 객체를 만들어야 한다.
쿼리 객체는 TypedQuery와 Query가 있는데 반환할 타입을 명확하게 지정할 수 있는 경우와 없는 경우에 각각 사용한다.

TypedQuery
```java
TypedQuery<Member> query = 
    em.createQuery("select m from Member m", Member.class);

List<Member> resultList = query.getResultList();

for(Member member : resultList){
    ...
}
```

Query
```java
Query query = 
    em.createQuery("select m.username, m.age from Member m", Member.class);

List<Member> resultList = query.getResultList();
//하나만 조회할 땐
//query.getSingleResult() 메서드 사용

for(Object o : resultList){
    Object[] result = (Object []) o;
    ...
}
```
위의 예제에서는 String 타입과 Integer 타입을 조회하므로 조회 대상 타입이 명확치 않다.
Query는 Object나 Object[]의 형태로 반환이 되므로 TypedQuery를 사용하는 것이 변환이 필요 없어 편리하다.



### 2. 파라미터 바인딩
JDBC는 위치 기준 파라미터 바인딩만 지원하지만 JPQL은 이름 기준 파라미터 바인딩도 지원한다.



<b>이름 기준 파라미터</b>
```java
public class 파라미터_바인딩{
    String usernameParam = "User1";

    TypedQuery<Member> query = 
        em.createQuery("select m from Member m where m.username = :username", Member.class);
            .setParameter("username", usernameParam);
            .getResultList();
}
```

<b>위치 기준 파라미터</b>
```java
public class 파라미터_바인딩{
    String usernameParam = "User1";

    TypedQuery<Member> query = 
        em.createQuery("select m from Member m where m.username = ?1", Member.class);
            .setParameter(1, usernameParam);
            .getResultList();
}
```

쿼리를 파라미터 바인딩하지 않고 문자열 그대로 사용하면 `SQL 인젝션 공격`을 당할 수도 있고 `성능이슈`도 발생할 수 있다.   
파라미터 바인딩 방식을 사용하면 파라미터의 값이 달라도 같은 쿼리로 인식해서 JPA는 JPQL을 SQL로 파싱한 결과를 재사용할 수 있고   
데이터베이스도 내부에서 실행한 SQL을 파싱해서 같은 쿼리는 파싱한 결과를 재사용할 수 있으므로 성능이 향상된다.


### 3. 프로젝션
SELECT 절에 `조회할 대상`을 지정하는 것을 프로젝션이라고 하고 [SELECT {프로젝션 대상} FROM]으로 대상을 선택한다.

<b>엔티티 프로젝션</b>   
```jpaql
SELECT m FROM Member m      // 회원 조회
SELECT m.team FROM Member m // 팀 조회
```
컬럼을 하나하나 나열해서 조회해야하는 SQL과는 달리 원하는 객체를 바로 조회할 수 있다.
참고로 조회한 엔티티는 영속성 컨텍스트에서 관리된다.

<b>임베디드 타입 프로젝션</b>   
임베디드 타입은 엔티티와 거의 비슷하게 사용되는데 임베디드 타입은 조회의 시작점으 될 수 없다는 제약이 있다.
```java
String query = "SELECT a FROM Address a";        //잘못된 예
String query = "SELECT o.address FROM Order o";  //올바른 예
```

임베디드 타입은 엔티티 타입이 아닌 값 타입이다.   
따라서 이렇게 직접 조회한 임베디드 타입은 영속성 컨텍스트에서 관리되지 않는다.   
   

<b>스칼라 타입 프로젝션</b>   
숫자, 문자, 날짜와 같은 기본데이터 타입들을 스칼라 타입이라 한다.
```java
Double orderAmountAvg = 
    em.createQuery("SELECT AVG(o.orderAmount) FROM Order o", Double.class)
            .getSingleResult();        
```


<b>여러 값 조회</b>   
프로젝션에 여러 값을 선택하면 TypedQuery를 사용할 수 없고 Query를 사용해야 한다.
```java
Query query = 
    em.createQuery("SELECT m.username, m.age FROM Member m");
```

<b>NEW 명령어</b>
아까 여러 값을 조회할 때는 TypedQuery를 사용할 수 없었다.
이들을 객체로 만들어서 프로젝션으로 사용할 수 있다.
```java
public class UserDTO{

    private String username;
    private int age;

    public UserDto(String username, int age){
        this.username = username;
        this.age = age;
    }
}

TypedQuery<UserDto> query =
    em.createQuery("SELCT new jpabook.jpql.UserDto(m.username, m.age) FROM Member m", UserDTO.class);

```
단, 주의해야 할 점이 있다.
1. 패키지명을 포함한 전체 클래스 명을 입려해야 한다.
2. 순서와 타입이 일치하는 생성자가 필요하다.


### 4. 페이징 API
페이징 처리용 SQL을 작성하는 일은 반복적일 뿐만 아니라 SQL문법마다 처리하는 방식이 다르다.   
JPA는 페이징을 다음 두 API로 추상화했다.

- setFirstResult(int startPosition) : 조회 시작 위치
- setMaxResult(int maxResult) : 조회할 데이터 수

```java
TypedQuery<Member> query = 
    em.createQuery("SELECT m FROM Member m ORDER BY m.username DESC", Member.class);

query.setFirstResult(10);
query.setMaxResults(20);
query.getResultList();  // 11번부터 20건의 데이터를 조회 ( 11 ~ // 30 )
```

데이터베이스마다 다른 페이징 처리를 같은 API로 처리할 수 있는 것은 데이터베이스 방언(Dialect) 덕분이다.


### 5. 집합과 정렬

집합은 집합함수와 함께 통계 정보를 구할 때 사용한다.
```jpaql
SELECT
    COUNT(m),   // 결과 수 ( 반환타입 : Long )
    SUM(m.age), // 합구하기, 숫자타입만 사용할 수 있음 ( 반환타입 : Long, Double, BigInger 등)
    AVG(m.age), // 평균값, 숫자타입만 사용할 수 있음ㅁ ( 반환 타입 : Double )
    MAX(m.age), //
    MIN(m.age)  // 최대, 최소는  문자, 숫자, 날짜 등에 사용한다.
FROM Member m
```

집합 함수 사용시 참고사항
- NULL 값은 무시한다.
- 값이 없을 때 함수를 사용하면 NULL이 되고 COUNT의 경우에만 0이 된다.
- DISTINCT를 집합 함수 안에서 사용해서 중복된 값을 제고하고 나서 집합을 구할 수 있다.
```jpaql
SELECT COUNT( DISTINCT m.age ) from Member m
```
- DISTINCT를 COUNT에서 사용할 때 임베디드 타입은 지원하지 않는다.   

<b>GROUP BY, HAVING</b>   
GROUP BY는 통계 데이터를 구할 때 특정 그룹끼리 묶어준다.   
HAVING은 GROUP BY와 함께 사용하는데 GROUP BY로 그룹화한 통계 데이터를 기준으로 필터링한다.   
```jpaql
SELECT t.name, COUNT(m.age), SUM(m.age), AVG(m.age), MAX(m.age), MIN(m.age)
FROM Member m LEFT JOIN m.team t
GROUP BY t.name  
HAVING AVG(m.age) >= 10
```
위의 코드는 팀 이름을 기준으로 그룹별로 묶어서 그 중 평균 나이가 10살 이상인 그룹을 조회하는 예제이다.
문법은 아래와 같다.
- group by절 : GROUP BY {단일값 경로 | 별칭}
- having절 : HAVING 조건식


<b>ORDER BY</b>   
ORDER BY는 결과를 정렬할 때 사용한다.
```jpaql
SELECT t.name, COUNT(m.age) AS cnt  
FROM Member m LEFT JOIN m.team t  
GROUP BY t.name  
ORDER BY t.name ASC, cnt DESC
```

문법은 아래와 같다.
- order by절 : ORDER BY {상태필드 경로 | 결과변수 [ASC | DESC]}
상태필드는 t.name처럼 객체의 상태를 나타내는 필드를 말한다.   
  결과변수는 SELECT절에서 나타나는 값, 별칭을 말한다.
  
  
### 6. JPQL 조인 
SQL 조인과 기능은 같고 문법만 약간 다르다.

- 내부조인
내부 조인은 INNER JOIN을 사용한다. ( INNER 생략 가능 )
```jpaql
SELECT m FROM Member m INNER JOIN m.team t
```
JPQL 내부 조인 구문을 보면 SQL의 조인과 약간 다르다.   
JPQL 조인의 가장 큰 특징은 연관 필드(m.team)를 사용한다는 것이다.   
`JPQL 조인을 SQL 조인처럼 사용하면 문법 오류`가 발생한다.
```jpaql
SELECT m FROM Member m INNER JOIN Team t // 잘못된 예
```

만약 서로 다른 타입의 두 엔티티를 조회했다면 TypedQuery를 쓸 수 없다.   
따라서 다음처럼 조회해야 한다.
```java
List<Object[]> list = em.createQuery(jpql).getResultList(); // 위에서 작성한 쿼리

for(Object[] o : list){
    Member m = (Member)o[0];
    Team t = (Team)o[1];
}
```

- 외부조인
OUTER는 보통 생략이 가능해서 보통 LEFT JOIN으로 사용한다.
```jpaql
SELECT m FROM Member m LEFT JOIN m.team t
```

- 컬렉션 조인
일대다 관계나 다대다 관계처럼 컬렉션을 사용하는 곳에 조인하는 것을 컬렉션 조인이라고 한다.   
[회원 -> 팀]으로의 조인은 다대일 조인이면서 단일 값 연관 필드(m.team)를 사용한다.   
[팀 -> 회원]은 반대로 일대다 조인이면서 컬렉션 값 연관 필드(m.members)를 사용한다.
```jpaql
SELECT t, m FROM Team t LEFT JOIN t.members m
```

- 세타 조인
WHERE절을 사용해서 세타 조인 할 수 있다.   
참고로 세타 조인은 내부 조인만 지원한다.
```jpaql
SELECT COUNT(m) FROM Member m, Team t
WHERE m.username = t.name
```

- ON 절
JPA 2.1부터 조인할 때 ON절을 지원한다.   
ON절을 사용하면 조인 대상을 필터링하고 조인할 수 있다.   
참고로 내부 조인의 ON절을 WHERE 절을 사용할 때와 결과가 같으므로 보통 외부 조인에서 사용한다.
```jpaql
SELECT m, t 
FROM Member m LEFT JOIN m.team t
ON t.name = 'A'
```
위 JPQL을 사용하면 아래 SQL 문이 실행되는데 조인 시점에서 조인 대상을 필터링하는 것을 볼 수 있다.
```sql
SELECT m.*, t.*
FROM Member m
LEFT JOIN Team t ON m.team_id = t.id AND t.name = 'A'
```

### 7. 페치 조인
페치 조인은 SQL에서 말하는 조인의 종류가 아니고 JPQL에서 성능 최적화를 위해 제공하는 기능이다.   
이것은 연관된 엔티티나 컬렉션을 한 번에 같이 조회하는 기능인데 `join fetch` 명령어로 사용할 수 있다.   
fetch join : [ LEFT [ OUTER ] | INNER ] JOIN FETCH 조인경로


<b>엔티티 페치 조인</b>   
```jpaql
SELECT m 
FROM Member m INNER JOIN FETCH m.team
```
위처럼 JOIN 뒤에 FETCH를 써주면 연관된 엔티티나 컬렉션을 함께 조회한다.   
일단 JPQL 조인과 다르게 m.team 다음에 별칭이 없는데 페치 조인은 별칭을 사용할 수 없다. ( 하이버네이트는 페치조인에 별칭 허용 )   

실행된 SQL을 보면 SELECT M.*, T.*과 같이 연관된 팀까지 함께 조회한다.   
기조의 INNER JOIN에서 Object[]로 받아야 했던 것과는 달리 Member에 Team이 다 채워진 상태로 리턴된다.   
즉, `객체 그래프를 그대로 유지하면서 받을 수 있는 방법`이다.


<b>컬렉션 페치 조인</b>
일대다 관계에서도 페치 조인을 할 수 있다.
```jpaql
SELECT t
FROM Team t INNER JOIN FETCH t.members
```

![image](https://leejaedoo.github.io/assets/img/collection_fetch.jpg)

주의해야 할점은 컬렉션 페치를 하는 경우 팀은 A 하나이지만 Member와 조인하면서 결과가 증가해서 2건이 조회되었다.   
일대다 조인은 결과가 증가할 수도 있다.   
하지만 일대일, 다대일 조인은 결과가 증가하지 않는다.

페치조인 원리 참조 : https://www.inflearn.com/questions/34797

<b>페치 조인과 DISTINCT</b>   
SQL의 DISTINCT는 중복된 결과를 제거하는 명령어다.   
JPQL의 DISTINCT 명령어는 SQL에 DISTINCT를 추가하는 것은 물론 애플리케이션에서 한 번 더 중복을 제거한다.   

바로 위에서 컬렉션 페치 조인에서 팀 A가 중복으로 조회되었다.
```jpaql
SELECT DISTINCT t
FROM Team t INNER JOIN FETCH t.members
```
이렇게 작성하면 SQL에 DISTINCT가 적용되는데 지금은 애초에 row가 다르므로 효과가 없고   
애플리케이션에서 다시 팀 엔티티 측면에서 중복을 제거한다.


<b>페치 조인과 일반 조인의 차이</b>
그냥 일반 내부 조인은 팀과 회원 컬렉션을 조인해서 조회하였으므로 회원 컬렉션도 조회가 될 것이다라고 생각할 수 있다.   
하지만 SQL은 팀만 조회하고 회원은 전혀 조회하지 않는 쿼리를 날린다.   
`JPQL은 결과를 반환할 때 연관관계까지 고려하지 않는다. 단지 SELECT 절에 설정한 엔티티만 조회할 뿐이다.`   


<b>페치 조인의 특징과 한계</b>
페치 조인을 사용하면 SQL 한 번으로 연관 엔티티들을 함께 조인할 수 있어서 SQL 호출 횟수를 줄여 성능을 최적화 할 수 있다.
```java
@OneToMany(fetch = FetchType.LAZY) // 글로벌 로딩 전략
```
위처럼 선언하는 것을 글로벌 로딩 전략이라고 부르는데 페치조인은 글로벌 로딩 전략보다 우선한다.   
글로벌 로딩 전략을 지연 로딩으로 설정해도 JPQL에서 페치조인을 사용하면 페치 조인이 적용된다.

`최적화를 위해 즉시 로딩 전략을 사용한다면 애플리케이션 전체에서 항상 즉시 로딩이 일어난다.
일부는 빠를 수도 있지만 전체로 보면 사용하지 않는 엔티티를 자주 로딩하므로 오히려 성능에 악영향을 미칠 수 있다.
따라서 지연 로딩을 사용하고 필요한 시점에 페치 조인을 사용하는 것이 효과적이다`

페치 조인은 다음과 같은 한계가 있다.
1. 페치 조인 대상에는 별칭을 줄 수 없다.   
2. 둘 이상의 컬렉션을 페치할 수 없다. 컬렉션 * 컬렉션의 카테시안 곱이 만들어질 수 있다.   
3. 컬렉션을 페치 조인하면 페이징 API를 사용할 수 없다. 일대일이나 다대일은 페치 조인을 써도 페이징 API를 사용할 수 있고 일대다는 불가능하다.   


`페치 조인은 객체 그래프 를 유지할 때 사용하면 효과적이다.    
반면 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야한다면 DTO를 만들어서 반환하는 것이 더 효과적일 수 있다.`

### 8. 경로 표현식
경로 표현식이라는 것은 .(점)을 찍어서 객체 그래프를 탐색하는 것이다.
```jpaql
SELECT m.username
FROM Member m
    INNER JOIN m.team t
    INNER JOIN m.orders o
WHERE t.name = 'TeamA';
```

위 m.username, m.team, m.order, t.name 모두 경로 표현식의 예다.   

- 용어 정리
1. 상태 필드   
단순히 값을 저장하기 위한 필드   
경로 탐색의 끝이다. 더는 탐색할 수 없다.

2. 연관 필드   
연관 관계를 위한 필드, 임베디드 타입 포함   

   - 단일 값 연관 필드   
@ManyToOne, @OneToOne 대상이 엔티티   
```jpaql
SELECT o.member from Order o
```
위 JPQL은 아래와 같이 변환된다.
```sql
SELECT m.*
FROM Order_ o
    INNER JOIN Member m ON o.member_id = m.id
```    
묵시적으로 내부 조인이 일어나서 계속 탐색할 수 있다.
JOIN을 직접 적어주면 명시적 조인, 적어주지 않으면 묵시적 조인이라고 한다.   
묵시적 조인은 내부 조인만 가능하고 외부조인은 명시적 조인을 해야한다.

   - 컬렉션 값 연관 필드   
@OneToMany, @ManyToMany 대상이 컬렉션   
묵시적으로 내부 조인이 일어나고 더는 탐색할 수 없다.   
```jpaql
SELECT t.members.username FROM Team t // 실패
```
단, FROM 절에서 조인을 통해 별칭을 얻으면 별칭으로 탐색할 수 있다.
```jpaql
SELECT m.username
FROM Team t  
INNER JOIN t.members m
```
    

```java
@Entity
public class Member{
    @Id @GeneratedValue
    private Long id;
    
    private String username; // 상태 필드

    @OneToOne
    private Team team; // 단일 값 연관 필드

    @OneToMany
    private List<Order> orders; // 컬렉션 값 연관 필드
}
```

경로 탐색을 사용한 묵시적 조인시 주의사항
- 항상 내부조인이 일어난다.
- 컬렉션은 경로 탐색의 끝이다. 컬렉션에서 경로 탐색을 하려면 명시적으로 조인해서 별칭을 얻어야 한다.
- 경로 탐색은 주로 SELECT, WHERE절에서 사용하지만 묵시적 조인으로 인해 SQL FROM 절에 영향을 준다.


### 9. 서브쿼리

JPQL도 SQL처럼 서브 쿼리를 지원한다.
WHERE, HAVING 절에서만 사용할 수 있고 SELECT, FROM절에서는 사용할 수 없다.
```jpaql
SELECT m
FROM Member m
WHERE m.age > (SELECT AVG(m2.age) FROM Member m2)
```
위는 평균 나이를 넘는 회원 조회하는 예제이다.

<b>서브 쿼리 함수</b>
서브쿼리는 다음 함수들과 함께 사용할 수 있다.

- EXISTS   
문법 : [NOT] EXISTS {서브쿼리}   
설명 : 서브쿼리가 결과에 존재하면 참이다(NOT은 반대)
```jpaql
SELECT m 
FROM Member m 
WHERE EXISTS (
    SELECT t
    FROM m.team t
    WHERE t.name = 'teamA'
)
```

- ALL | ANY | SOME   
문법 : { ALL | ANY | SOME } {서브쿼리}   
설명 : 비교 연산자와 같이 사용한다.
    - ALL : 조건을 모두 만족하면 참
    - ANY, SOME : 둘은 같은 의미로 조건을 하나라도 만족하면 참이다.
```jpaql
SELECT o
FROM Order o
WHERE o.orderAmoun > ALL(
    SELECT p.stockAmoun from Product p
)
```

- IN   
문법 : [NOT] IN {서브쿼리}   
설명 : 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참이다.
```jpaql
SELECT t FROM Team t
WHERE t IN (SELECT t2 FROM Team t2 JOIN t2.members m2 WHERE m2.age >= 20)
```


### 10. 조건식

<b>연산자 우선순위</b>   
1. 경로 탐색 연산자(.)   
2. 수학 연산(+,-,*,/)   
3. 비교 연산자(=, >, <, >=, <=, <>(다름), [NOT] BETWEEN, [NOT] LIKE, [NOT] IN, IS [NOT] NULL, IS [NOT] EMPTY, [NOT] EXISTS)
4. 논리 연산(NOT, AND, OR)   

<b>논리 연산과 비교식</b>   
- 논리연산   
   - AND : 둘다 만족하면 참
   - OR : 둘 중 하나만 만족해도 참
   - NOT : 조건식의 결과 반대
- 비교식   
= | > | >= | < | <= | <>   


<b>BETWEEN, IN, LIKE, NULL 비교</b>
- BETWEEN   
문법 : X [NOT] BETWEEN A AND B   
설명 : X는 A~B 사이의 값이면 참 ( A,B 포함 )   
```jpaql
SELECT m FROM Member m
WHERE m.age BETWEEN 10 AND 20
```

- IN   
문법 : X [NOT] IN (예제)   
설명 : x와 같은 값이 예제에 하나라도 있으면 참   
```jpaql
SELECT m FROM Member m
WHERE m.username IN ('회원1', '회원2')
```

- LIKE   
문법 : 문자표현식 [NOT] LIKE 패턴값 [ESCAPE 이스케이프 문자]   
설명 : 문자 표현식과 패턴값을 비교
   - % : 아무 값들이 입력되어도 된다. 없어도 된다.
   - _ : 한 글자는 아무 값이 입력되어되 되지만 값이 있어야 한다.   
```jpaql
SELECT m FROM Member m
WHERE m.username LIKE '%원%'
```

- NULL 비교식   
문법 : {단일값 경로 | 입력 파라미터 } IS [NOT] NULL   
설명 : NULL인지 비교한다.
```jpaql
WHERE m.username IS NULL
```

<b>컬렉션 식</b>   
컬력션에만 사용될 수 있다.

- 빈 컬렉션 비교식   
문법 : {컬렉션 값 연관 경로} IS [NOT] EMPTY   
설명 : 컬렉션에 값이 비었으면 참   
```jpaql
SELECT m
FROM Member m
WHERE m.orders IS EMPTY
```

- 컬렉션의 멤버 식   
문법 : {엔티티나 값} [NOT] MEMBER [OF] {컬렉션 값 연관경로}   
설명 : 엔티티나 값이 컬렉션에 포함되어 있으면 참   
```jpaql
SELECT t
FROM Team t
WHERE :memberParam MEMBER OF t.members
```


<b>스칼라 식</b>   
스칼라는 숫자, 문자, 날짜, CASE 엔티티 타입 같은 가장 기본적인 타입들을 말한다.

문자함수   
<table style="border-collapse: collapse; width: 100%;" border="1">
<tbody>
<tr><td>함수</td><td>설명</td><td>예제</td></tr>
<tr><td>CONCAT(문자1, 문자2)</td><td>문자를 합한다</td><td>CONCAT(‘A’, ‘B’) = AB</td></tr>
<tr><td>SUBSTRING(문자, 위치[, 길이])</td><td>위치부터 시작해 길이만큼 문자를 구한다. 길이 값이 없으면 나머지 전체 길이를 뜻한다</td><td>SUBSTRING(‘ABCDEF’, 2, 3) = BCD</td></tr>
<tr><td>TRIM([[LEADING | TRAILING | BOTH] [트림 문자] FROM] 문자)</td><td>LEADING : 왼쪽만 TRAILING :  오른쪽만 BOTH : 양쪽다</td><td>TRIM('ABC') = 'ABC'</td></tr>
<tr><td>LOWER(문자)</td><td>소문자로 변경</td><td>LOWER(‘ABC’) = abc</td></tr>
<tr><td>UPPER(문자)</td><td>대문자로 변경</td><td>UPPER(‘abc’) = ABC</td></tr>
<tr><td>LENGTH(문자)</td><td>문자 길이</td><td>LENGTH(‘ABC’) = 3</td></tr>
<tr><td>LOCATE(찾을 문자, 원본 문자[, 검색 시작 위치])</td><td>검색위치부터 문자를 검색한다. 1부터 시작하고 못찾으면 0을 반환한다.</td><td>LOCATE(‘DE’, ‘ABCDEFG’) = 4</td></tr>
</tbody></table>
<b></b>

수학함수
<table style="border-collapse: collapse; width: 100%;" border="1"><tbody>
<tr><td>함수</td><td>설명</td><td>예제</td></tr>
<tr><td>ABS(식수학식)</td><td>절대값을 구한다</td><td>ABS(-10) = 10</td></tr>
<tr><td>SQRT(수학식)</td><td>제곱근을 구한다</td><td>SQRT(4) = 2.0</td></tr>
<tr><td>MOD(수학식, 나눌 수)</td><td>나머지를 구한다</td><td>MOD(4, 3) = 1</td></tr>
<tr><td>SIZE(컬렉션 값 연관 경로식)</td><td>컬렉션의 크기를 구한다</td><td>SIZE(t.members)</td></tr>
<tr><td>INDEX(별칭)</td><td>LIST 타입 컬렉션의 위치값을 구함. 단 컬렉션이 @OrderColumn을 사용하는 LIST 타입일 때만 사용할 수 있다</td><td>t.members m where INDEX(m) &gt; 3</td></tr></tbody></table>


날짜함수
- CURRENT_DATE : 현재날짜   
- CURRENT_TIME : 현재시간   
- CURRENT_TIMESTAMP : 현재 날짜 시간   
*하이버네이트는 날짜 타입에서 년, 월, 일, 시간, 분, 초 값을 구하는 기능을 지원한다.   


<b>CASE 식</b>
- 기본 CASE   
문법 : 
```jpaql
CASE  
    {WHEN <조건식> THEN <스칼라식>}+  
    ELSE <스칼라식>  
END
====================
SELECT
    CASE WHEN m.age <= 10 THEN '학생요금'
         WHEN m.age >= 60 THEN '경로요금'
         ELSE '일반요금'
    END
FROM Member m
```

- 심플 CASE   
문법 :   
```jpaql
CASE <조건대상>  
    {WHEN <스칼라식1> THEN <스칼라식2>}+
    ELSE <스칼라식>
END
====================
SELECT
    CASE t.name
         WHEN '팀A' <= 10 THEN '인센티브110%'
         WHEN '팀B' >= 60 THEN '인센티브120%'
         ELSE '인센티브 105%'
    END
FROM Team t
```

- COALESCE   
문법 : COALESCE(<스칼라식>, {,<스칼라식>}+)   
설명 : 스칼라식을 차례대로 조회해서 null이 아니면 반환한다. IFNULL과 약간 비슷하다.   
```jpaql
SELECT COALESCE(m.usernae, 'nobody') //NULL이면 nobody를 반환해라
FROM Member m
```

- NULLIF   
문법 : NULLIF(<스칼라식>, <스칼라식>)   
설명 : 두 값이 같으면 null 반환, 다르면 첫번째 값을 반환한다.   
```jpaql
SELECT NULLIF(m.username, '관리자') FROM Member m
```

### 11. 다형성 쿼리
JPQL로 부모 엔티티를 조회하면 그 자식 엔티티도 함께 조회한다.

<b>TYPE</b>   
상속 구조에서 조회 대상을 특정 타입으로 한정할 때 사용한다.   
```jpaql
SELECT i // Item 중에 Book과 Movie를 조회하라
FROM Item i
WHERE TYPE(i) IN(Book, Movie)
```
 
<b>TREAT</b>   
상속 구조에서 부모 타입을 특정 타입으로 다룰 때 사용한다.(자바의 `타입 캐스팅`과 비슷하다)   
JPA 표준은 FROM, WHERE절에서만 사용 가능하고, 하이버네이트의 경우 SELECT에서도 가능하다.   
```jpaql
SELECT i
FROM Item i
WHERE TREAT(i as Book).author = 'kim'
```

### 12. 사용자 정의 함수 호출
JPA 2.1부터 사용자 정의 함수를 지원한다.   
문법 : FUNCTION(function_name {, function_arg}*)   
```jpaql
SELECT FUNCTION('group_concat', i.name)
FROM Item i
```
하이버네이트를 사용할 경우 아래와 같이 방언 클래스를 상속해서 사용할 데이터베이스 함수를 미리 등록해야 한다.   
```java
public class MyH2Dialect extends H2Dialect{
    public MyH2Dialect(){
        registerFunction(
            "group_concat", //함수이름
            new StandardFunction("group_concat", StandardBasicTypes.STRING) //하이버네이트의 SQL Function 구현체
        );
    }
}
```
그리고 아래처럼 hibernate.dialect에 해당 방언을 등록해야 한다.
```xml
<property name="hibernate.dialect" value="com.joont.dialect.MyH2Dialect" />
```

하이버네이트 구현체를 사용하면 아래처럼 축약해서 사용할 수 있다.
```jpaql
SELECT group_concat(i.name)
FROM Item i
```


### 13. 기타 정리
- enum은 = 비교 연산만 지원한다.   
- 임베디드 타입은 비교를 지원하지 않는다.   
- JPA 표준은 ''을 길이 0 인 스트링으로 정했지만 데이터 베이스에 따라 다르므로 확인하고 사용해야 한다.   


### 14. 엔티티 직접 사용

<b>기본 키 값</b>
객체 인스턴스는 참조 값으로 식별하고 테이블 로우는 기본 키 값으로 식별하기 때문에
JPQL에서 엔티티 객체를 직접 사용하면 SQL에서는 해당 엔티티의 기본 키 값을 사용한다.   

```jpaql
SELECT COUNT(m) // 엔티티를 직접 사용( 기본 키 값을 사용함 )
FROM Member m

SELECT COUNT(m.id) // 엔티티의 아이디를 사용 
FROM Member m
```

<b>외래 키 값</b>   
외래키 비교도 마찬가지다   
```java
List<Member> result = 
    em.createQuery("SELECT m FROM Member m WHERE m.team = :team")
    .setParameter("team", team) // 엔티티 객체 직접 사용
    .getResultList();
```
위와 같이 실행하면 아래와 같이 실행된다.   
```sql
SELECT m.*
FROM Member m 
WHERE m.team_id = ? 
```
위의 예제에서 MEMBER 테이블은 이미 TEAM의 식별자 값을 가지고 있기 때문에 묵시적 조인은 일어나지 않는다.


### 15. Named 쿼리 : 정적 쿼리
em.createQuery("select ... ") 처럼 JPQL을 직접 문자로 넘기는 것을 동적 쿼리라고 하고,
미리 정의한 쿼리에 이름을 부여해서 해당 이름으로 사용하는 것을 Named 쿼리(정적 쿼리)라고 한다.   

Named 쿼리는 어플리케이션 로딩 시점에 JPQL 문법을 체크하고 미리 파싱해두므로 오류를 빨리 확인할 수 있고, 사용하는 시점에는 파싱된 결과를 재사용하므로 성능상 이점도 있다.   

Named 쿼리는 @NamedQuery 어노테이션을 사용해서 자바 코드에 작성하거나 XML 문서에 작성할 수 있다.   
만약 둘다 설정이 있다면 XML이 우선권을 갖는다.

- 어노테이션에 정의
```java
@Entity
@NamedQueries({
    @NamedQuery(
        name = "Member.findByUsername",
        query = "SELECT m FROM Member WHERE m.username = :username"
    ),
    @NamedQuery(
        name = "Member.count", // 영속성 유닛 단위로 관리되므로 충돌을 방지하기 위해 엔티티 이름을 앞에 주었다. 관리도 용이
        query = "SELECT COUNT(m) FROM Member m"
    )
})
class Member{
    // ...
}
```

- XML에 정의
```xml
<!--xml version="1.0" encoding="UTF-8"?-->
<entity-mappings xmlns="http://java.sun.com/xml/ns/persistence/orm" version="2.0">

    <named-query name="Member.findByUserName">
        <query>
            select m 
            from Member m 
            where m.username = :username
        </query>
    </named-query>

    <named-query name="Member.findByAgeOver">
        <query><![CDATA[
            select m 
            from Member m 
            where m.age > :age
        ]]></query>
    </named-query>


</entity-mappings>
```
XML에서 &, <, >,는 예약문자어 이므로 &amp;, &lt;, &gt;를 사용해야 한다.   
<![CDATA[ ]]>를 사용하면 그 사이에 있는 문자를 그대로 출력하므로 예약 문자도 사용할 수 있다.   

## 10.3 Criteria
Criteria 쿼리는 JPQL을 자바 코드로 작성하도록 도와주는 빌더 클래스 API이다.   

- 장점   
1. 문법 오류를 컴파일 단계에서 잡을 수 있다.   
2. IDE 자동완성 기능을 제공하여 동적 쿼리를 안전하게 생성할 수 있다.   

- 단점
1. 코드가 복잡하고 장황해서 직관적으로 이해하기가 어렵다.    


### 1. Criteria 기초 및 쿼리 생성
```
CriteriaBuilder cb = em.getCriteriaBuilder(); - 1

CriteriaQuery<Member> query = cb.createQuery(Member.class); - 2
        
//검색조건 정의
Predicate usernameEqual = cb.equal(m.get("username"), "회원1");
//정렬조건 정의
Order ageDesc = cb.desc(m.get("age"));

//m이 쿼리 루트고 조회의 시작점이다.
Root<Member> m = query.from(Member.class); - 3
query.select(m); - 4
    
TypedQuery<Member> typeQuery = em.createQuery(query);
    
List<Member> members = typeQuery.getResultList();
```

1. Criteria 쿼리를 생성하려면 먼저 Criteria 빌더를 얻어야 한다.
2. Criteria 쿼리 빌더에서 Criteria 쿼리를 생성한다. 이때 반환타입 지정
3. FROM 절을 생성한다. 반환된 m은 Criteria에서 사용하는 특별한 별칭
4. SELECT 절 생성

### 2. 조회
```java
public interface  CriteriaQuery<T> extends  AbstractQuery<T> { 
    //한건 지정
    CriteriaQuery<T> select(Selection<? extends T> selection);
    //여러건 지정
    CriteriaQuery<T> multiselect(Selection<?> selections);
    CriteriaQuery<T> multiselect(List<Selection<?>> selectionList);
}
```

- DISTINCT   
select / multiselect 다음에 distinct(true)를 사용하면 된다.
```java
/distinct 중복제거.
query.select(m)
     .distinct(true)
     .where(usernameEqual)
     .orderBy(ageDesc);

```

- NEW, construct()   
JPQL에서 SELECT NEW 생성자() 구문을 Criteria에서는 cb.construct(클래스 타입, ...)로 사용한다.
```java
query.select(builder.construct(MemberDTO.class, m.get("username"),m.get("age")) );
```
JPQL에서는 DTO를 쓸때 패키지명까지 모두 썼지만 Criteria는 그럴 필요 없다.   

- 튜플   
Criteria는 Map과 비슷한 튜플이라는 반환 객체를 제공한다.
```java
    CriteriaBuilder cb = em.getCriteriaBuilder();
 
    CriteriaQuery<Tuple> query = cb.createTupleQuery();
    //CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class); -> 위와 같다.
    
    Root<Member> m = query.from(Member.class);
    query.multiselect(m.get("username").alias("username"),m.get("age").alias("age")); // 튜플 별칭

    TypedQuery<Tuple> query2 = em.createQuery(query);
    List<Tuple> resultList = query2.getResultList();
  
    for(Tuple tuple : resultList) {
        //튜플 별칭으로 조회
        System.out.println(tuple.get("username",String.class)+" "+tuple.get("age",Integer.class));
    }
```

### 3. 집합
- GROUP BY
```java
query.groupBy(m.get("team").get("name"));
```
위 코드는 group by m.team, name과 같다.

- HAVING
```java
query.multiselect(m.get("team").get("name"), maxAge, minAge)
    .groupBy(m.get("team").get("name"))
    .having(cb.gt(minAge, 10)); // HAVING
```
위 having(cb.gt(minAge, 10))은 HAVING min(m.age) > 10과 같다.

### 4. 정렬
```java
query.selct(m)
    .where(ageGt)
    .orderBy(cb.desc(m.get("age")));
```

### 5. 조인
조인은 join()메서드와 JoinType클래스를 이용한다.
```java
public enum JoinType{
    INNER,
    LEFT,
    RIGHT
}

//조인
Join<Member, Team> t = m.join("team", JoinType.INNER);

//페치조인
m.fetch("team", JoinType.LEFT);
```

### 6. 서브 쿼리
```java
//서브쿼리 생성
SubQuery<Double> subQuery = mainQuery.subQuery(Double.class);

//메인 쿼리 생성
Root<Member> m = mainQuery.form(Memeber.class);
mainQuery.select(m)
    .where(cb.ge(m.<Integer>get("age"), subQuery));
```

### 7. IN
IN은 Criteria 빌더에서 in(..) 메서드를 사용한다.
```java
query.select(m)
    .where(cb.in(m.get("username"))
        .value("회원1")
        .value("회원2"));
```

### 8. CASE
CASE 식에서는 selectCase() 메서드와 when(), otherwise() 메서드를 사용한다.
```java
query.multiselect(
    m.get("username")
    cb.selectCase()
        .when(cb.ge(m.<Integer>get("age"), 60), 600)
        .when(cb.le(m.<Integer>get("age"), 15), 500)
        .otherwise(1000)
);
```


### 9. 파라미터 정의
JPQL에서 :param처럼 파마티터를 정의했든 Criteria도 할 수 있다.
```java
//정의
query.select(m)
    .where(cb.equal(m.get("username"), cb.parameter(String.class, "usernameParam")));

//바인딩
List<Memeber> resultList = em.createQuery(query)
    .setParameter("usernameParam", "회원1")
    .getResultList();
```

### 10. 네이티브 함수 호출
cb.function(...) 메서드를 사용하여 호출한다.
```java
Root<Member> m = query.from(Memeber.clas);
Expression<Long> function = cb.function("SUM", Long.class, m.get("age"));
query.select(function);
```

riteria의 함수들에 대한 부분은 책을 읽어보는 것이 좋을 듯 하다.
