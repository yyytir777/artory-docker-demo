package com.example.demo.api.login.service;


import com.example.demo.api.login.dto.OauthLoginDto;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.domain.member.constant.MemberType;
import com.example.demo.domain.member.constant.Role;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.exteranal.oauth.model.OAuthAttributes;
import com.example.demo.exteranal.oauth.service.SocialLoginApiService;
import com.example.demo.exteranal.oauth.service.SocialLoginApiServiceFactory;
import com.example.demo.global.error.ErrorCode;
import com.example.demo.global.error.exception.BusinessException;
import com.example.demo.global.jwt.dto.JwtTokenDto;
import com.example.demo.global.jwt.service.TokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OauthLoginService {

    private final MemberService memberService;
    private final TokenManager tokenManager;
//
    public OauthLoginDto.Response oauthLogin(String accessToken, MemberType memberType) {
        SocialLoginApiService socialLoginApiService = SocialLoginApiServiceFactory.getSocialLoginApiService(memberType);
        OAuthAttributes userInfo = socialLoginApiService.getUserInfo(accessToken);
        log.info("userInfo : {}",  userInfo);


        JwtTokenDto jwtTokenDto;
        Optional<Member> optionalMember = memberService.findMemberByEmail(userInfo.getEmail());
        if(optionalMember.isEmpty()){//신규
            Member oauthMember = userInfo.toMemberEntity(memberType, Role.USER);
            oauthMember = memberService.registerMember(oauthMember);
            //토큰 생성
            jwtTokenDto = tokenManager.createJwtTokenDto(oauthMember.getMemberId(), oauthMember.getRole());
            oauthMember.updateRefreshToken(jwtTokenDto);
            //시큐리티에 저장


        }else if(memberType == optionalMember.get().getMemberType()){//기존
            Member oauthMember = optionalMember.get();

            //토큰 생성
            jwtTokenDto = tokenManager.createJwtTokenDto(oauthMember.getMemberId(), oauthMember.getRole());
            oauthMember.updateRefreshToken(jwtTokenDto);
        } else throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        //같은 이메일 사용한 경우

        return OauthLoginDto.Response.of(jwtTokenDto);

    }

}
