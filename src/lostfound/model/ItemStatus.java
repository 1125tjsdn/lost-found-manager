package lostfound.model;

// - 분실물 보관 상태 값
// - 보관 중/수령 완료를 구분
public enum ItemStatus {
    STORED("보관 중"),
    RETURNED("수령 완료");

    private final String label;

    ItemStatus(String label) { // 한글 이름으로 출력하기 위함
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
