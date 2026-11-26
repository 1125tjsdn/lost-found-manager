package lostfound.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import lostfound.controller.LostItemController;
import lostfound.model.ItemStatus;
import lostfound.model.LostItem;

// 보관 중, 수령 완료 패널
public class ItemTablePanel extends JPanel {
    private final LostFoundFrame frame;
    private final LostItemController controller;
    private final ItemStatus mode; // STORED(보관 중) or RETURNED(수령 완료)

    private JTextField keywordField; // 검색어 입력 필드 
    private DefaultTableModel tableModel; 
    private JTable table;
    private List<LostItem> currentItems;

    public ItemTablePanel(LostFoundFrame frame, LostItemController controller, ItemStatus mode) {
        this.frame = frame;
        this.controller = controller;
        this.mode = mode;

        initLayout(); // 배치 설정
        initTopPanel(); // 상단 패널
        initTable(); // 중앙 테이블
        initBottomPanel(); // 하단 패널 

        refreshPanel();
    }

    private void initLayout() {
    	 setLayout(new BorderLayout(10, 10)); // 컴포넌트 사이 간격
         setBorder(new EmptyBorder(20, 25, 20, 25)); // new EmptyBorder(위, 왼쪽, 아래, 오른쪽) : n px만큼 여백 삽입
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 위쪽 패널을 만들고 왼쪽 정렬

        keywordField = new JTextField(16); // 16칸 검색 필드
        JButton searchButton = new JButton("검색");
        JButton resetButton = new JButton("초기화");
        
        searchButton.addActionListener(e -> search());
        resetButton.addActionListener(e -> resetSearch());
        
        topPanel.add(new JLabel("검색어"));
        topPanel.add(keywordField);
        topPanel.add(searchButton);
        topPanel.add(resetButton);
        add(topPanel, BorderLayout.NORTH); // 위쪽에 붙임
    }

    private void initTable() {
        String[] columnNames;
        if (mode == ItemStatus.STORED) {
            columnNames = new String[] {"ID", "물건명", "분류", "습득 위치", "등록 날짜", "비고"};
        } else {
            columnNames = new String[] {"ID", "물건명", "분류", "습득 위치", "찾아간 사람", "수령 날짜", "비고"};
        }

        tableModel = new DefaultTableModel(columnNames, 0) { // 처음 행의 갯수는 0
            @Override public boolean isCellEditable(int r, int c) { return false; } 
            // 테이블 칸을 사용자가 직접 수정하지 못하게 하는 코드
        };
        
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); // column name 클릭시 table 정렬 기능 true
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 하나만 선택
        
        add(new JScrollPane(table), BorderLayout.CENTER); // Scroll 가능한 영역을 만든 후, 가운데 배치
    }

    private void initBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // 아래 쪽 패널, 오른쪽 정렬
        JButton detailButton = new JButton("상세보기");
        
        detailButton.addActionListener(e -> {
            LostItem selectedItem = getSelectedItem();
            if (selectedItem == null) return; // 선택된 아이템 없음
            frame.showDetailPanel(selectedItem);
            table.clearSelection(); // 테이블에서 선택된 행 표시를 없애는 코드
        });
        
        bottomPanel.add(detailButton);
        
        if (mode == ItemStatus.STORED) { // 보관중
            JButton registerButton = new JButton("분실물 등록");
            JButton mapButton = new JButton("지도 보기");
            JButton returnedButton = new JButton("수령 완료 목록");
            
            registerButton.addActionListener(e -> frame.showRegisterPanel());
            mapButton.addActionListener(e -> frame.showMapPanel());
            returnedButton.addActionListener(e -> frame.showReturnedPanel());
            
            bottomPanel.add(registerButton);
            bottomPanel.add(mapButton);
            bottomPanel.add(returnedButton);
        } else { // 수령완료
            JButton clearButton = new JButton("목록 비우기");
            JButton backButton = new JButton("뒤로가기");
            clearButton.addActionListener(e -> clearReturnedItems());
            backButton.addActionListener(e -> frame.showStoredPanel());
            bottomPanel.add(clearButton);
            bottomPanel.add(backButton);
        }
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshPanel() { // 값이 변경되었을 경우 새로고침
        List<LostItem> items;
        
        if (mode == ItemStatus.STORED) {
            items = controller.getStoredItems();
        } else {
            items = controller.getReturnedItems();
        }
        
        showItems(items);
    }

    private void search() {
        if (mode == ItemStatus.STORED) {
            showItems(controller.searchStoredItems(keywordField.getText()));
        } else {
            showItems(controller.searchReturnedItems(keywordField.getText()));
        }
    }

    private void resetSearch() {
        keywordField.setText("");
        table.getRowSorter().setSortKeys(null);
        
        if (mode == ItemStatus.STORED) {
            showItems(controller.getStoredItems());
        } else {
            showItems(controller.getReturnedItems());
        }
    }

    private void clearReturnedItems() {
    	int result = JOptionPane.showConfirmDialog(this, "수령 완료 목록을 비우시겠습니까?", "목록 비우기",
                JOptionPane.YES_NO_OPTION); // 이 창을 기준으로 dialog 띄움, 내용, 제목
        if (result == JOptionPane.YES_OPTION) { 
            controller.clearReturnedItems();
            resetSearch();
        }
    }

    private void showItems(List<LostItem> items) {
        currentItems = items;
        table.clearSelection();
        tableModel.setRowCount(0);
        
        for (LostItem item : items) {
            if (mode == ItemStatus.STORED) {
                tableModel.addRow(new Object[] {
                    item.getId(), item.getName(), item.getCategory(), item.getMapName(),
                    item.getRegisteredDate(), item.getDescription()
                });
            } else {
                tableModel.addRow(new Object[] {
                    item.getId(), item.getName(), item.getCategory(), item.getMapName(),
                    item.getReceiverName(),
                    item.getReturnedDate(),
                    item.getDescription()
                });
            }
        }
    }

    // 화면에 보이는 순서와 실제 데이터 순서가 달라질 수 있음 (필드를 눌러 정렬하였을 때)
    // convertRowIndexToModel(번호) 를 사용하여 실제 데이터 번호로 변환함
    private LostItem getSelectedItem() {
        int selectedRow = table.getSelectedRow();
        
        if (selectedRow == -1) { // 아무 행도 선택 안됨
            return null;
        }
        
        int modelRow = table.convertRowIndexToModel(selectedRow);
        return currentItems.get(modelRow);
    }

}
