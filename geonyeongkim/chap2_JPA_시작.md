## 2.5 persistence.xml 설정

JPA는 `META-INF/persistence.xml` 파일을 참조하여 설정 정보를 관리합니다.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence" version="2.1">

    <persistence-unit name="jpabook">

        <properties>

            <!-- 필수 속성 -->
            <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="javax.persistence.jdbc.user" value="sa"/>
            <property name="javax.persistence.jdbc.password" value=""/>
            <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect" />

            <!-- 옵션 -->
            <property name="hibernate.show_sql" value="true" />
            <property name="hibernate.format_sql" value="true" />
            <property name="hibernate.use_sql_comments" value="true" />
            <property name="hibernate.id.new_generator_mappings" value="true" />

            <!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
        </properties>
    </persistence-unit>

</persistence>
```

위 persistence.xml을 보면 JPA 표준 속성, 하이버네이트 속성으로 나눠 볼 수 있습니다.

#### 1. JPA 표준 속성

- javax.persistence.jdbc.driver: JDBC 드라이버
- javax.persistence.jdbc.user: 데이터베이스 접속 아이디
- javax.persistence.jdbc.password: 데이터베이스 접속 비밀번호
- javax.persistence.jdbc.url: 데이터베이스 접속 URL

#### 2. 하이버네이트 속성

- hibernate.dialect: 데이터베이스 방언 설정



### 데이터베이스 방언

데이터베이스 방언( = `dialect`)이란, SQL 표준을 지키지 않거나 특정 데이터베이스만의 고유한 기능을 일컫습니다.

아래 그림과 같이 개발자는 JPA가 제공하는 문법에 맞게 사용한다면, 특정 데이터베이스에 맞는 SQL로 dialect 이 처리하게 됩니다.

![image](https://user-images.githubusercontent.com/31622350/96356152-9d923900-1125-11eb-8040-09444eed8ca9.png)



## 2.6 애플리케이션 개발


아래는 하이버네이트의 예제입니다.


```java
package jpabook.start;

import javax.persistence.*;
import java.util.List;

/**
 * @author holyeye
 */
public class JpaMain {

    public static void main(String[] args) {

        //엔티티 매니저 팩토리 생성
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        EntityManager em = emf.createEntityManager(); //엔티티 매니저 생성

        EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득

        try {


            tx.begin(); //트랜잭션 시작
            logic(em);  //비즈니스 로직
            tx.commit();//트랜잭션 커밋

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback(); //트랜잭션 롤백
        } finally {
            em.close(); //엔티티 매니저 종료
        }

        emf.close(); //엔티티 매니저 팩토리 종료
    }

    public static void logic(EntityManager em) {

        String id = "id1";
        Member member = new Member();
        member.setId(id);
        member.setUsername("지한");
        member.setAge(2);

        //등록
        em.persist(member);

        //수정
        member.setAge(20);

        //한 건 조회
        Member findMember = em.find(Member.class, id);
        System.out.println("findMember=" + findMember.getUsername() + ", age=" + findMember.getAge());

        //목록 조회
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        System.out.println("members.size=" + members.size());

        //삭제
        em.remove(member);

    }
}
```

### 엔티티 매니저 설정

엔티티 매니저의 생성 과정은 아래 절차로 이루어집니다.

![image](https://user-images.githubusercontent.com/31622350/96356185-1abdae00-1126-11eb-9390-3418594af4aa.png)

#### 엔티티 매니저 팩토리 생성

JPA를 사용하기 위해서는 persistence.xml을 참조하여 엔티티 매니저 팩토리를 생성해야 합니다.

아래 이유로 `엔티티 매니저 팩토리의 생성 비용은 큽니다.`

- JPA를 동작시키기 위한 기반 객체 생성
- 데이터 베이스 커넥션 풀 생성( -> jdbc 영역으로 예상되지만, jpa가 jdbc를 사용한 wrapper 느낌이기 때문에 생성비용에 포함.)

따라서, 엔티티 매니저 팩토리는 애플리케이션 전체에서 딱한번만 생성하고 공유해서 사용해야 합니다.


#### 엔티티 매니저 생성

엔티티 매니저는 엔티티 매니저 팩토리를 사용하여 생성 가능합니다.

실제로 엔티티를 DB에 CRUD 할 때 사용하게 됩니다.

> 유의할 점은, 엔티티 매니저는 thread-safe하지 않기 때문에 multi-thread 환경에서 공유해서 사용해서는 안된다는 점입니다.

#### 종료

사용이 끝난 후에는 반드시 엔티티 매니저와 엔티티 매니저 팩토리를 종료해야합니다.
> 엔티티 매니저 팩토리의 경우에는 app 종료시입니다.
> 자동으로 종료를 호출되어있는 상태가 아니라면 graceful shutdown하도록 구현할 때, 이 부분을 추가해야 합니다.


### 트랜잭션 관리

JPA는 항상 트랜잭션 안에서 데이터를 변경해야 합니다.
아래는 엔티티 매니저에서 트랜잭션을 가져오는 샘플 코드입니다.

```java
EntityTransaction tx = em.getTransaction(); //트랜잭션 기능 획득

try {
    tx.begin(); //트랜잭션 시작
    logic(em);  //비즈니스 로직
    tx.commit();//트랜잭션 커밋

} catch (Exception e) {
    e.printStackTrace();
    tx.rollback(); //트랜잭션 롤백
}
```

### 비즈니스 로직

아래는 실제 CRUD하는 비즈니스 로직입니다.

```java
    public static void logic(EntityManager em) {

        String id = "id1";
        Member member = new Member();
        member.setId(id);
        member.setUsername("지한");
        member.setAge(2);

        //등록
        em.persist(member);

        //수정
        member.setAge(20);

        //한 건 조회
        Member findMember = em.find(Member.class, id);
        System.out.println("findMember=" + findMember.getUsername() + ", age=" + findMember.getAge());

        //목록 조회
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        System.out.println("members.size=" + members.size());

        //삭제
        em.remove(member);

    }
```

아래 로직을 수행하는 경우 출력결과는 아래와 같습니다.

```
findMember=지한, age=20
members.size=1
```

이때, 유의 깊게 볼 부분은 age=20 입니다.

저희는 등록을 위해 persist 호출 후 별도의 em의 메서드를 호출하지 않았습니다.
하지만, find의 결과에는 java object를 수정한 값이 그대로 담겨있습니다.

이는, JPA가 관리하는 `엔티티의 경우에는 변경을 감지/추적하는 기능`이 있기 때문입니다.


### JPQL

JPA를 사용하는 목적은 DB를 사용함에 있어서도 객체지향적인 프로그래밍을 하도록 하는것입니다.

하지만 조회쿼리는 이를 방해하게 됩니다.

예를들어, member 테이블의 전체 조회의 경우 sql로는 아래와 같이 작성하게 됩니다.

```sql
select * from member
```

하지만, 이는 DB SQL을 사용함으로써 DB 종속성이 생기는 꼴입니다.

때문에, JPA는 JPQL이라는 쿼리언어를 별도로 제공하여 객체지향적인 sql 작성을 가능하도록 합니다.

```sql
select m from Member -> 테이블이 아닌 Entity를 의미
```

JPQL과 SQL의 차이점은 아래와 같습니다.

- JPQL은 엔티티 객체를 대상으로 쿼리
- SQL은 데이터베이스 테이블을 대상으로 쿼리 
