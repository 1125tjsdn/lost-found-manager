package lostfound.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import lostfound.controller.LostItemController;
import lostfound.model.ItemStatus;
import lostfound.model.LostItem;

/**
 * [분실물 상세 정보 패널]
 */
public class ItemDetailPanel extends JPanel {
    private static final int IMAGE_WIDTH = 360;
    private static final int IMAGE_HEIGHT = 280;

    private final LostFoundFrame frame;
    private final LostItemController controller;
    
    private LostItem currentItem;

    private JPanel infoContainer; 
    private JLabel imageLabel;    
    private JPanel buttonPanel;   

    public ItemDetailPanel(LostFoundFrame frame, LostItemController controller) {
        this.frame = frame;
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 40, 20, 40));

        imageLabel = new JLabel("사진 없음", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(400, 320));
        imageLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        infoContainer = new JPanel(new BorderLayout(0, 10));
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        add(imageLabel, BorderLayout.WEST);
        add(infoContainer, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setItem(LostItem item) {
        this.currentItem = item;
        refreshPanel();
    }

    public void refreshPanel() {
        if (currentItem == null) return;
        updateImage();
        updateInfo();
        updateButtons();
        revalidate();
        repaint();
    }

    private void updateImage() {
        imageLabel.setIcon(null);
        imageLabel.setText("사진 없음");
        String path = currentItem.getImagePath();
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getPath());
                Image scaled = scaleImage(icon, IMAGE_WIDTH, IMAGE_HEIGHT);
                imageLabel.setText("");
                imageLabel.setIcon(new ImageIcon(scaled));
            }
        }
    }

    private void updateInfo() {
        infoContainer.removeAll();
        JPanel infoSection = new JPanel(new GridLayout(0, 2, 10, 8));
        addInfoRow(infoSection, "ID", String.valueOf(currentItem.getId()));
        addInfoRow(infoSection, "물건명", currentItem.getName());
        addInfoRow(infoSection, "분류", currentItem.getCategory().toString());
        addInfoRow(infoSection, "습득 위치", currentItem.getMapName());
        addInfoRow(infoSection, "등록 날짜", currentItem.getRegisteredDate());
        addInfoRow(infoSection, "현재 상태", currentItem.getStatus().toString());

        if (currentItem.getStatus() == ItemStatus.RETURNED) {
            addInfoRow(infoSection, "찾아간 사람", currentItem.getReceiverName());
            addInfoRow(infoSection, "연락처", currentItem.getReceiverContact());
            addInfoRow(infoSection, "수령 일시", currentItem.getReturnedDate());
        }
        infoContainer.add(infoSection, BorderLayout.NORTH);
        infoContainer.add(createDescriptionSection(currentItem.getDescription()), BorderLayout.CENTER);
    }

    private JPanel createDescriptionSection(String description) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        JLabel label = new JLabel("비고");
        label.setForeground(java.awt.Color.GRAY);
        panel.add(label, BorderLayout.NORTH);
        JTextArea area = new JTextArea(description);
        area.setLineWrap(true); area.setWrapStyleWord(true); area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(0, 150));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void updateButtons() {
        buttonPanel.removeAll();
        buttonPanel.add(createStatusButton());
        JButton deleteBtn = new JButton("삭제");
        deleteBtn.addActionListener(e -> onDeleteAction());
        buttonPanel.add(deleteBtn);
        JButton backBtn = new JButton("뒤로가기");
        backBtn.addActionListener(e -> {
            if (currentItem.getStatus() == ItemStatus.STORED) frame.showStoredPanel();
            else frame.showReturnedPanel();
        });
        buttonPanel.add(backBtn);
    }

    private JButton createStatusButton() {
        if (currentItem.getStatus() == ItemStatus.STORED) {
            JButton btn = new JButton("수령 완료 처리");
            btn.addActionListener(e -> onProcessReturn());
            return btn;
        } else {
            JButton btn = new JButton("복원 (보관 중으로)");
            btn.addActionListener(e -> onProcessRestore());
            return btn;
        }
    }

    private void onProcessReturn() {
        JTextField nameF = new JTextField(16);
        JTextField contactF = new JTextField(16);
        JPanel p = new JPanel(new GridLayout(0, 1, 2, 2));
        p.add(new JLabel("찾아간 사람")); p.add(nameF);
        p.add(new JLabel("연락처 (숫자 11자리)")); p.add(contactF);
        int res = JOptionPane.showConfirmDialog(this, p, "수령 정보 입력", JOptionPane.OK_CANCEL_OPTION);
        String name = nameF.getText().trim();
        String contact = contactF.getText().trim();
        if (res == JOptionPane.OK_OPTION && validateReturnInput(name, contact)) {
            String formattedContact = contact.substring(0, 3) + "-" + contact.substring(3, 7) + "-" + contact.substring(7);
            controller.returnItem(currentItem.getId(), name, formattedContact);
            JOptionPane.showMessageDialog(this, "수령 완료 처리가 되었습니다.");
            frame.showReturnedPanel();
        }
    }

    private boolean validateReturnInput(String n, String c) {
        if (n.isEmpty() || c.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름과 연락처를 입력하세요."); return false;
        }
        if (c.length() != 11) {
            JOptionPane.showMessageDialog(this, "연락처는 숫자 11자리여야 합니다."); return false;
        }
        for (int i = 0; i < c.length(); i++) {
            char ch = c.charAt(i);
            if (ch < '0' || ch > '9') {
                JOptionPane.showMessageDialog(this, "연락처는 숫자만 입력하세요."); return false;
            }
        }
        return true;
    }

    private void onProcessRestore() {
        if (JOptionPane.showConfirmDialog(this, "보관 중 상태로 되돌릴까요?", "복원 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.updateStatus(currentItem.getId(), ItemStatus.STORED);
            JOptionPane.showMessageDialog(this, "복원되었습니다.");
            frame.showStoredPanel();
        }
    }

    private void onDeleteAction() {
        if (JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.deleteItem(currentItem.getId());
            JOptionPane.showMessageDialog(this, "삭제되었습니다.");
            frame.showStoredPanel();
        }
    }

    private void addInfoRow(JPanel panel, String key, String value) {
        JLabel k = new JLabel(key); k.setForeground(java.awt.Color.GRAY);
        String displayValue;
        if (value == null) {
            displayValue = "-";
        } else {
            displayValue = value;
        }
        panel.add(k); panel.add(new JLabel(displayValue));
    }

    private Image scaleImage(ImageIcon icon, int maxWidth, int maxHeight) {
        int w = icon.getIconWidth(), h = icon.getIconHeight();
        double s = Math.min((double) maxWidth / w, (double) maxHeight / h);
        return icon.getImage().getScaledInstance((int)(w*s), (int)(h*s), Image.SCALE_SMOOTH);
    }
}
