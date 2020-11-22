package com.example.orm.jpa.repository;

import com.example.orm.jpa.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update member set cnt = cnt + :cnt where id = :id", nativeQuery = true)
    void updateCnt(@Param(value = "id") long id, @Param(value = "cnt") int cnt);
}
