package lostfound.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import lostfound.controller.LostItemController;
import lostfound.model.LostItem;

// 분실물 위치를 지도에서 조회하는 패널
public class ViewMapPanel extends JPanel {
    private static final int MARKER_SIZE = 20;
    private static final double MAP_SCALE = 0.5; // 0.5 배로 축소

    private final LostFoundFrame frame;
    private final LostItemController controller;

    private JComboBox<String> mapBox;
    private JScrollPane mapScrollPane;

    private String selectedMapName;
    private boolean refreshingMapBox;

    public ViewMapPanel(LostFoundFrame frame, LostItemController controller) {
        this.frame = frame;
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        mapScrollPane = new JScrollPane();
        add(createControlPanel(), BorderLayout.NORTH);
        add(mapScrollPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mapBox = new JComboBox<>();
        mapBox.addActionListener(e -> onMapSelected());

        controlPanel.add(new JLabel("지도"));
        controlPanel.add(mapBox);
        return controlPanel;
    }

    private JPanel createButtonPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("뒤로가기");
        backButton.addActionListener(e -> frame.showStoredPanel());

        bottomPanel.add(backButton);
        return bottomPanel;
    }

    private void onMapSelected() {
        if (refreshingMapBox) return;
        String selected = (String) mapBox.getSelectedItem();
        if (selected != null && !selected.equals(selectedMapName)) {
            selectedMapName = selected;
            showSelectedMap();
        }
    }

    public void refreshPanel() {
        List<String> mapNames = controller.getMapNames();
        refreshingMapBox = true;
        mapBox.removeAllItems();

        if (mapNames.isEmpty()) {
            selectedMapName = null;
            showMessage("data/maps 폴더에 지도 이미지를 추가하세요.");
        } else {
            if (selectedMapName == null || !mapNames.contains(selectedMapName)) {
                selectedMapName = mapNames.get(0);
            }
            for (String mapName : mapNames) mapBox.addItem(mapName);
            mapBox.setSelectedItem(selectedMapName);
            refreshingMapBox = false;
            showSelectedMap();
        }
        refreshingMapBox = false;
    }

    private void showSelectedMap() {
        if (selectedMapName == null) {
            showMessage("선택된 지도가 없습니다.");
            return;
        }

        File mapFile = controller.getMapFile(selectedMapName);
        if (mapFile == null) {
            showMessage("지도 파일을 찾을 수 없습니다.");
            return;
        }

        ImageIcon icon = new ImageIcon(mapFile.getPath());
        ViewMapImagePanel mapPanel = new ViewMapImagePanel(icon.getImage(),
                controller.getItemsByMapName(selectedMapName));
        mapScrollPane.setViewportView(mapPanel);
    }

    private void showItemDetail(LostItem item) {
        frame.showDetailPanel(item);
    }

    private void showMessage(String message) {
        mapScrollPane.setViewportView(new MessagePanel(message));
    }

    private class ViewMapImagePanel extends JPanel {
        private final Image mapImage;
        private final List<LostItem> items;
        private final int imageWidth;
        private final int imageHeight;
        private final int displayWidth;
        private final int displayHeight;

        ViewMapImagePanel(Image mapImage, List<LostItem> items) {
            this.mapImage = mapImage;
            this.items = items;
            imageWidth = mapImage.getWidth(this);
            imageHeight = mapImage.getHeight(this);
            displayWidth = scaled(imageWidth);
            displayHeight = scaled(imageHeight);

            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(displayWidth, displayHeight));
            addMouseListener(new MarkerClickListener());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.drawImage(mapImage, 0, 0, displayWidth, displayHeight, this);
            for (LostItem item : items) {
                if (item.hasCoordinate()) {
                    drawMarker(g2, scaledX(item), scaledY(item), item.getId());
                }
            }
            g2.dispose();
        }

        private void drawMarker(Graphics2D g2, int x, int y, int id) {
            int r = MARKER_SIZE / 2;
            g2.setColor(new Color(220, 60, 60));
            g2.fillOval(x - r, y - r, MARKER_SIZE, MARKER_SIZE);
            g2.setColor(Color.WHITE);
            g2.drawOval(x - r, y - r, MARKER_SIZE, MARKER_SIZE);

            String idText = String.valueOf(id);
            java.awt.FontMetrics metrics = g2.getFontMetrics();
            int textX = x - metrics.stringWidth(idText) / 2;
            int textY = y + (metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(idText, textX, textY);
        }

        private int scaledX(LostItem item) {
            return scaled(item.getX());
        }

        private int scaledY(LostItem item) {
            return scaled(item.getY());
        }

        private int scaled(int value) {
            return (int) (value * MAP_SCALE);
        }

        private LostItem findClosestItem(int clickX, int clickY) {
            int maxDistance = (MARKER_SIZE / 2) * (MARKER_SIZE / 2);
            int closestDistance = maxDistance + 1;
            LostItem closestItem = null;

            for (LostItem item : items) {
                if (!item.hasCoordinate()) {
                    continue;
                }

                int dx = clickX - scaledX(item);
                int dy = clickY - scaledY(item);
                int distance = dx * dx + dy * dy;
                if (distance <= maxDistance && distance < closestDistance) {
                    closestDistance = distance;
                    closestItem = item;
                }
            }

            return closestItem;
        }

        private class MarkerClickListener extends MouseAdapter {
            @Override
            public void mouseClicked(MouseEvent e) {
                LostItem clickedItem = findClosestItem(e.getX(), e.getY());
                if (clickedItem != null) {
                    showItemDetail(clickedItem);
                }
            }
        }
    }

    private class MessagePanel extends JPanel {
        private final String message;

        MessagePanel(String message) {
            this.message = message;
            setPreferredSize(new Dimension(800, 500));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.DARK_GRAY);
            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g.drawString(message, Math.max(20, x), y);
        }
    }
}
