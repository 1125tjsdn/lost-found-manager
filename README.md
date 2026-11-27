# 분실물 관리 프로그램 (LostFound)

- Java Swing 기반 GUI 분실물 관리 프로그램
- Java 21, Eclipse

## 주요 기능

- 분실물 등록: 이름, 분류, 비고, 사진, 습득 위치(약도 클릭)
- 보관 중 / 수령 완료 목록 조회 및 검색
- 층별 약도(B1, F1~F5) 위 분실물 위치 마커 표시
- 상세 보기: 수령 처리, 보관 중 복원, 삭제
- CSV 파일(`data/lost_items.csv`)로 데이터 저장

## 구조 (MVC)

- `model`: LostItem, Category, ItemStatus
- `view`: LostFoundFrame, RegisterPanel, ItemTablePanel, ViewMapPanel, SelectMapPanel, ItemDetailPanel
- `controller`: LostItemController
- `repository`: LostItemRepository (CSV 입출력)
- `data`: lost_items.csv, maps/(약도 이미지), item_photos/(분실물 사진)
- `docs`: 발표 자료

## 문서

- [시스템 설계 문서](docs/ARCHITECTURE.md): 아키텍처, 클래스 다이어그램, 화면 흐름도, 데이터·메서드 명세
- [발표 자료](docs/presentation.pdf)

## 실행 방법

- Eclipse: 프로젝트 Import 후 `LostFoundApp.java` 실행
- 터미널 (프로젝트 루트에서 실행)

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin lostfound.LostFoundApp
```
