package com.example.jpa.test;

import com.example.jpa.entity.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class NoResultExceptionTestService {

    @PersistenceContext
    EntityManager em;

    public Member findMember() throws NoResultException {
        //조회된 데이터가 없는 경우
        return em.createQuery("select m from Member m", Member.class)
                .getSingleResult();
    }

    public void save(){
        em.persist(new Member("이름", 20));
    }
}
