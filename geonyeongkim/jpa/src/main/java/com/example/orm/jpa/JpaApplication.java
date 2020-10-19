package com.example.orm.jpa;

import com.example.orm.jpa.entity.Member;
import com.example.orm.jpa.entity.Team;
import com.example.orm.jpa.repository.MemberRepository;
import com.example.orm.jpa.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootApplication
public class JpaApplication implements ApplicationRunner {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        Team team = new Team();
        team.setId(1);
        team.setName("team1");
        teamRepository.save(team);

        Member member1 = new Member();
        member1.setId(1);
        member1.setName("member1");
        member1.setTeam(team);
        team.getMemberList().add(member1);


        Member member2 = new Member();
        member2.setId(2);
        member2.setName("member2");
        member2.setTeam(team);
        team.getMemberList().add(member2);

        memberRepository.saveAll(Arrays.asList(member1, member2));
    }
}
