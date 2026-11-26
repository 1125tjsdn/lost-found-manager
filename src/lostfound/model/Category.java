package lostfound.model;

public enum Category {
    ELECTRONICS("전자기기"),
    CARD("카드"),
    WALLET("지갑"),
    BOOK("도서"),
    STATIONERY("학용품"),
    BAG("가방"),
    CLOTHING("의류"),
    ACCESSORY("악세서리"),
    DAILY_GOODS("생활용품"),
    ETC("기타");

    private final String label; // 한글 이름으로 출력하기 위함

    Category(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
