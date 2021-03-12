package tetris;

import javax.swing.JLabel;
import javax.swing.JPanel;//
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import tetris.Shape.Tetrominoe;

public class Board extends JPanel {//JPanel 상속

	private final int BOARD_WIDTH = 10;
	private final int BOARD_HEIGHT = 22;//맵 사이즈 final로 선언
	private final int PERIOD_INTERVAL = 300;//snake에서 delay역할인듯. 난이도에 따라 빠르게도 되나?
	//난이도에 따라 속도 조절하려면 이걸 final 아니게 해야할듯.
	private Timer timer;
	private boolean isFallingFinished = false;//떨어질때마다 true로 뭔가 실행하고 다시 false로 바꿀듯
	private boolean isPaused = false;
	private int numLinesRemoved = 0;//이건 아마 점수 세기용.
	private int curX = 0;
	private int curY = 0; //테트로미노의 위친가?
	private JLabel statusbar;//snake와 달리 상황바도 존재
	private Shape curPiece;//현재 테트로미노. 아직 테트로미노 import안해도 에러 안남
	private Tetrominoe[] board;//이제 에러남. 근데 왜 배열? 보드창에 다음 테트로미노 띄우는거라 추정
	
	public Board(Tetris parent) {//새로 Tetris라는 형 사용. parent는 뭔가싶음.
		initBoard(parent);
	}
	private void initBoard(Tetris parent) {//전체 구조 보니 Tetris가 게임실행용 클래스임.
		setFocusable(true);//키 입력이 현재 화면에 포커스 되게하는것.
		statusbar = parent.getStatusBar();//getStatusBar는 Tetris에서 구현?
		addKeyListener(new TAdapter());//TAdapter는 나중에 구현
	}
	private int squareWidth() {
		return (int) getSize().getWidth() / BOARD_WIDTH;
	}//snake에서는 getPrefferedSize하고 Dimension으로 한번에 W,H값 입력했는데 다르게함.
	//화면 전체를 맵으로 쓰지 않아서 그런건가?
	private int squareHeight() {
		return (int) getSize().getHeight() / BOARD_HEIGHT;
	}//겟사이즈 겟W,H는 JPANEL에 정의되어 있는건가? 뒤에 왜 /로 나눌까?
	private Tetrominoe shapeAt(int x, int y) {
		return board[(y * BOARD_WIDTH) + x];
	}//shape 위치를 구하는 것 같은데 1도 이해불가
	void start() {
		curPiece = new Shape();//셰잎 하나 가져와서 현재걸로 사용
		board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];//보드가 왜 테트로미노?? 그리고 아까 square로 보드 만든거 아닌가?
		
		clearBoard();
		newPiece();//둘 다 아직 정의x
		
		timer = new Timer(PERIOD_INTERVAL, new GameCycle());//게임사이클 아직 정의x
		timer.start();)
	}
	
	private void pause() {
		isPaused = !isPaused;//boolean을 이렇게 하면 바로 전환가능하구나
		if(isPaused) {
			statusbar.setText("Paused");//퍼즈를 트루로 만든 후 퍼즈라고 글자띄움.
		}else {
			statusbar.setText(String.valueOf(numLinesRemoved));
		}//퍼즈 중에 누르면 스테이터스바 글자에 지운 줄 수 보여줌. 이후에 따로 statusbar구현 안하나?
		repaint();
	}
	@Override
	public void paintComponent(Graphics g) {
		//내용물 그릴때 쓰는것.
		super.paintComponent(g);
		doDrawing(g);
	}
	private void doDrawing(Graphics g) {
		var size = getSize();//JPanel의 사이즈를 구하는것?
		int boardTop = (int)size.getHeight() - BOARD_HEIGHT * squareHeight();
		//보드의 끝을 보드 높이에서 거기에 square높이를 곱한거만큼 뺌. squareH는 한번 보드높이로 나눴었으니 겟H-겟W가 됨.보드탑을 제외한 부분이 한변이 W인 정사각형이 될듯.
		for(int i=0; i< BOARD_HEIGHT;i++) {//i는 0부터 h까지
			for(int j=0; j< BOARD_WIDTH; j++) {//j는 w까지
				Tetrominoe shape = shapeAt(j, BOARD_HEIGHT -i -1);//한칸씩 내리는것 같은데, 이걸 어떻게 timer로 시간에 맞게 구현할까
				
				if(shape != Tetrominoe.NoShape) {//NoShape일때 셰잎 그리는 건데 draw네모를 구현하고 놔야 이해가능할듯.
					drawSquare(g, j*squareWidth(),
							boardTop+ i*squareHeight(), shape);
				}
			}
		}
		if(curPiece.getShape() != Tetrominoe.NoShape) {
			for(int i =0; i<4; i++) {
				int x = curX + curPiece.x(i);//x는 셰잎클래스 에서 coords4개 배열중 i번째 배열의 x.
				int y = curY - curPiece.y(i);//배열이 4개니 4번반복 for문
				
				drawSquare(g, x*squareWidth(),
						boardTop+(BOARD_HEIGHT-y-1)*squareHeight,
						curPiece.getShape());
			}
		}
	}
	
	private void dropDown() {
		int newY =curY;
		
		while (newY>0) {
			if(!tryMove(curPiece, curX, newY -1)) {
				break;
			}//움직일 수 없을때 가지인듯.
			newY--;//이게 Y를 계속 떨어뜨리는 거였음. 위에거 다시 확인 필요.
		}//그게아니고 쭉 내리는 거인듯.
		pieceDropped();
	}
	
	private void oneLineDown() {
		if(!tryMove(curPiece, curX, curY-1)) {
			pieceDropped();//움직일수 없으면 피스드롭??이해불가
		}
	}
	private void clearBoard() {
		for(int i=0; i<BOARD_HEIGHT*BOARD_WIDTH; i++) {
			board[i] = Tetrominoe.NoShape;
		}
	}
	private void pieceDropped() {//피스가 떨어졌을때 그쪽자리에 그려넣는것 같은데 아직 이해불가
		for(int i=0; i<4; i++) {
			int x = curX +curPiece.x(i);//각 칸(배열{x,y})의 x만큼 현 x에 +
			int y = curY - curPiece.y(i);//y는 그만큼 현 y에 -
			board[(y*BOARD_WIDTH)+x] = curPiece.getShape();//그 y에 너비곱하고 x더한걸 board[]안에 넣고. 구부분에 현재피스그림
		}//위의 보드배열안의 식을 모르겠다.
		removeFullLines();//다 찬 라인 삭제.
		
		if(!isFallingFinished) {//폴링이 안 끝났는데 왜 뉴피스??
			newPiece();//구현해보고 다시 봐야할듯
		}
	}
	
	private void newPiece() {
		curPiece.setRandomShape();
		curX = BOARD_WIDTH/2+1;
		curY = BOARD_HEIGHT - 1 + curPiece.minY();
		
		if(!tryMove(curPiece, curX, curY)) {//tryMove를 구현하고 나야 뭐가 이해가 될듯
			//밑에 보니 아마 새거 만들었는데 움직일 수 없는 상황인듯. 즉 겜오버
			curPiece.setShape(Tetrominoe.NoShape);//이거는 필요한가?
			timer.stop();
			var msg = String.format("Game over. Score: %d", numLinesRemoved);		
		}//%d는 정수를 집어넣는것. 위의 경우에는 numLineR.
	}
	private boolean tryMove(Shape newPiece, int newX, int newY) {
		for(int i=0; i<4; i++) {
			int x = newX + newPiece.x(i);//newX에 newPiece의 각 칸의 x값을 더한것.
			int y = newY - newPiece.y(i);
			
			if(x<0||x>= BOARD_WIDTH|| y<0 || y>= BOARD_HEIGHT) {
				return false;//가로로 맵을 나가거나 세로로 맵을 나가거나 하면 false
			}
			if(shapeAt(x,y)!= Tetrominoe.NoShape) {
				return false;//shapeAt 리턴값을 이해를 못하겠어서 이것도...
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
