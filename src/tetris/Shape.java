package tetris;

import java.util.Random;//snake에서도 썼던 랜덤

public class Shape {

	protected enum Tetrominoe{
		//각각의 테트로미노들 이름을 선언하는듯.enum은 열거형.https://limkydev.tistory.com/50참조
		//enum으로 이렇게 가능한 선택지를 열거해놓으면 테트로미노는 String형 전체가 아닌 여기서만 고를 수 있음.
		NoShape, SShape, ZShape, LineShape,//원래 거에 s랑 z 쉐잎이 반대로 설정된것같아서 바꿈. 어차피 실행과정에선 노상관일듯.
		TShape, SquareShape, LShape, MirroredShape
	}
	private Tetrominoe pieceShape;
	private int[][] coords;//coord=coordination 조직.
	//2차원 인트 배열. 테트로미노 만들때 쓸 것 같은데 왜boolean이 아닌 int일까?
	public Shape() {
		coords = new int[4][2];//테트로미노들은 돌리면 다4*2로 표현가능.
		setShape(Tetrominoe.NoShape);//일단은 다 노셰잎이 되는듯
	}
	void setShape(Tetrominoe shape) {
		int[][][] coordsTable = new int[][][] {//왜 삼차원?
			{{0,0},{0,0},{0,0},{0,0}},
			{{0,-1},{0,0},{-1,0},{-1,1}},//-1을 써서 int인건 알겠는데 -1은 뭘까
			{{0, -1}, {0, 0}, {1, 0}, {1, 1}},
            {{0, -1}, {0, 0}, {0, 1}, {0, 2}},
            {{-1, 0}, {0, 0}, {1, 0}, {0, 1}},
            {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
            {{-1, -1}, {0, -1}, {0, 0}, {0, 1}},
            {{1, -1}, {0, -1}, {0, 0}, {0, 1}}
		};
		
		for(int i=0; i<4;i++) {
			System.arraycopy(coordsTable[shape.ordinal()], 0, coords, 0, 4);
		}//테이블에서 coords로 4개를 복사하는 것. ordinal은 해당값이 enum에 정의된 순서.
		//coordTable[]이기에 3차원 배열 중 한번 들어가서 2차원배열, 즉 {n,n}의 4쌍을 복사
		//즉 테이블의 첫줄부터 NoShape,다음은 Zshape 이런식. 근데 그렇게 그려봐도 이상함
		pieceShape = shape;//pieceShape을 방금 만든 shape로 만듬
	}
	private void setX(int index, int x) {
		coords[index][0] = x;//coords에서 index순서놈의 첫 숫자를 x로 변경
	}//아래거랑 합쳐보면 coords[index][0]=coords[index][0] 이해불가.
	//@@어차피 변수 x를 넣는데 무슨의미가 있나 했는데, 실제 쓸때는 x자리에 전혀 다른걸 넣어서 변경이 되는 거임.즉 원래 자리의 x를 새로 변수로 넣은 x로 변경
	private void setY(int index, int y) {
		coords[index][1] = y;//coords에서 index순서놈의 두번째 숫자
	}
	int x(int index) {
		return coords[index][0];//coords에서 인덱스순서의 첫 숫자.
	}
	int y(int index) {
		return coords[index][1];
	}//아직은 도저히 이해가 안된다.
	Tetrominoe getShape() {
		return pieceShape;//아까 만든 피스셰잎을 테트로미노에게 반환
	}
	void setRandomShape() {
		var r = new Random();//var은 자료형을 알아서 유추하게 하는 자료형.
		//random()이 double형으로 만든다는데 왜 double이 아닌 var를 썼을까?double을 하면 왜 에러가 날까
		int x = Math.abs(r.nextInt()) % 7 + 1;//int로 쓰려고 var했나?
		//abs는 절댓값.r.nextint하면 2^32승까지의 수중에 하나가 나온다고함. 7로 나눈 나머지는 랜덤으로 7개를 만들기 위한거임.
		//8개인 테트로미노중에 하나를 빼는건데. +1을 통해 첫 순서인 NoShape를 빼는것을 유추가능 
		Tetrominoe[] values = Tetrominoe.values();//enum.values는 순서 반환인듯
		setShape(values[x]);//즉 이렇게 만든 1~7까지의 밸류를 가진 숫자들로 setShape.
	}
	public int minX() {//최솟값 가져오는듯
		int m = coords[0][0];//4,2의 coords에서 걍 초기화.
		for(int i=0; i<4; i++) {//4번 실행
			m = Math.min(m, coords[i][0]);
		}//각 쌍의 첫 숫자와 비교해 더 작은 숫자 선택
		return m;
	}//젤 작은것 리턴
	int minY() {
		int m = coords[0][1];
		for(int i=0;i<4;i++) {
			m = Math.min(m, coords[i][1]);
		}//각 쌍의 뒷 숫자중에 최솟값 선택, 반환
		return m;
	}
	Shape rotateLeft() {
		if(pieceShape == Tetrominoe.SquareShape) {
			return this;//네모면 걍 그대로. pieceshape이 현재 나온 테트로미노인듯
		}
		var result = new Shape();
		result.pieceShape = pieceShape;//결과물 만들기 앞서 현재 상황으로 초기화인듯.
		
		for(int i=0; i<4; i++) {
			
			result.setX(i, y(i));//i의 x를 i의 y로 변경
			result.setY(i, -x(i));//-는 아직 이해불가.
		}//@@@젯코드 찾아보니 각 배열의 숫자는 있음, 없음이 아니라 좌표를 의미하고. 배열이 4개인 이유는 4개의 조각의 좌표
		return result;
	}
	Shape rotateRight() {
		if(pieceShape == Tetrominoe.SquareShape) {
			return this;
		}
		var result = new Shape();//실험결과 var을 Shape로 바꿔도 이상x
		result.pieceShape = pieceShape;
		
		for(int i=0; i<4; i++) {
			result.setX(i, -y(i));
			result.setY(i, x(i));
		}
		return result;
	}
}
