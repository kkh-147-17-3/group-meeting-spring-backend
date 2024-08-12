package com.sideprj.groupmeeting.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class NicknameService {

    private final UserService userService;

    private final String[] adjectives = {
            "사랑스러", "신비로운", "멋있는", "강렬한", "화려한", "고요한", "상쾌한",
            "편안한", "밝은", "청명한", "고운", "신속한", "행복한", "부드러운",
            "명쾌한", "감미로운", "조용한", "깊은", "푸른", "신선한", "신나는",
            "기쁜", "멀리", "고상한", "청순한", "귀여운", "강한", "섬세한",
            "아름다운", "감동적인", "간단한", "열정적인", "부유한", "매력적인",
            "찬란한", "확실한", "시원한", "독특한", "기발한", "푸근한", "우아한",
            "통통한", "기분좋은", "맑은", "수수한", "원활한", "고백하는",
            "따뜻한", "평화로운", "현대적인", "소중한", "감각적인", "쾌적한",
            "여유로운", "창의적인", "재미있는", "충실한", "신비한", "우수한",
            "대담한", "뚜렷한", "활기찬", "솔직한", "깨끗한", "즐거운", "성실한",
            "평온한", "자연스러운", "상큼한", "순수한", "이국적인", "굳건한",
            "온화한", "세련된", "깔끔한", "명랑한", "정직한", "화창한", "순진한",
            "독창적인", "빠른", "청초한", "정중한", "포근한", "굉장한"
    };

    private final String[] nouns = {
            "바닷물", "별빛", "산길", "청춘", "꽃밭", "물결", "강물", "숲속",
            "별밤", "하늘", "도시락", "여행", "바람", "해돋이", "구름", "눈물",
            "마음", "이야기", "날씨", "우주", "꿈속", "초원", "소리", "문학",
            "바다", "책상", "음악", "거리", "정원", "뒷산", "친구", "향기",
            "새벽", "성격", "축제", "꽃다발", "저녁", "오후", "연구", "기억",
            "햇살", "해변", "일상", "도서관", "정수기", "물고기", "술잔", "일기",
            "빛깔", "가족", "선물", "메모리", "서재", "수면", "포도", "연극",
            "가구", "식물", "커피", "노래", "춤추기", "꽃잎", "편지", "운동",
            "구슬", "칼끝", "동산", "언덕", "시계", "도로", "해수", "대화",
            "방울", "탁자", "이불", "물건", "강좌", "수업", "겨울", "나무",
            "알람", "선풍기", "도시", "꽃집", "환상", "즐거움", "애인",
            "세계", "구름"
    };

    public NicknameService(UserService userService) {
        this.userService = userService;
    }

    public String generateRandomNickname() {
        var random = new Random();
        int min = 100;
        int max = 999;
        int randomNumber = random.nextInt((max - min) + 1) + min;

        var randomAdjIdx = random.nextInt(adjectives.length);
        var randomNounIdx = random.nextInt(nouns.length);

        var isDuplicated = true;
        String nickname;
        do {
            nickname = "%s%s%s".formatted(adjectives[randomAdjIdx], nouns[randomNounIdx], String.valueOf(randomNumber));
            isDuplicated = userService.checkNicknameDuplicated(nickname);
        } while (isDuplicated || nickname.isEmpty());

        return nickname;
    }
}
