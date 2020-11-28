package com.example.jpa.service;

import com.example.jpa.entity.Member;
import com.example.jpa.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    public Long join(Member member){
        memberRepository.save(member);
        return member.getId();
    }

    public Member getMember(Long id){
        return memberRepository.findOne(id);
    }

    public Member getProxy(Long id){
        return memberRepository.getProxy(id);
    }
}
