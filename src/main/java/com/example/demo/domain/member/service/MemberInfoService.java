package com.example.demo.domain.member.service;


import com.example.demo.domain.member.dto.MemberInfoResponseDto;
import com.example.demo.domain.member.dto.MemberInfoSaveDto;
import com.example.demo.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberInfoService {

    private final MemberService memberService;

    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMemberInfo(Long memberId) {
        Member member = memberService.findMemberByMemberId(memberId);
        return MemberInfoResponseDto.of(member);
    }

    public Member saveMemberInfo(MemberInfoSaveDto.MemberInfo memberInfoSaveDto, Long memberId){
        Member member = memberService.findMemberByMemberId(memberId);
        return member.toBuilder()
                .age(memberInfoSaveDto.getAge())
                .nickname(memberInfoSaveDto.getNickname())
                .image(memberInfoSaveDto.getImage())
                .gender(memberInfoSaveDto.getGender())
                .genre1(memberInfoSaveDto.getGenre1())
                .genre2(memberInfoSaveDto.getGenre2())
                .genre3(memberInfoSaveDto.getGenre3())
                .memberName(memberInfoSaveDto.getMemberName())
                .profile(memberInfoSaveDto.getProfile())
                .build();
    }
    public Member saveMemberNickname(MemberInfoSaveDto.MemberNickname memberNickname, Long memberId){
        Member member = memberService.findMemberByMemberId(memberId);
        return member.toBuilder()
                .nickname(memberNickname.getNickname())
                .image(memberNickname.getImage())
                .build();
    }
    public Member saveMemberAgeAndGender(MemberInfoSaveDto.MemberAgeAndGender memberAgeAndGender, Long memberId){
        Member member = memberService.findMemberByMemberId(memberId);
        return member.toBuilder()
                .age(memberAgeAndGender.getAge())
                .gender(memberAgeAndGender.getGender())
                .build();
    }
    public Member saveMemberGenre(MemberInfoSaveDto.MemberGenre memberGenre, Long memberId){
        Member member = memberService.findMemberByMemberId(memberId);

        return member.toBuilder()
                .genre1(memberGenre.getGenre1())
                .genre2(memberGenre.getGenre2())
                .genre3(memberGenre.getGenre3())
                .build();
    }
    public Member saveMemberPw(String password, Long memberId){
        Member member = memberService.findMemberByMemberId(memberId);
        return member.toBuilder()
                .password(password)
                .build();
    }






}

