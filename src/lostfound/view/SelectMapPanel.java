package lostfound.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import lostfound.controller.LostItemController;
import lostfound.model.LostItem;

// 분실물 위치 좌표를 선택하는 패널
public class SelectMapPanel extends JPanel {
    private static final int MARKER_SIZE = 20;
    private static final double MAP_SCALE = 0.5; // 0.5 배로 축소

    private final LostFoundFrame frame;
    private final LostItemController controller;

    private String selectedMapName;
    private JComboBox<String> mapBox;
    private boolean updatingMapBox; // 코드로 콤보박스를 바꿀 때 리스너가 중복 동작하지 않도록 함
    private JScrollPane mapPane;
    private SelectMapImagePanel currentMapPanel;

    public SelectMapPanel(LostFoundFrame frame, LostItemController controller) {
        this.frame = frame;
        this.controller = controller;

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 15, 10, 15));

        mapPane = new JScrollPane();
        add(createTopPanel(), BorderLayout.NORTH);
        add(mapPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    public void setup(String mapName) {
        updatingMapBox = true;
        mapBox.removeAllItems();
        for (String name : controller.getMapNames()) mapBox.addItem(name);
        mapBox.setSelectedItem(mapName); // 등록 화면에서 고른 지도와 콤보박스를 일치시킴
        updatingMapBox = false;

        this.selectedMapName = mapName;
        changeMap(mapName);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        mapBox = new JComboBox<>();
        for (String name : controller.getMapNames()) mapBox.addItem(name);
        mapBox.addActionListener(e -> {
            if (!updatingMapBox) changeMap((String) mapBox.getSelectedItem());
        });

        controlPanel.add(new JLabel("지도 선택"));
        controlPanel.add(mapBox);

        topPanel.add(controlPanel, BorderLayout.NORTH);
        return topPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("취소");
        JButton applyBtn = new JButton("이 위치로 결정");

        cancelBtn.addActionListener(e -> frame.showRegisterPanel());
        applyBtn.addActionListener(e -> onApply());

        panel.add(cancelBtn);
        panel.add(applyBtn);
        return panel;
    }

    private void changeMap(String mapName) {
        File mapFile = controller.getMapFile(mapName);
        if (mapFile == null) return;

        selectedMapName = mapName;
        ImageIcon mapIcon = new ImageIcon(mapFile.getPath());
        currentMapPanel = new SelectMapImagePanel(mapIcon.getImage(),
                controller.getItemsByMapName(selectedMapName));

        mapPane.setViewportView(currentMapPanel);
    }

    private void onApply() {
        if (currentMapPanel == null || !currentMapPanel.hasSelection()) {
            JOptionPane.showMessageDialog(this, "지도 위에서 분실 장소를 클릭해주세요.");
            return;
        }

        frame.returnToRegisterWithCoordinate(selectedMapName,
                currentMapPanel.getSelectedX(), currentMapPanel.getSelectedY());
    }

    private class SelectMapImagePanel extends JPanel {
        private final Image mapImage;
        private final List<LostItem> items;
        private final int imageWidth;
        private final int imageHeight;
        private final int displayWidth;
        private final int displayHeight;
        private int selectedMapX;
        private int selectedMapY;
        private boolean selected;

        SelectMapImagePanel(Image mapImage, List<LostItem> items) {
            this.mapImage = mapImage;
            this.items = items;
            imageWidth = mapImage.getWidth(this);
            imageHeight = mapImage.getHeight(this);
            displayWidth = scaled(imageWidth);
            displayHeight = scaled(imageHeight);

            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(displayWidth, displayHeight));
            addMouseListener(new CoordinateClickListener());
        }

        boolean hasSelection() {
            return selected;
        }

        int getSelectedX() {
            return selectedMapX;
        }

        int getSelectedY() {
            return selectedMapY;
        }

        private void selectCoordinate(MouseEvent e) {
            selectedMapX = clampToImageX(e.getX());
            selectedMapY = clampToImageY(e.getY());
            selected = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.drawImage(mapImage, 0, 0, displayWidth, displayHeight, this);
            drawItemMarkers(g2);
            if (selected) {
                drawMarker(g2, scaled(selectedMapX), scaled(selectedMapY), "");
            } else {
                drawCenterMessage(g2, "클릭해서 위치 선택");
            }
            g2.dispose();
        }

        private void drawItemMarkers(Graphics2D g2) {
            for (LostItem item : items) {
                if (item.hasCoordinate()) {
                    drawMarker(g2, scaled(item.getX()), scaled(item.getY()), String.valueOf(item.getId()));
                }
            }
        }

        private void drawMarker(Graphics2D g2, int x, int y, String text) {
            int r = MARKER_SIZE / 2;
            g2.setColor(new Color(220, 60, 60));
            g2.fillOval(x - r, y - r, MARKER_SIZE, MARKER_SIZE);
            g2.setColor(Color.WHITE);
            g2.drawOval(x - r, y - r, MARKER_SIZE, MARKER_SIZE);
            if (!text.isEmpty()) {
                java.awt.FontMetrics metrics = g2.getFontMetrics();
                int textX = x - metrics.stringWidth(text) / 2;
                int textY = y + (metrics.getAscent() - metrics.getDescent()) / 2;
                g2.drawString(text, textX, textY);
            }
        }

        private int scaled(int value) {
            return (int) (value * MAP_SCALE);
        }

        private int clampToImageX(int displayX) {
            return Math.max(0, Math.min(imageWidth, (int) Math.round(displayX / MAP_SCALE)));
        }

        private int clampToImageY(int displayY) {
            return Math.max(0, Math.min(imageHeight, (int) Math.round(displayY / MAP_SCALE)));
        }

        private void drawCenterMessage(Graphics g, String message) {
            g.setColor(new Color(0, 0, 0, 150));
            java.awt.FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g.drawString(message, Math.max(20, x), y);
        }

        private class CoordinateClickListener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectCoordinate(e);
            }
        }
    }
}
