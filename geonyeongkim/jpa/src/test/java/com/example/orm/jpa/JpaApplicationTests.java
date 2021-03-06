package com.example.orm.jpa;

import com.example.orm.jpa.entity.Member;
import com.example.orm.jpa.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;


@Slf4j
@SpringBootTest
class JpaApplicationTests {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    public void test() {
        Member member1 = Member.builder()
                .id(1)
                .name("testName1")
                .cnt(1)
                .build();

        Member member2 = Member.builder()
                .id(2)
                .name("testName2")
                .cnt(2)
                .build();

        memberRepository.saveAll(Arrays.asList(member1, member2));

        memberRepository.updateCnt(1, 2);

        Member m1 = memberRepository.findById(1L).get();
        log.info("m1 -> {}", m1);
    }
}
