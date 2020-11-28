package com.example.jpa.repository;

import com.example.jpa.entity.Member;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext
    EntityManager em;

    public void save(Member member){
        em.persist(member);
        em.flush();
        em.clear();
    }

    public Member findOne(Long id){
        return em.find(Member.class, id);
    }

    public Member getProxy(Long id){
        return em.getReference(Member.class, id);
    }
}
