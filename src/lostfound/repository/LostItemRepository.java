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
                if (line.isEmpty()) continue;
                try {
                    list.add(parseLine(line));
                } catch (RuntimeException e) { // 깨진 행 하나 때문에 프로그램이 시작되지 않는 것을 방지
                    System.err.println("잘못된 행을 건너뜁니다: " + line);
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
                unescape(v[1]),
                parseCategory(v[2]),
                unescape(v[3]),
                unescape(v[4]),
                unescape(v[5]),
                unescape(v[6]),
                Integer.parseInt(v[7]),
                Integer.parseInt(v[8]),
                parseStatus(v[9]),
                unescape(v[10]),
                unescape(v[11]),
                unescape(v[12]));
    }

    // CSV 구분자(쉼표)와 줄바꿈이 값에 들어 있어도 행이 깨지지 않도록 변환
    // \ -> \\, 쉼표 -> \c, 줄바꿈 -> \n, 캐리지리턴 -> \r
    private String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '\\': sb.append("\\\\"); break;
                case ',': sb.append("\\c"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String unescape(String s) {
        if (s.indexOf('\\') < 0) return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if (next == 'c') { sb.append(','); i++; continue; }
                if (next == 'n') { sb.append('\n'); i++; continue; }
                if (next == 'r') { sb.append('\r'); i++; continue; }
                if (next == '\\') { sb.append('\\'); i++; continue; }
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private String toCsvLine(LostItem item) { // csv 형식 변환
        return String.join(",",
                String.valueOf(item.getId()),
                escape(item.getName()),
                item.getCategory().name(),
                escape(item.getMapName()),
                escape(item.getRegisteredDate()),
                escape(item.getDescription()),
                escape(item.getImagePath()),
                Integer.toString(item.getX()),
                Integer.toString(item.getY()),
                item.getStatus().name(),
                escape(item.getReceiverName()),
                escape(item.getReceiverContact()),
                escape(item.getReturnedDate()));
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
