package com.newest.querydsl;

import com.newest.querydsl.entity.Member;
import static com.newest.querydsl.entity.QMember.member;
import com.newest.querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        // given
        // 런타임 시점에 오타 발견
        String qlString = "select m from Member m where m.username = :username";

        // when
        Member singleResult = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        // then
        assertThat(singleResult.getUsername()).isEqualTo("member1");
    }


    @Test
    void startQuerydsl() {
        // given
//        QMember m = new QMember("m"); 셀프조인을 하는 경우 alias를 별도로 지정해서 활용하기도 한다.
//        QMember m = QMember.member;

        // when
        // 컴파일 시점에 문법 오류 검사 발견
        Member member1 = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        // then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        // given

        // when
        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        // then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchAndParam() {
        // given

        // when
        Member member1 = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        // then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }


}
