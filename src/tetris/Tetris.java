package tetris;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Tetris extends JFrame {//jframe상속
	private JLabel statusbar;
	public Tetris() {
		initUI();
	}
	
	private void initUI() {
		
		statusbar = new  JLabel(" 0");
		add(statusbar, BorderLayout.SOUTH);
		
		var board = new Board(this);//new Board(Tetris)
		add(board);
		board.start();//이 두줄은?
		
		setTitle("Tetris");//위의 제목 테트리스
		setSize(200,400);//화면 사이즈
		setDefaultCloseOperation(EXIT_ON_CLOSE);//닫는법
		setLocationRelativeTo(null);//생성위치 따로 안 정하는듯?
	}
	
	JLabel getStatusBar() {
		return statusbar;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			var game = new Tetris();
			game.setVisible(true);
		});
	}
	
}
