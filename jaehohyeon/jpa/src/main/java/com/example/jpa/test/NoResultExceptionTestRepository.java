package com.example.jpa.test;

import com.example.jpa.entity.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class NoResultExceptionTestRepository {
    @PersistenceContext
    EntityManager em;

    public Member findMember(){
        //조회된 데이터가 없는 경우
        return em.createQuery("select m from Member m", Member.class)
                .getSingleResult();
    }
}
