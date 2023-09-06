package com.newest.querydsl;

import com.newest.querydsl.dto.MemberDto;
import com.newest.querydsl.dto.QMemberDto;
import com.newest.querydsl.entity.Member;
import com.newest.querydsl.entity.QMember;
import static com.newest.querydsl.entity.QMember.member;
import static com.newest.querydsl.entity.QTeam.team;
import com.newest.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    @Autowired
    EntityManagerFactory emf;
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

    @Test
    void resultFetch() {
        // given

        // when
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();


        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Long count = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();

        // then
    }

    @Test
    void sort() {
        // given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        // when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        // then
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging() {
        // given

        // when
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        // then
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void aggragation() {
        // given

        // when
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        // then
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void groupBy() {
        // given

        // when
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .having(team.name.eq("teamA"))
                .fetch();

        // then
        Tuple teamA = result.get(0);
//        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
//        assertThat(teamB.get(team.name)).isEqualTo("teamB");
//        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void join() {
        // given

        // when
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        // then
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    void theta_join() {
        // given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        // when
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        // then
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    @Test
//    JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
    void join_on_filitering() {
        // given

        // when
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        // then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void fetchJoinNo() {
        // given
        em.flush();
        em.clear();

        // when
        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        // then
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoin() {
        // given
        em.flush();
        em.clear();

        // when
        Member member1 = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        // then
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isTrue();
    }

    @Test
    public void subQuery() {
        // given
        QMember memberSub = new QMember("memberSub");
        // when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        // then
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    @Test
    public void selectSubquery() {
        // given
        QMember memberSub = new QMember("memberSub");
        // when
        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();


        // then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void simpleProjection() {
        // given

        // when
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        // then
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() {
        // given

        // when
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        // then
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    @Test
    public void findDtoByJPQL() {
        // given

        // when
        List<MemberDto> result = em.createQuery(
                        "select new com.newest.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        // then
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoBySetter() {
        // given

        // when
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        // then
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField() {
        // given

        // when
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        // then
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByConstructor() {
        // given

        // when
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        // then
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    
    @Test
    public void findDtoByQueryProjection() {
        // given
        //생성자 방식 대비 컴파일 시점에 오류를 잡아낼 수 있는 장점
        //반면에 의존성이 생기는 단점이 있음
        // when
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        // then
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void dynamicQueryBooleanBuilder() {
        // given
        String usernameParam = "member1";
        Integer ageParam = 10;

        // when
        List<Member> result = searchMember1(usernameParam, ageParam);

        // then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();

    }
        
}
