package com.example.orm.jpa.service;

import com.example.orm.jpa.entity.Member;
import com.example.orm.jpa.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Transactional
    public void testPersist() {
        Member member1 = Member.builder()
                .id(1)
                .name("testName1")
                .cnt(1)
                .build()
                ;

        Member member2 = Member.builder()
                .id(2)
                .name("testName2")
                .cnt(2)
                .build();

        memberRepository.saveAll(Arrays.asList(member1, member2));
        memberRepository.updateCnt(1, 2);
//        memberRepository.updateCnt(1, Integer.valueOf("adsf"));

        Member m1 = memberRepository.findById(1L).get();
        log.info("m1 -> {}", m1);
    }

    @Transactional
    public void update(long sleepTime, int cnt, String name) {
//        Member m1 = memberRepository.findById(1L).get();
//        System.out.println("thread name -> " + Thread.currentThread().getName());
//
//        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        m1.setName(name);
//
//        memberRepository.updateCnt(m1.getId(), cnt);
    }
}
