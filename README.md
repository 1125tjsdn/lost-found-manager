# 분실물 관리 프로그램 (LostFound)

Java Swing으로 만든 GUI 분실물 관리 프로그램입니다. 분실물을 등록하고, 건물 층별 약도 위에 습득 위치를 표시하며, 수령 처리까지 관리할 수 있습니다.

- 작성자: 개인 프로젝트
- 개발 환경: Java 21, Eclipse, Swing

## 주요 기능

- **분실물 등록**: 이름, 카테고리, 설명, 사진, 습득 위치(약도 클릭으로 좌표 지정)
- **보관 중 목록**: 표 형태로 조회 및 키워드 검색
- **약도 보기**: 층별(B1, F1~F5) 약도 위에 분실물 위치를 마커로 표시, 마커 클릭 시 상세 보기
- **상세 보기**: 분실물 정보 확인, 수령 처리, 삭제, 상태 변경
- **수령 완료 목록**: 수령자 이름/연락처/수령일 기록, 검색, 전체 삭제
- **데이터 저장**: CSV 파일(`data/lost_items.csv`)에 저장

## 프로젝트 구조 (MVC)

```
LostFound/
├── src/lostfound/
│   ├── LostFoundApp.java            # 프로그램 시작점 (main)
│   ├── model/
│   │   ├── LostItem.java            # 분실물 데이터
│   │   ├── Category.java            # 카테고리 enum
│   │   └── ItemStatus.java          # 보관 중 / 수령 완료 enum
│   ├── view/
│   │   ├── LostFoundFrame.java      # 메인 프레임 (CardLayout으로 화면 전환)
│   │   ├── RegisterPanel.java       # 등록 화면
│   │   ├── ItemTablePanel.java      # 보관 중 / 수령 완료 목록
│   │   ├── ViewMapPanel.java        # 약도 위 분실물 보기
│   │   ├── SelectMapPanel.java      # 약도에서 좌표 선택
│   │   └── ItemDetailPanel.java     # 상세 화면
│   ├── controller/
│   │   └── LostItemController.java  # 등록/검색/수령/삭제 로직
│   └── repository/
│       └── LostItemRepository.java  # CSV 읽기/쓰기
├── data/
│   ├── lost_items.csv               # 분실물 데이터
│   ├── maps/                        # 층별 약도 이미지 (B1, F1~F5)
│   └── item_photos/                 # 등록된 분실물 사진
└── docs/presentation.pdf            # 발표 자료
```

## 실행 방법

**Eclipse**
1. `File > Import > Existing Projects into Workspace`에서 이 폴더를 선택합니다.
2. `src/lostfound/LostFoundApp.java`를 Run As > Java Application으로 실행합니다.

**터미널**
```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin lostfound.LostFoundApp
```

> `data/` 폴더를 상대경로로 읽으므로 반드시 프로젝트 루트에서 실행해야 합니다.
