package com.example.orm.jpa;

import com.example.orm.jpa.entity.Member;
import com.example.orm.jpa.entity.MemberId;
import com.example.orm.jpa.repository.MemberRepository;
import com.example.orm.jpa.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class JpaApplication implements ApplicationRunner {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JpaApplication jpaApplication = applicationContext.getBean("jpaApplication", JpaApplication.class);

        log.info("jpaApplication -> {}", jpaApplication);

        MemberService memberService = applicationContext.getBean("memberService", MemberService.class);

        log.info("memberService -> {}", memberService);
//        memberService.testPersist();


//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//
//        executorService.execute(() -> {
//            System.out.println("thread name -> " + Thread.currentThread().getName());
//            memberService.update(2 * 1000, 10, "m1");
//        });
//
//        executorService.execute(() -> {
//            System.out.println("thread name -> " + Thread.currentThread().getName());
//            memberService.update(10 * 1000, 30, "m2");
//        });
//        System.out.println("asdf");


//        Team team = new Team();
//        team.setId(1);
//        team.setName("team1");
//        teamRepository.save(team);
//
//        Member member1 = new Member();
//        member1.setId(1);
//        member1.setName("member1");
//        member1.setTeam(team);
//        team.getMemberList().add(member1);
//
//
//        Member member2 = new Member();
//        member2.setId(2);
//        member2.setName("member2");
//        member2.setTeam(team);
//        team.getMemberList().add(member2);
//
//        memberRepository.saveAll(Arrays.asList(member1, member2));
    }
}
