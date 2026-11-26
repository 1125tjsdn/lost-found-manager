package lostfound.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lostfound.model.Category;
import lostfound.model.ItemStatus;
import lostfound.model.LostItem;

// CSV 파일을 읽고 쓰는 저장소
public class LostItemRepository {
    public static final String DATA_DIR = "data";
    public static final String MAP_DIR = "data/maps";
    public static final String PHOTO_DIR = "data/item_photos";
    public static final String CSV_PATH = "data/lost_items.csv";

    private static final String HEADER = "id,name,category,mapName,registeredDate,description,imagePath,x,y,status,receiverName,receiverContact,returnedDate";

    public LostItemRepository() {
        initializeStorage();
    }

    private void initializeStorage() {
        new File(DATA_DIR).mkdirs();
        new File(MAP_DIR).mkdirs();
        new File(PHOTO_DIR).mkdirs();

        File file = new File(CSV_PATH);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) { // 자동으로 FileWriter.close()
                writer.write(HEADER + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<LostItem> findAll() {
        List<LostItem> list = new ArrayList<>();
        File file = new File(CSV_PATH);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // 헤더 건너뛰기
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    list.add(parseLine(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    public void rewriteAll(List<LostItem> list) { // csv는 특정 레코드만 삭제하기 어려워서 전체 삭제 후 다시 쓰는 것 일반적, 수정/삭제/삽입 시 사용
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_PATH))) {
            writer.write(HEADER);
            writer.newLine();
            for (LostItem item : list) {
                writer.write(toCsvLine(item));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LostItem parseLine(String line) {
        String[] v = line.split(",", -1); // 빈 값도 가져옴
        return new LostItem(
                Integer.parseInt(v[0]),
                v[1],
                parseCategory(v[2]),
                v[3],
                v[4], 
                v[5], 
                v[6], 
                Integer.parseInt(v[7]),
                Integer.parseInt(v[8]),
                parseStatus(v[9]),
                v[10],
                v[11],
                v[12]);
    }

    private String toCsvLine(LostItem item) { // csv 형식 변환
        return String.join(",",
                String.valueOf(item.getId()),
                item.getName(),
                item.getCategory().name(),
                item.getMapName(),
                item.getRegisteredDate(),
                item.getDescription(),
                item.getImagePath(),
                Integer.toString(item.getX()),
                Integer.toString(item.getY()),
                item.getStatus().name(),
                item.getReceiverName(),
                item.getReceiverContact(),
                item.getReturnedDate());
    }

    public int getNextId(List<LostItem> items) { 
        if (items.isEmpty()) {
            return 0;
        }
        return items.get(items.size() - 1).getId() + 1;
    }

    private Category parseCategory(String value) {
        try {
            return Category.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Category.ETC;
        }
    }

    private ItemStatus parseStatus(String value) {
        try {
            return ItemStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return ItemStatus.STORED;
        }
    }

}
