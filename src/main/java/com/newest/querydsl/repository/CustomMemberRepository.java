package com.newest.querydsl.repository;

import com.newest.querydsl.dto.MemberSearchCondition;
import com.newest.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface CustomMemberRepository {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
