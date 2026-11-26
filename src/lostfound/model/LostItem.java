package lostfound.model;

public class LostItem {
    public static final int NO_COORDINATE = -1; // 좌표 선택이 안됐음을 의미하는 상수

    private int id;
    private String name;
    private Category category;
    private String mapName;
    private String registeredDate;
    private String description;
    private String imagePath;
    private int x;
    private int y;
    private ItemStatus status;
    
    // 수령 완료시 작성되는 멤버 변수
    private String receiverName;
    private String receiverContact;
    private String returnedDate;

    public LostItem(int id, String name, Category category, String mapName, String registeredDate,
            String description, String imagePath, int x, int y, ItemStatus status,
            String receiverName, String receiverContact, String returnedDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.mapName = mapName;
        this.registeredDate = registeredDate;
        this.description = description;
        this.imagePath = imagePath;
        this.x = x;
        this.y = y;
        this.status = status;
        this.receiverName = receiverName;
        this.receiverContact = receiverContact;
        this.returnedDate = returnedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public String getMapName() {
        return mapName;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverContact() {
        return receiverContact;
    }

    public void setReceiverContact(String receiverContact) {
        this.receiverContact = receiverContact;
    }

    public String getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(String returnedDate) {
        this.returnedDate = returnedDate;
    }

    public void clearReturnInfo() { // 수령 완료에서 보관중으로 다시 변경했을 때 호출
        receiverName = "";
        receiverContact = "";
        returnedDate = "";
    }

    public boolean hasCoordinate() {
        return x != NO_COORDINATE && y != NO_COORDINATE;
    }
}
