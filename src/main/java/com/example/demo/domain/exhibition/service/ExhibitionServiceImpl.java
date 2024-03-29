package com.example.demo.domain.exhibition.service;


import com.example.demo.domain.exhibition.converter.ExhibitionConverter;
import com.example.demo.domain.exhibition.dto.ExhibitionRequestDto;
import com.example.demo.domain.exhibition.dto.ExhibitionResponseDto;
import com.example.demo.domain.exhibition.entity.Exhibition;
import com.example.demo.domain.exhibition.repository.ExhibitionRepository;
import com.example.demo.domain.member.constant.Genre;
import com.example.demo.domain.member.entity.Member;
import com.example.demo.domain.member.service.MemberService;
import com.example.demo.global.resolver.memberInfo.MemberInfo;
import com.example.demo.global.resolver.memberInfo.MemberInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExhibitionServiceImpl implements ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionConverter exhibitionConverter;
    private final ExhibitionDistanceRecommendService exhibitionDistanceRecommendService;
    private final MemberService memberService;


    @Override
    public ExhibitionResponseDto.ExhibitionListResponseDto getAllExhibitionList(@MemberInfo MemberInfoDto memberInfoDto, LocalDate currentDate, int page) {
//        Long memberId = memberInfoDto.getMemberId();

        ExhibitionResponseDto.ExhibitionListResponseDto allResponseDto = new ExhibitionResponseDto.ExhibitionListResponseDto();
        // 최근 전시회 가져오기
        allResponseDto.setRecentExhibitionDtoList(getRecentExhibitions(memberInfoDto, currentDate, page));
        // 인기 있는 전시회 가져오기
        allResponseDto.setPopluarExhibitionDtoList(getPopularityExhibitions(memberInfoDto, page));
        // 랜덤 전시회 가져오기
        allResponseDto.setRandomExhibitionDtoList(getRandomExhibitions(memberInfoDto, page));
        // 비슷한 전시회 가져오기
        allResponseDto.setRecommendExhibitionDtoList(getRandomExhibitions(memberInfoDto, page));
        // 추천 전시회 가져오기
        allResponseDto.setSimilarExhibitionDtoList(getRecommendExhibitions(memberInfoDto, page));

        return allResponseDto;
    }

    @Override
    public ExhibitionResponseDto.ExhibitionGenreListResponseDto getGenreList() {
        ExhibitionResponseDto.ExhibitionGenreListResponseDto allResponseDto = new ExhibitionResponseDto.ExhibitionGenreListResponseDto();
        //카테고리 정보
        allResponseDto.setMediaCategoryResponseDto(findMediaExhibition());
        allResponseDto.setCraftCategoryResponseDto(findCraftExhibition());
        allResponseDto.setDesignCategoryResponseDto(findDesignExhibition());
        allResponseDto.setPictureCategoryResponseDto(findPictureExhibition());
        allResponseDto.setSpecialExhibitionCategoryResponseDto(findSpecialExhibitionExhibition());
        allResponseDto.setSculptureCategoryResponseDto(findSculptureExhibition());
        allResponseDto.setPlanExhibitionCategoryResponseDto(findPlanExhibitionExhibition());
        allResponseDto.setInstallationArtCategoryResponseDto(findInstallationArtExhibition());
        allResponseDto.setPaintingCategoryResponseDto(findPaintingExhibition());
        allResponseDto.setArtistExhibitionCategoryResponseDto(findArtistExhibitionExhibition());
        return allResponseDto;
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getDistanceRecommendExhibitions(ExhibitionRequestDto requestDto, @MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 10;
        double userLatitude = Double.parseDouble(requestDto.getLatitude());
        double userLongitude = Double.parseDouble(requestDto.getLongitude());

        // 거리 기반으로 가장 가까운 전시회 가져오기
        List<Exhibition> closestExhibitions = exhibitionDistanceRecommendService.findClosestExhibitions(userLatitude, userLongitude, getAllExhibitions(), page, pageSize);

        // 좋아요 여부 가져오기
        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> result = closestExhibitions.stream()
                .map(exhibition -> {
                    Boolean isLiked = exhibitionRepository.findLikeStatusByMemberIdAndExhibitionId(memberId, exhibition.getId());
                    Boolean isScrapped = exhibitionRepository.findScrapStatusByMemberIdAndExhibitionId(memberId, exhibition.getId());
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());

        return result;
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getRecentExhibitions(@MemberInfo MemberInfoDto memberInfoDto, LocalDate currentDate, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Object[]> recentExhibitionsPage = exhibitionRepository.findAllByOrderByCreateTimeByDesc(memberId, currentDate, pageable);


        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> recentExhibitions = recentExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());


        return recentExhibitions;
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getPopularityExhibitions(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Object[]> likeExhibitionsPage = exhibitionRepository.findAllByOrderByExhibitionLikeCountDesc(memberId, pageable);

        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> likeExhibitions = likeExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());


        return likeExhibitions;
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> searchExhibitionsByTitle(String title, @MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Object[]> searchResultsPage = exhibitionRepository.findByExhibitionTitleContainingCase(memberId, title, pageable);


        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> searchResults = searchResultsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());

        return searchResults;
    }


    @Override
    public ExhibitionResponseDto.ExhibitionSpecificResponseDto getExhibitionById(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exhibition not found with id: " + id));

        return exhibitionConverter.convertToSpecificDto(exhibition);
    }


    // 모든 전시회 정보 조회 메서드
    @Override
    public List<Exhibition> getAllExhibitions() {
        return exhibitionRepository.findAll();
    }

    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getRandomExhibitions(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Object[]> randomExhibitionsPage = exhibitionRepository.findRandomExhibitions(memberId, pageable);

        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> randomExhibitions = randomExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());


        return randomExhibitions;
    }

    @Override
    public ExhibitionResponseDto.ExhibitionGeneralOneResponseDto getRandomOneExhibition() {
        Exhibition randomExhibition = exhibitionRepository.findRandomOneExhibition();
        return exhibitionConverter.convertToOneDto(randomExhibition);
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getRecommendExhibitions(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 각 멤버의 genre1, genre2, genre3 값 가져오기
        Member member = memberService.findMemberByMemberId(memberId);
        Genre genre1 = member.getGenre1();
        Genre genre2 = member.getGenre2();
        Genre genre3 = member.getGenre3();
        // genre1, genre2, genre3를 문자열로 변환
        String genre1String = genre1.name();
        String genre2String = genre2.name();
        String genre3String = genre3.name();


        Page<Object[]> recommendExhibitionsPage = exhibitionRepository.findRecommendedExhibitions(memberId, genre1String, genre2String, genre3String, pageable);


        return recommendExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getSimilarExhibitions(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Object[]> similarExhibitionsPage = exhibitionRepository.findRandomExhibitions(memberId, pageable);

        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> similarExhibitions = similarExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());

        return similarExhibitions;
    }


    //각 페이지 눌렀을때
    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getDistanceRecommendExhibitions1(ExhibitionRequestDto requestDto, @MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 40;
        double userLatitude = Double.parseDouble(requestDto.getLatitude());
        double userLongitude = Double.parseDouble(requestDto.getLongitude());

        // 거리 기반으로 가장 가까운 전시회 가져오기
        List<Exhibition> closestExhibitions = exhibitionDistanceRecommendService.findClosestExhibitions(userLatitude, userLongitude, getAllExhibitions(), page, pageSize);

        // 좋아요 여부 가져오기
        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> result = closestExhibitions.stream()
                .map(exhibition -> {
                    Boolean isLiked = exhibitionRepository.findLikeStatusByMemberIdAndExhibitionId(memberId, exhibition.getId());
                    Boolean isScrapped = exhibitionRepository.findScrapStatusByMemberIdAndExhibitionId(memberId, exhibition.getId());
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());

        return result;
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getRecentExhibitions1(@MemberInfo MemberInfoDto memberInfoDto, LocalDate currentDate, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 40;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Object[]> recentExhibitionsPage = exhibitionRepository.findAllByOrderByCreateTimeByDesc(memberId, currentDate, pageable);


        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> recentExhibitions = recentExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());


        return recentExhibitions;
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getPopularityExhibitions1(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 40;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Object[]> likeExhibitionsPage = exhibitionRepository.findAllByOrderByExhibitionLikeCountDesc(memberId, pageable);

        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> likeExhibitions = likeExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());


        return likeExhibitions;
    }


    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getRandomExhibitions1(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 40;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Object[]> randomExhibitionsPage = exhibitionRepository.findRandomExhibitions(memberId, pageable);

        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> randomExhibitions = randomExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());


        return randomExhibitions;
    }

    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getRecommendExhibitions1(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 40;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // 각 멤버의 genre1, genre2, genre3 값 가져오기
        Member member = memberService.findMemberByMemberId(memberId);
        Genre genre1 = member.getGenre1();
        Genre genre2 = member.getGenre2();
        Genre genre3 = member.getGenre3();
        // genre1, genre2, genre3를 문자열로 변환
        String genre1String = genre1.name();
        String genre2String = genre2.name();
        String genre3String = genre3.name();


        Page<Object[]> recommendExhibitionsPage = exhibitionRepository.findRecommendedExhibitions(memberId, genre1String, genre2String, genre3String, pageable);


        return recommendExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> getSimilarExhibitions1(@MemberInfo MemberInfoDto memberInfoDto, int page) {
        Long memberId = memberInfoDto.getMemberId();

        int pageSize = 40;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Object[]> similarExhibitionsPage = exhibitionRepository.findRandomExhibitions(memberId, pageable);

        List<ExhibitionResponseDto.ExhibitionGeneralResponseDto> similarExhibitions = similarExhibitionsPage.getContent()
                .stream()
                .map(array -> {
                    Exhibition exhibition = (Exhibition) array[0];
                    Boolean isLiked = (Boolean) array[1];
                    Boolean isScrapped = (Boolean) array[2];
                    return exhibitionConverter.convertToGeneralDto(exhibition, isLiked, isScrapped);
                })
                .collect(Collectors.toList());

        return similarExhibitions;
    }


    @Override
    public  ExhibitionResponseDto.GenreCategoryResponseDto findMediaExhibition(){
        Exhibition mediaExhibition = exhibitionRepository.findMediaExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(mediaExhibition);
    }
    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findCraftExhibition() {
        Exhibition craftExhibition = exhibitionRepository.findCraftExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(craftExhibition);
    }

    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findDesignExhibition(){
        Exhibition designExhibition = exhibitionRepository.findDesignExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(designExhibition);
    }
    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findPictureExhibition() {
        Exhibition pictureExhibition = exhibitionRepository.findPictureExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(pictureExhibition);
    }

    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findSpecialExhibitionExhibition() {
        Exhibition specialExhibitionExhibition = exhibitionRepository.findSpecialExhibitionExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(specialExhibitionExhibition);
    }
    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findSculptureExhibition() {
        Exhibition sculptureExhibition = exhibitionRepository.findSculptureExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(sculptureExhibition);
    }
    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findPlanExhibitionExhibition() {
        Exhibition planExhibitionExhibition = exhibitionRepository.findPlanExhibitionExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(planExhibitionExhibition);
    }

    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findInstallationArtExhibition() {
        Exhibition installationArtExhibition = exhibitionRepository.findInstallationArtExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(installationArtExhibition);
    }

    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findPaintingExhibition() {
        Exhibition paintingExhibition = exhibitionRepository.findPaintingExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(paintingExhibition);
    }

    @Override
    public ExhibitionResponseDto.GenreCategoryResponseDto findArtistExhibitionExhibition() {
        Exhibition artistExhibitionExhibition = exhibitionRepository.findArtistExhibitionExhibition();
        return exhibitionConverter.convertToGenreCategoryDto(artistExhibitionExhibition);
    }


}

