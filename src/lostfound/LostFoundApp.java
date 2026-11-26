package lostfound;

import javax.swing.SwingUtilities;

import lostfound.view.LostFoundFrame;

// 프로그램 시작점
public class LostFoundApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // Swing 화면은 EDT(Event Dispatch Thread)에서 실행해야 안전
            new LostFoundFrame().setVisible(true);
        });
    }
}	
