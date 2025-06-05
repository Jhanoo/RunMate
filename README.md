# 💗 **싸피 12기 2학기 자율 프로젝트**
## 당신의 발걸음을 책임질 마라톤 파트너 [런메이트]
- 팀명 : **달려라 환이**
- 팀원 : **정찬우, 김환, 이한나, 김미경, 한아영, 황선혁**

</br>

**개발 기간** : `2025/04/14(월) ~ 2025/05/22(목)`



## 목차
1. [팀원 소개](#팀원-소개)
2. [기술 스택](#기술-스택)
3. [서비스 소개](#서비스-소개)
4. [산출물](#산출물)
5. [주요 기능 상세설명](#주요-기능-상세설명)
6. [AI 활용내역](#AI-활용내역)
6. [맡은 역할](#맡은-역할)

</br>


## 📌팀원 소개
### D102 - 애리조나

| 이름    | 역할  | 파트 |  
| ------ | ------ | ---- | ---|
| **정찬우**  | 팀장 | Backend, AI Prompt |   
| **김환** | 팀원 | Backend, Crawling |      
| **이한나** |  팀원 | Backend, Infra  |     
| **김미경** | 팀원 | Android, Smart Watch |  
| **한아영** | 팀원 | Android, UI/UX Design  |  
 | **황선혁** | 팀원 | Android, IoT |  

<br>

## 📌기술 스택
<left>

### 백엔드


[![Spring%20Boot](https://img.shields.io/badge/springboot-6DB33F?logo=springboot&logoColor=white)](#)
[![Postgres](https://img.shields.io/badge/Postgres-%23316192.svg?logo=postgresql&logoColor=white)](#)
[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white)](#)
[![OpenAi](https://img.shields.io/badge/openai-%412991.svg?logo=openai&logoColor=white)](#)

[![Firebase](https://img.shields.io/badge/Firebase-039BE5?logo=Firebase&logoColor=white)](#)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000?logo=intellij-idea&logoColor=white)](#)

### 인프라

[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=fff)](#)
[![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?logo=amazon-web-services&logoColor=white)](#)
[![Redis](https://img.shields.io/badge/Redis-%23DD0031.svg?logo=redis&logoColor=white)](#)
[![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?logo=nginx&logoColor=white)](#)
[![Linux](https://img.shields.io/badge/Linux-FCC624?logo=linux&logoColor=black)](#)
[![Jenkins](https://img.shields.io/badge/Jenkins-D24939?logo=jenkins&logoColor=white)](#)



### 안드로이드 

[![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-%237F52FF.svg?logo=kotlin&logoColor=white)](#)


### AI


[![PyTorch](https://img.shields.io/badge/PyTorch-EE4C2C?logo=pytorch&logoColor=fff)](#)


### 디자인

[![Figma](https://img.shields.io/badge/Figma-F24E1E?logo=figma&logoColor=white)](#)
[![Canva](https://img.shields.io/badge/Canva-00C4CC?logo=Canva&logoColor=white)](#)


### 상태 관리
[![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=fff)](#)
[![GitLab](https://img.shields.io/badge/GitLab-FC6D26?logo=gitlab&logoColor=fff)](#)
[![Jira](https://img.shields.io/badge/Jira-0052CC?logo=jira&logoColor=fff)](#)
[![Mattermost](https://img.shields.io/badge/mattermost-002E5F?logo=mattermost&logoColor=fff)](#)
[![Notion](https://img.shields.io/badge/Notion-000?logo=notion&logoColor=fff)](#)

</left>

<br>


</br>

## 📌서비스 소개

### 1. 기획의도

> “AI와 함께, 실전 같은 영어 회화를 매일 내 손안에서!”

기존 영어 회화 학습은 반복적인 문장 암기, 정적인 콘텐츠 중심으로 진행되어 실제 회화 능력 향상에 한계를 보였습니다. 특히, 실제 상황에서 말해보는 경험 부족은 가장 큰 문제였습니다.
이런 문제를 해결하고자, <b>"Lip It!"</b>은 AI 음성과 영상 기술을 활용한 전화 기반의 영어 회화 앱으로 기획되었습니다.
사용자는 셀럽 혹은 커스텀 보이스와 통화하며 자연스럽게 회화 연습을 할 수 있으며, 매일 특정 시간에 전화가 걸려오는 구조를 통해 지속적인 학습 습관 형성도 가능합니다.

또한, 대화 후에는 AI가 생성한 개인 맞춤형 학습 리포트를 통해 피드백을 제공하여, 자신의 회화 실력을 점검하고 개선할 수 있도록 도와줍니다.


### 2. 주요 기능(서비스)

**1. AI 전화 영어 학습**

- 사용자와 AI 보이스(셀럽 및 커스텀 보이스 선택 가능) 간 실시간 전화 통화
- 사용자의 선택에 따라 보이스 모드 / 텍스트 모드로 대화 진행 가능
- RAG를 활용한 자연스러운 문맥 흐름과 대화 기억
- 셀럽 보이스를 리워드 형식으로 해금하며 동기 부여

**2. 커스텀 보이스 생성**

- 지인의 목소리로 커스텀 보이스 생성 가능 (보이스 클로닝)

**3. 전화 알림 스케줄링**

- 사용자가 지정한 시간에 AI 전화가 오도록 예약 설정
- 카테고리 기반으로 원하는 주제에 맞는 대화 가능
- 부재중 전화 기능으로 학습 기회 보완

**4. AI 학습 리포트 제공**

- 통화 후 자동으로 리포트 생성
- 주요 피드백, 원어민 표현 제안, 전체 대화 스크립트 제공

**5. 온보딩 및 맞춤 설정**

- 사용자 정보 기반으로 개인화된 학습 추천
- 앱 필수 권한 안내 및 설정 유도

<br>


<br>

## 📌주요 기능 상세설명

### 1. 온보딩 화면

- 온보딩 화면을 통해 시작 전 간단한 설명 제공
- 본인의 추가 정보를 입력해 전화 학습 시, 관련 내용으로 학습 가능
- 앱 사용 필수 권한 3가지(알림, 갤러리, 마이크) 허용해야 서비스 이용 가능


<img width=550 src="https://velog.velcdn.com/images/bmlsj/post/da91d05b-22cd-4663-adec-8492858f6498/image.png"/>

</br>

<img width=270 src="https://velog.velcdn.com/images/bmlsj/post/cc5830c5-69ed-4106-8608-3a5c8e1141da/image.png"/>


### 2. 셀럽 및 커스텀 보이스로 전화 학습 기능

1) 셀럽 및 커스텀 보이스 선택 가능
    - 셀럽 카드를 뒤집을 경우, 해당 음성을 들을 수 있음
    - 셀럽 음성은 **리워드 형식**으로 레벨 업으로로 모든 셀럽 음성 얻을 수 있음

    <img width=150 src="https://velog.velcdn.com/images/bmlsj/post/5b9afad8-b640-4c57-b674-b9ff21f1423c/image.gif"/>


2) 선택한 보이스로 전화 영어 학습
    - **보이스 모드**와 **텍스트 모드**가 존재
        - 각 모드별 **번역과 자막 기능**이 존재
    - RAG를 사용 => **AI가 이전 대화 기록을 기억**해 더 친근한 대화 가능

    <img width=150 src="https://velog.velcdn.com/images/bmlsj/post/fa30de01-9dee-4516-a894-0853b078cba7/image.gif"/>

### 3. 통화 알림 설정

- 통화 알람 설정을 통해 설정된 시간에 **전화 알림**이 옴
  전화 알림을 통해 좀 더 지속적인 학습 가능
  - 알림 설정 시, 카테고리 설정을 통해 해당 **카테고리에 해당하는 주제로 학습 가능**
- 부재중 시, 부재중 알림이 뜨며 하루에 총 2번까지 **부재중 전화**가 옴
- 전화를 받을 시, 당일 전화는 더이상 오지 않음

<img width=150 src="https://velog.velcdn.com/images/bmlsj/post/6fc8949f-c32a-4312-9fb7-5b1334951a8b/image.gif"/>


### 3. 커스텀 보이스 생성

- 셀럽 보이스 외에도 지인 목소리를 통해 **커스텀 보이스 생성 가능**
- 총 10개의 영어 문장을 따라 읽으면 보이스 생성
- 정확한 보이스 생성을 위해 **음성과 텍스트의 발음 유사도**를 통과해야 다음 문장을 녹음 할 수 잇음

 <img width=150 src="https://velog.velcdn.com/images/bmlsj/post/158484f5-272a-4368-beea-839d5ad30195/image.gif"/>


### 4. AI 학습 리포트 생성

- 전화 학습 이후, 학습 리포트 생성
- 총 3가지 항목을 볼 수 있음
   - 리포트 요약 / 원어민 표현 / 전체 대화 스크립트 
- **리포트 요약** : 전체 대화 내용와 AI 피드백을 요약해줌. 말한 단어 수와 문장 수도 제공
- **원어민 표현** : 사용자가 말한 문장에서 원어민이 사용하는 표현으로 고쳐줌
- **전체 대화 스크립트** : AI와 전체 대화 내역 제공

    <img width=150 src="https://velog.velcdn.com/images/bmlsj/post/ad0454d3-c5e3-438e-a748-bf8941cd0166/image.gif"/>



<br>

## 📌AI 활용내역

### 음성 AI
1. 사전 학습된 Coqui TTS의 VITS 기반 모델을 활용하여 Fine-tuning 수행

🎤 음성 모델 1 : Coqui TTS의 VITS 기반 모델을 **Fine-tuning**하여 **셀럽 음성** 생성

- 음성 wav 파일 들어갈 예정


🎤 음성 모델 2 : XTTS API 기반으로, **사용자가 직접 녹음한 음성**을 바탕으로 **제로샷 커스텀 음성 생성**을 실시간 수행

- 음성 wav 파일 들어갈 예정

### 영상 AI


<img src="docs/hallo.gif" width="700"/>



<br>

## 📌맡은 역할

- 정찬우
  - 팀장, AI, 안드로이드
  - AI: XTTSv2를 통해 AI 음성 파인튜닝 진행(보이스 클로닝)
  - 안드로이드
    - 학습 리포트
    - 셀럽 및 커스텀 보이스
    - 알림 스케쥴링
    - 네비게이션 구현

- 김환
  - AI, 안드로이드 
  - AI
    - Hallo 모델을 사용해 문장에 맞춰 AI 영상 생성
    - AI 음성 보이스 클로닝 함께 진행
  - 안드로이드
    - 로그인 및 회원 가입
    - 알림 편집
    - 커스텀 음성 생성

- 이한나
  - 안드로이드, UIUX 디자인
  - 안드로이드
    - 로그인/로그아웃/회원가입 기능 구현
    - 전화통화 보이스 및 텍스트 모드 구현
      - 웹소켓 통신, SpeechRecognizer, ExoPlayer 활용용
    - 메인 화면
      - FCM 활용한 오늘의 문장
      - Weekly Calls 및 Next Level 구현
    - 전호 스케줄 편집 및 커스텀 보이스 생성 관련 UI 작업

- 김미경
  - 인프라, 백엔드
  - 백엔드
    - 회원관리(JWT) API
    - 전화통화 API
    - 웹소켓 통신 구현
    - TTS 모델 추론 서버 구축
    - RAG를 위한 pinecone

- 한아영
  - 백엔드, UCC
  - 백엔드
    - 커스텀 및 셀럽 음성 API
    - FCM
    - 스케줄링 기반 뉴스 크롤링

- 황선혁
  - 백엔드
    - 학습 리포트 API
    - 전화 알림 예약(일정) API
    - 전화 내용 프롬프팅


<br>
## 🏃‍➡️ 캐릭터 소개
<img src="https://lab.ssafy.com/s12-final/S12P31D107/-/raw/master/images/tonie.gif?ref_type=heads" />
