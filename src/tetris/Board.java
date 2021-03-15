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
	}//겟사이즈 겟W,H는 JPANEL에 정의되어 있는건가? 뒤에 왜 /로 나눌까?/@한 칸의 높이로 추정
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
		}//퍼즈 중에 누르면 스테이터스바 글자에 지운 줄 수 보여줌. 이후에 따로 statusbar구현 안할듯
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
		//보드의 끝을 보드 높이에서 거기에 square높이를 곱한거만큼 뺌. squareH는 겟사이즈/보드h이니 위 식은 size.겟h-getsize.겟h인데 뭔차인지 모르겠음. 이름을 보면 게임판을 뺀 스테이터스 창인거같긴함
		for(int i=0; i< BOARD_HEIGHT;i++) {//i는 0부터 h까지
			for(int j=0; j< BOARD_WIDTH; j++) {//j는 w까지
				Tetrominoe shape = shapeAt(j, BOARD_HEIGHT -i -1);//한칸씩 내리는것 같은데, 이걸 어떻게 timer로 시간에 맞게 구현할까
				//@@@내리는게 아니고 각 칸을 색칠하는 것ㄴ듯.
				if(shape != Tetrominoe.NoShape) {//NoShape일때 셰잎 그리는 건데 draw네모를 구현하고 놔야 이해가능할듯.
					drawSquare(g, j*squareWidth(),
							boardTop+ i*squareHeight(), shape);
				}
			}
		}
		if(curPiece.getShape() != Tetrominoe.NoShape) {//현재 피스가 노셰잎이 아니면
			for(int i =0; i<4; i++) {
				int x = curX + curPiece.x(i);//x는 셰잎클래스 에서 coords4개 배열중 i번째 배열의 x.
				int y = curY - curPiece.y(i);//배열이 4개니 4번반복 for문
				
				drawSquare(g, x*squareWidth(),
						boardTop+(BOARD_HEIGHT-y-1)*squareHeight,
						curPiece.getShape());//현재 피스 그림
			}
		}
	}
	
	private void dropDown() {
		int newY =curY;
		
		while (newY>0) {
			if(!tryMove(curPiece, curX, newY -1)) {
				break;
			}//움직일 수 없을때 가지인듯.@@아래로 움직일수 없을때까지
			newY--;//이게 Y를 계속 떨어뜨리는 거였음. 위에거 다시 확인 필요.
		}//그게아니고 쭉 내리는 거인듯.
		pieceDropped();
	}//@@쭉 내리는거 맞음. space키 눌렀을때 쭉 내리는것.
	
	private void oneLineDown() {
		if(!tryMove(curPiece, curX, curY-1)) {
			pieceDropped();//움직일수 없으면 피스드롭??이해불가
		}//@한 줄 내렸을때 움직일 수 없으면 드롭된 상태라고 알려주는것. 즉 새 피스를 만들어야되니 현재 피스 상태를 드롭드로 처리
	}//@@아님. d키 눌렀을때 실행하는데 위의 dropdown하고 뭐가 다른지 몰겠음.
	private void clearBoard() {
		for(int i=0; i<BOARD_HEIGHT*BOARD_WIDTH; i++) {
			board[i] = Tetrominoe.NoShape;
		}//말 그대로 보드의 모든 칸을 노셰잎으로 초기화
	}
	private void pieceDropped() {//피스가 떨어졌을때 그쪽자리에 그려넣는것 같은데 아직 이해불가
		for(int i=0; i<4; i++) {
			int x = curX +curPiece.x(i);//각 칸(배열{x,y})의 x만큼 현 x에 +
			int y = curY - curPiece.y(i);//y는 그만큼 현 y에 -
			board[(y*BOARD_WIDTH)+x] = curPiece.getShape();//그 y에 너비곱하고 x더한걸 board[]안에 넣고. 구부분에 현재피스그림
		}//위의 보드배열안의 식을 모르겠다.
		//@@@보드의 정해진 위치에 현재피스.겟셰잎으로 배열을 가져와서 채움
		//그런데 for문으로 현재 피스의 각 칸을 가져와 놓고 왜 겟셰잎을 쓰는걸까? 겟 셰잎은 칸이 아니라 하나의 덩어리아닌가?
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
			//@@새거 만들었는데 움직일 수 없는게 아니라 새거 만드는 최초 칸으로 설정하는게 불가능(다 차있는 상태)할때.
			curPiece.setShape(Tetrominoe.NoShape);//이거는 필요한가?
			timer.stop();
			var msg = String.format("Game over. Score: %d", numLinesRemoved);		
		}//%d는 정수를 집어넣는것. 위의 경우에는 numLineR.
	}
	private boolean tryMove(Shape newPiece, int newX, int newY) {
		for(int i=0; i<4; i++) {
			int x = newX + newPiece.x(i);//newX에 newPiece의 각 칸의 x값을 더한것.
			int y = newY - newPiece.y(i);
			//@@즉 원래 x,y(newPiece.x,y(i))를 입력한newX,Y를 통해 새로운 int x,y로 움직이는것.
			if(x<0||x>= BOARD_WIDTH|| y<0 || y>= BOARD_HEIGHT) {
				return false;//가로로 맵을 나가거나 세로로 맵을 나가거나 하면 false
			}
			if(shapeAt(x,y)!= Tetrominoe.NoShape) {
				return false;//shapeAt 리턴값을 이해를 못하겠어서 이것도...
			}//이게 이미 존재하는 블럭에 충돌하면 못움직이게 하는 식 같은데, 식을 제대로 이해못하겠음. tetrominoe.Noshape은 빈칸인것같은데. 확실하게 이해가 안됨
		}
		curPiece = new Piece;
		curX = newX;
		curY = newY;
		
		repaint();
		
		return true;
	}
	private void removeFullLines() {
		int numFullLines = 0;
		
		for(int i =BOARD_HEIGHT-1; i>=0; i--) {
			boolean lineIsFull = true;//일단 트루로 설정
			
			for(int j=0; j< BOARD_WIDTH; j++) {
				if(shapeAt(j,i)== Tetrominoe.NoShape) {
					lineIsFull = false;//모든 라인의 가로 칸들중에
					break;//노셰잎인 칸이 있으면 라인is풀이 아님. 즉 노셰잎은 빈칸이 맞았음
				}
			}
			
			if(lineIsFull) {
				numFullLines++;
				
				for(int k =i; k< BOARD_HEIGHT -1; k++) {//방금 삭제한 위의 줄들. k가 y역할, j가 x역할
					for(int j =0; j<BOARD_WIDTH;j++) {
						board[(k*BOARD_WIDTH)+j] = shapeAt(j,k+1);
					}//k+1, 즉 y+1의 위치에 있는것을, 그냥 y로 내림. 즉 한칸씩 내려주는것.
				}//다 알겠는데 밑에서 위로 훑으면서 진행되는 for문이면 체크해야될 두 줄이 붙어있을때 생략되는일은 없나? 한칸 내리고 다음 줄 체크할건데?
			}//@@아 다시 읽어보니 k로 하는 for문이 위에서 부터 내려오는 순서여서 두 줄 붙어있어도 체크할것같음.
		}
		if(numFullLines>0) {
			numLinesRemoved += numFullLines;
			
			statusbar.setText(String.valueOf(numLinesRemoved));
			isFallingFinished = true;
			curPiece.setShape(Tetrominoe.NoShape);//위에건 다 알겠는데 왜 노셰잎으로 만듦? 일단 노셰잎으로 초기화하는것같긴한데 왜 굳이? 그리고 그럼 어떤 식에서 다시 랜덤셰잎가져옴?
		}
	}
	private void drawSquare(Graphic g, int x, int y, Tetrominoe shape) {
		
		Color color[] = {new Color(0,0,0), new Color(204, 102, 102),
				new Color(102, 204, 102), new Color(102, 102, 204),
				new Color(204, 204, 102), new Color(204, 102, 204),
				new Color(102, 204, 204), new Color(218, 170, 0)
		};//8개 테트로미노 셰잎 들 색을 이걸로 할듯. 000은 노셰잎
		var color = colors[shape.ordinal()];
		
		g.setColor(color);
		g.fillRect(x+1, y+1, squareWidth()-2, squareHeight()-2);//-2는 2픽셀식 빼서 겉에 1픽셀짜리 테두리를 만드는건듯
		//근데 +1은 도저히 이해불가
		g.setColor(color.brighter());//밝음으로 하고 밑에 두줄 긋고 어두움으로 아래 두줄 긋는듯.
		g.drawLine(x, y +squareHeight()-1, x, y);
		g.drawLine(x,y,x+squareWidth()-1,y);//위랑 왼쪽 라인은 밝게,
		
		g.setColor(color.darker());//오른쪽, 아래 라인은 어둡게(그림자처럼)
		g.drawLine(x+1,y+squareHeight()-1,
				x+squareWidth()-1,y+squareHeight()-1);
		g.drawLine(x+squareWidth()-1, y+squareHeight()-1,
				x+squareWidth()-1, y+1);
	}//주석 추후작성
	
	private class GameCycle implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			doGameCycle();
		}
	}
	
	private void doGameCycle() {
		update();
		repaint();
	}
	private void update() {
		if(isPaused) {
			return;
		}//퍼즈 상태면 암것도 안함
		if(isFallingFinished) {
			//폴링이 끝난 상태면
			isFallingFinished = false;
			newPiece();//새 피스만들고 폴링 안끝남으로 되돌림
		} else {
			oneLineDown();//폴링중이면 한칸 내림. 
		}//이름 그대로 틱마다 업데이트 해주는 건듯.
	}
	
	class TAdapter extends KeyAdapter{
		
		@Override
		public void keyPressed(KeyEvent e) {
			if (curPiece.getShape() == Tetrominoe.NoShape) {
				return;
			}
			int keycode = e.getKeyCode();
			
			//Java12 switch expressions - 원문에 이렇게 적혀있는거임
			switch(keycode) {
			
			case KeyEvent.VK_P -> pause();
			case KeyEvent.VK_LEFT -> tryMove(curPiece, curX-1, curY);
			case KeyEvent.VK_RIGHT -> tryMove(curPiece, curX+1, curY);
			case KeyEvent.VK_DOWN -> tryMove(curPiece.rotateRight(), curX,curY);
			case KeyEvent.VK_UP -> tryMove(curPiece.rotateLeft(), curX,curY);
			//piece입력값에 커피스의 돌린 새로운 도형을 입력하는 방식.
			case KeyEvent.VK_SPACE -> dropDown();
			case KeyEvent.VK_D -> oneLineDown();//이건 그냥 d키 말하는거 맞나?
			}
		}
	}
}
