package com.example.orm.jpa.entity;

import lombok.*;

import javax.persistence.*;

@Setter
//@SequenceGenerator(
//        name="USER_SEQ_GEN",
//        sequenceName="USER_SEQ",
//        allocationSize=1
//)
@Getter
@Entity
@ToString
//@IdClass(MemberId.class)
@NoArgsConstructor
public class Member {

    @Id
//    @GeneratedValue(
//            strategy= GenerationType.SEQUENCE,
//            generator="USER_SEQ_GEN"
//    )
    private long id;

    private String name;

    private int cnt;

    @Builder
    public Member(long id, String name, int cnt) {
        this.id = id;
        this.name = name;
        this.cnt = cnt;
    }
}
