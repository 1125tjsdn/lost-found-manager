package lostfound.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import lostfound.controller.LostItemController;
import lostfound.model.Category;
import lostfound.model.ItemStatus;
import lostfound.model.LostItem;

// 분실물 등록 패널
public class RegisterPanel extends JPanel {
    private final LostFoundFrame frame;
    private final LostItemController controller;

    private JTextField nameField;
    private JComboBox<Category> categoryBox;
    private JComboBox<String> mapBox;
    private JTextArea descriptionArea; // 비고 
    private JLabel imageStatusLabel; // 사진 선택 유무를 알려주는 라벨
    private JLabel coordinateLabel; // 지도 선택 유무를 알려주는 라벨

    // 일시적으로 저장해야하는 값들, 사용자가 선택 후 등록하기를 누르지 않을 수도 있기 때문
    private String selectedImagePath = ""; // 이미지 위치 
    private int selectedX = LostItem.NO_COORDINATE; // x좌표 값
    private int selectedY = LostItem.NO_COORDINATE; // y좌표 값
    private String coordinateMapName = null; // 좌표를 찍은 지도 이름, 지도를 바꾸면 좌표가 맞지 않으므로 초기화하기 위함

    public RegisterPanel(LostFoundFrame frame, LostItemController controller) {
        this.frame = frame;
        this.controller = controller;

        initLayout(); // 레이아웃 설정
        initForm(); // 입력 양식(이름, 분류, 지도 등) 배치
        initBottomPanel(); // 하단 패널 배치
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10)); // 화면을 위, 가운데, 오른쪽 ,아래, 왼쪽으로 분할 배치
        setBorder(new EmptyBorder(20, 40, 20, 40)); // 패널 안쪽 가장자리에 빈 여백(위, 왼쪽, 아래, 오른쪽)
    }

    private void initForm() {
        JPanel formPanel = new JPanel(new GridBagLayout()); // 행과 열을 기준으로 세밀하게 배치하는 레이아웃
        
        nameField = new JTextField(24);
        categoryBox = new JComboBox<>(Category.values());
        mapBox = new JComboBox<>();
        for (String mapName : controller.getMapNames()) {
            mapBox.addItem(mapName);
        }
        mapBox.addActionListener(e -> resetCoordinateIfMapChanged());
        descriptionArea = new JTextArea(5, 24); // 높이, 넓이
        imageStatusLabel = new JLabel("선택 안 됨");
        coordinateLabel = new JLabel("선택 안 됨");

        int row = 0;
        addRow(formPanel, row++, "물건명", nameField);
        addRow(formPanel, row++, "분류", categoryBox);
        addRow(formPanel, row++, "지도", mapBox);
        addRow(formPanel, row++, "비고", new JScrollPane(descriptionArea));
        addRow(formPanel, row++, "사진", createImagePanel());
        addRow(formPanel, row++, "지도 좌표", createCoordinatePanel());

        add(formPanel, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, int row, String labelText, java.awt.Component component) { // GridBagLayout 설정 함수
        GridBagConstraints labelGbc = new GridBagConstraints(); // GridBagLayout의 설정, 라벨 설정
        labelGbc.gridx = 0; labelGbc.gridy = row; // 0번째 열, row번째 행 배치
        panel.add(new JLabel(labelText), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints(); // GridBagLayout의 설정, 필드 설정
        fieldGbc.gridx = 1; fieldGbc.gridy = row; // 1번째 열, row번째 행 배치
        fieldGbc.weightx = 1.0; // 남는 공간을 가져감
        fieldGbc.fill = GridBagConstraints.HORIZONTAL; // 수직방향으로 늘어남
        fieldGbc.insets = new Insets(6, 6, 6, 6); //  입력 컴포넌트 주변 여백, 위 왼쪽 아래 오른쪽
        panel.add(component, fieldGbc);
    }
    
    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        JButton imageSelectButton = new JButton("사진 선택");
        imageSelectButton.addActionListener(e -> selectImage());
        
        panel.add(imageSelectButton);
        panel.add(imageStatusLabel);
        return panel;
    }

    private JPanel createCoordinatePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        JButton coordinateSelectButton = new JButton("좌표 선택");
        coordinateSelectButton.addActionListener(e -> openCoordinateSelectPanel());
        
        panel.add(coordinateSelectButton);
        panel.add(coordinateLabel);
        return panel;
    }

    private void initBottomPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("뒤로가기");
        JButton submitButton = new JButton("등록");

        backButton.addActionListener(e -> frame.showStoredPanel());
        submitButton.addActionListener(e -> submitItem());

        buttonPanel.add(backButton);
        buttonPanel.add(submitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void submitItem() {
        String mapName = (String)mapBox.getSelectedItem();
        if (!validateInput(mapName)) return;

        try {
        	LostItem newItem = new LostItem(
        	        0,
        	        nameField.getText(),
        	        (Category)categoryBox.getSelectedItem(),
        	        mapName,
        	        "",
        	        descriptionArea.getText(),
        	        "",
        	        selectedX,
        	        selectedY,
        	        ItemStatus.STORED,
        	        "",
        	        "",
        	        ""
        	);
            
            controller.addItem(newItem, selectedImagePath);
            completeRegistration();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "등록 중 오류 발생: " + ex.getMessage());
        }
    }
    
    private void selectImage() {
        // macOS 한글 입력기 버그, 한글 입력 중에 사진 선택 버튼을 누르면 이후 영어로 바꾸기 전까지 멈춤 해결하기 위해 입력기 종료
        if (getInputContext() != null) getInputContext().endComposition();

        JFileChooser chooser = new JFileChooser(); // 파일 선택창
        chooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "png", "jpg", "jpeg"));

        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) { // 파일 선택창을 연 후, 파일을 선택했다면
            selectedImagePath = chooser.getSelectedFile().getPath();
            imageStatusLabel.setText("선택됨");
        }
    }

    private void openCoordinateSelectPanel() {
        String mapName = (String)mapBox.getSelectedItem();
        if (mapName == null) {
            JOptionPane.showMessageDialog(frame, "먼저 지도를 선택하거나 data/maps 폴더를 확인하세요.");
            return;
        }

        frame.showCoordinateSelectPanel(mapName);
    }

    public void setCoordinate(String mapName, int x, int y) {
        coordinateMapName = mapName; // 콤보박스를 바꾸기 전에 설정해야 좌표가 초기화되지 않음
        mapBox.setSelectedItem(mapName);
        this.selectedX = x;
        this.selectedY = y;
        coordinateLabel.setText("선택됨");
    }

    // 좌표를 찍은 뒤 다른 지도로 바꾸면 이전 지도의 좌표가 남지 않도록 초기화
    private void resetCoordinateIfMapChanged() {
        if (coordinateMapName != null && !coordinateMapName.equals(mapBox.getSelectedItem())) {
            selectedX = LostItem.NO_COORDINATE;
            selectedY = LostItem.NO_COORDINATE;
            coordinateMapName = null;
            coordinateLabel.setText("선택 안 됨");
        }
    }

    private boolean validateInput(String mapName) {
        if (mapName == null) {
            JOptionPane.showMessageDialog(frame, "지도 파일이 없습니다.");
            return false;
        }
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "물건명을 입력하세요.");
            return false;
        }
        return true;
    }

    private void completeRegistration() {
        clearFields();
        JOptionPane.showMessageDialog(frame, "분실물이 등록되었습니다.");
        frame.showStoredPanel();
    }

    private void clearFields() {
        nameField.setText("");
        descriptionArea.setText("");
        selectedImagePath = "";
        selectedX = LostItem.NO_COORDINATE;
        selectedY = LostItem.NO_COORDINATE;
        coordinateMapName = null;
        imageStatusLabel.setText("선택 안 됨");
        coordinateLabel.setText("선택 안 됨");
    }
}
