package com.newest.querydsl.repository;

import com.newest.querydsl.dto.MemberSearchCondition;
import com.newest.querydsl.dto.MemberTeamDto;
import com.newest.querydsl.entity.Member;
import com.newest.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        // given
        Member member1 = new Member("member1", 10);
        memberJpaRepository.save(member1);

        // when
        Member findMember = memberJpaRepository.findById(member1.getId()).get();
        List<Member> result1 = memberJpaRepository.findAll();
        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        List<Member> result3 = memberJpaRepository.findAll_Querydsl();
        List<Member> result4 = memberJpaRepository.findByUsername_Querydsl("member1");

        // then
        assertThat(findMember).isEqualTo(member1);
        assertThat(result1).containsExactly(member1);
        assertThat(result2).containsExactly(member1);
        assertThat(result3).containsExactly(member1);
        assertThat(result4).containsExactly(member1);
    }

    @Test
    public void searchTest() {
        // given
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

        // when
        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setAgeGoe(35);
        memberSearchCondition.setAgeLoe(40);
        memberSearchCondition.setTeamName("teamB");
        // 조건이 없는 경우는 전체 데이터를 가져오기 때문에 기본 검색 조건이나 페이징이 있어야 좋다

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(memberSearchCondition);

        // then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    public void search() {
        // given
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

        // when
        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setAgeGoe(35);
        memberSearchCondition.setAgeLoe(40);
        memberSearchCondition.setTeamName("teamB");
        // 조건이 없는 경우는 전체 데이터를 가져오기 때문에 기본 검색 조건이나 페이징이 있어야 좋다

        List<MemberTeamDto> result = memberJpaRepository.search(memberSearchCondition);

        // then
        assertThat(result).extracting("username").containsExactly("member4");
    }


}