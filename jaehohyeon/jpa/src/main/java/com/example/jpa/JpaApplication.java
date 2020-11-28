package com.example.jpa;

import com.example.jpa.entity.Member;
import com.example.jpa.repository.MemberRepository;
import com.example.jpa.service.MemberService;
import com.example.jpa.test.NoResultExceptionTestRepository;
import com.example.jpa.test.NoResultExceptionTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootApplication
public class JpaApplication implements ApplicationRunner {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        //Member member = noResultExceptionTestRepository.findMember();
        Member member = new Member("Kim", 20);

        //when
        Long saveId = memberService.join(member);

        //then
        Member memberProxy = memberService.getProxy(saveId);

        System.out.println(memberProxy.getClass());


        System.out.println(Member.class == memberProxy.getClass());
        System.out.println(memberProxy instanceof Member);
    }
}
