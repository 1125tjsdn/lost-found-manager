package lostfound.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lostfound.model.ItemStatus;
import lostfound.model.LostItem;
import lostfound.repository.LostItemRepository;

// view와 repository 사이의 중계
public class LostItemController {
    private final LostItemRepository itemRepository;
    private final List<LostItem> lostItems;

    public LostItemController() {
        itemRepository = new LostItemRepository();
        lostItems = itemRepository.findAll();
    }

    public List<LostItem> getStoredItems() {
        return getItemsByStatus(ItemStatus.STORED);
    }

    public List<LostItem> getReturnedItems() {
        return getItemsByStatus(ItemStatus.RETURNED);
    }

    private List<LostItem> getItemsByStatus(ItemStatus status) {
        List<LostItem> result = new ArrayList<>();
        for (LostItem item : lostItems) {
            if (item.getStatus() == status) {
                result.add(item);
            }
        }
        return result;
    }

    public void addItem(LostItem item, String sourceImagePath) throws IOException { // IOException은 Panel 에서 catch
        if (sourceImagePath != null && !sourceImagePath.isEmpty()) { // 사진이 선택되었다면 폴더로 복사하고 경로를 저장
            item.setImagePath(copyImageToPhotoDir(new File(sourceImagePath)));
        }
        
        item.setId(itemRepository.getNextId(lostItems));
        item.setRegisteredDate(getCurrentDateTime()); // 등록 날짜 자동 생성
        lostItems.add(item);
        itemRepository.rewriteAll(lostItems);
    }

    private String copyImageToPhotoDir(File sourceFile) throws IOException {
        File photoDir = new File(LostItemRepository.PHOTO_DIR);

        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }

        String fileName = sourceFile.getName();
        int dotIndex = fileName.lastIndexOf(".");

        String nameOnly = fileName.substring(0, dotIndex); // 파일 이름
        String extension = fileName.substring(dotIndex); // 확장자

        File targetFile = new File(
                photoDir,
                nameOnly + "_" + System.currentTimeMillis() + extension // 같은 사진이더라도 겹치지 않게 하기 위함
        );

        Files.copy(
                sourceFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING // 겹치면 덮어쓰기
        );

        return targetFile.getPath();
    }

    public List<LostItem> searchStoredItems(String keyword) {
        return searchItems(keyword, ItemStatus.STORED);
    }

    public List<LostItem> searchReturnedItems(String keyword) {
        return searchItems(keyword, ItemStatus.RETURNED);
    }

    private List<LostItem> searchItems(String keyword, ItemStatus status) {
        List<LostItem> result = new ArrayList<>();
        String lowerKeyword;
        if (keyword == null) {
            lowerKeyword = "";
        } else {
            lowerKeyword = keyword.trim().toLowerCase();
        }

        for (LostItem item : getItemsByStatus(status)) {
            if (!lowerKeyword.isEmpty() && !containsKeyword(item, lowerKeyword)) {
                continue;
            }
            result.add(item);
        }

        return result;
    }

    public void clearReturnedItems() {
        for (LostItem item : getReturnedItems()) {
            deleteImageFile(item.getImagePath());
        }

        lostItems.removeIf(item -> item.getStatus() == ItemStatus.RETURNED);
        itemRepository.rewriteAll(lostItems);
    }

    public List<LostItem> getItemsByMapName(String mapName) { // 약도에는 보관 중인 분실물만 표시
        List<LostItem> result = new ArrayList<>();
        for (LostItem item : lostItems) {
            if (item.getStatus() == ItemStatus.STORED && item.getMapName().equals(mapName)) {
                result.add(item);
            }
        }
        return result;
    }

    public void updateStatus(int id, ItemStatus status) {
        for (LostItem item : lostItems) {
            if (item.getId() == id) {
                item.setStatus(status);
                if (status != ItemStatus.RETURNED) {
                    item.clearReturnInfo();
                }
                itemRepository.rewriteAll(lostItems);
                return;
            }
        }
    }

    public void returnItem(int id, String receiverName, String receiverContact) {
        for (LostItem item : lostItems) {
            if (item.getId() == id) {
                item.setStatus(ItemStatus.RETURNED);
                item.setReceiverName(receiverName);
                item.setReceiverContact(receiverContact);
                item.setReturnedDate(getCurrentDateTime()); // 수령 날짜 자동 생성
                itemRepository.rewriteAll(lostItems);
                return;
            }
        }
    }

    public void deleteItem(int id) {
        LostItem targetItem = null;
        for (LostItem item : lostItems) {
            if (item.getId() == id) {
                targetItem = item;
                break;
            }
        }

        if (targetItem != null) {
            deleteImageFile(targetItem.getImagePath());
        }

        lostItems.removeIf(item -> item.getId() == id);
        itemRepository.rewriteAll(lostItems);
    }

    public List<String> getMapNames() {
        List<String> mapNames = new ArrayList<>();
        File mapDir = new File(LostItemRepository.MAP_DIR);
        File[] files = mapDir.listFiles();

        if (files == null) {
            return mapNames;
        }

        for (File file : files) {
            if (file.isFile() && isImageFile(file.getName())) {
                mapNames.add(removeExtension(file.getName()));
            }
        }

        Collections.sort(mapNames);
        return mapNames;
    }

    public File getMapFile(String mapName) {
        String[] extensions = {".png", ".jpg", ".jpeg"};
        for (String ext : extensions) {
            File file = new File(LostItemRepository.MAP_DIR, mapName + ext);
            if (file.exists()) return file;
        }
        return null;
    }

    private boolean containsKeyword(LostItem item, String keyword) {
        return contains(Integer.toString(item.getId()), keyword)
                || contains(item.getName(), keyword)
                || contains(item.getDescription(), keyword)
                || contains(item.getCategory().toString(), keyword)
                || contains(item.getMapName(), keyword)
                || contains(item.getRegisteredDate(), keyword)
                || contains(item.getReceiverName(), keyword)
                || contains(item.getReceiverContact(), keyword)
                || contains(item.getReturnedDate(), keyword);
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    private boolean isImageFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg");
    }

    private String removeExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private void deleteImageFile(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        File imageFile = new File(imagePath);
        if (imageFile.exists() && !imageFile.delete()) {
            System.out.println("사진 파일 삭제 실패: " + imagePath);
        }
    }

    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
