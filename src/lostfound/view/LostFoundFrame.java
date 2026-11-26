package lostfound.view;

import java.awt.CardLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import lostfound.controller.LostItemController;
import lostfound.model.ItemStatus;
import lostfound.model.LostItem;

// 프로그램의 껍데기 + 화면 전환 관리, 프로그램의 흐름 담당
// 각각의 패널은 서로를 모르도록 설계

public class LostFoundFrame extends JFrame { 
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    private RegisterPanel registerPanel;
    private ItemTablePanel storedPanel;
    private ViewMapPanel viewMapPanel;
    private ItemTablePanel returnedPanel;
    private ItemDetailPanel detailPanel;
    private SelectMapPanel selectMapPanel;

    public LostFoundFrame() {
        initWindow();
        initComponents();
        showStoredPanel(); // ItemTablePanel의 Stored 모드 패널을 우선 띄움
    }

    private void initWindow() {
        // applyGlobalFont(); // 맥북 환경에서 스윙 사용할 때 글씨 깨짐 버그 
        
        setTitle("분실물 관리 프로그램");
        setSize(1000, 700); // 프레임 사이즈
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창의 X 버튼을 눌렀을 때 프로그램을 완전히 종료하라는 뜻
        setLocationRelativeTo(null); // 창을 화면 가운데에 띄우라는 뜻
    }

    private void initComponents() {
        LostItemController controller = new LostItemController(); // 컨트롤러 객체는 하나만 생성해서 파라미터로 넘김
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        registerPanel = new RegisterPanel(this, controller);
        storedPanel = new ItemTablePanel(this, controller, ItemStatus.STORED);
        viewMapPanel = new ViewMapPanel(this, controller);
        returnedPanel = new ItemTablePanel(this, controller, ItemStatus.RETURNED);
        detailPanel = new ItemDetailPanel(this, controller);
        selectMapPanel = new SelectMapPanel(this, controller);

        cardPanel.add(registerPanel, "REGISTER");
        cardPanel.add(storedPanel, "STORED");
        cardPanel.add(viewMapPanel, "MAP");
        cardPanel.add(returnedPanel, "RETURNED");
        cardPanel.add(detailPanel, "DETAIL");
        cardPanel.add(selectMapPanel, "COORD_SELECT");

        add(cardPanel);
    }

    public void showRegisterPanel() {
        showPanel("REGISTER");
    }

    public void showStoredPanel() {
        storedPanel.refreshPanel();
        showPanel("STORED");
    }

    public void showMapPanel() {
        viewMapPanel.refreshPanel();
        showPanel("MAP");
    }

    public void showReturnedPanel() {
        returnedPanel.refreshPanel();
        showPanel("RETURNED");
    }

    public void showDetailPanel(LostItem item) {
        detailPanel.setItem(item);
        showPanel("DETAIL");
    }

    public void showCoordinateSelectPanel(String mapName) {
        selectMapPanel.setup(mapName);
        showPanel("COORD_SELECT");
    }

    // SelectMapPanel 에서 고른 좌표값을 가지고 RegisterPanel로 돌아가기 위한 함수
    // 각 패널은 서로 모르게 설계해야 하기 떄문에 Frame에 선언
    public void returnToRegisterWithCoordinate(String mapName, int x, int y) {
        registerPanel.setCoordinate(mapName, x, y);
        showPanel("REGISTER");
    }

    // cardLayout을 통해 특정 Panel을 보여주는 함수
    private void showPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);
    }
    
    private void applyGlobalFont() { // 맥북에서 글씨 깨짐 해결을 위한 폰트 설정변경, 필요시 사용
        String[] components = { // 사용하는 컴포넌트들
            "Label.font", "Button.font", "Table.font", "TableHeader.font",
            "TextField.font", "TextArea.font", "ComboBox.font", "List.font"
        };
        
        for (String comp : components) {
           UIManager.put(comp, new Font("맑은 고딕", Font.PLAIN, 14));
        }
    }
}
