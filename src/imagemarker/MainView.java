package imagemarker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import imagemarker.SelectiveSearchSegmentation.Region;

/**
 * 利用loadImage来更新显示的图片
 * 一个Frame用于展示结果
 * 
 */ 
public class MainView extends JFrame{

	protected static ExecutorService exec = Executors.newCachedThreadPool();
	/**
	 * 画板
	 */
	private GraphBasedImageSegmentation gbiSeg;
	protected SelectiveSearchSegmentation selSeg;
	/**
	 * 显示图像
	 */
	protected PicView jp;
	protected Image img = null;
	private static final long serialVersionUID = 1L;
	protected int k = 1000;
	protected int min = 30;
	/**
	 * 存放其他组件
	 */
	private JPanel jpWin;
	protected OptionView jpOp;
	/**
	 * 存放当前正处理的图像文件
	 */
	public Vector<String> fileNameList = new Vector<String>();

	MainView(){
		//设置
		gbiSeg = new GraphBasedImageSegmentation();
		selSeg = new SelectiveSearchSegmentation();
		//设置标题
		this.setTitle("Image");
		//设置组件的位置和大小(缩放一倍)
		this.setBounds(200, 200, 1100, 700);
		//设置关闭窗口
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//绝对布局
		this.setLayout(null);//设置布局NULL  
		/*
		 * JPanel实现初始时绘制图片
		 * 和paint(x,y)方法绘制方框
		 */
		this.jp = new PicView();
		jp.setSize(900, 600);
		/*
		 * 设置
		 */
		this.add(jp);
		jp.setVisible(true);
		/*
		 * jpWin及其附件
		 */
		JTextField x = new JTextField(10);
		JTextField y = new JTextField(10);
		JLabel jlX = new JLabel("x:");
		JLabel jlY = new JLabel("y:");
		x.setText("0.0");
		y.setText("0.0");
		jpWin = new JPanel(new FlowLayout(2,10,10));
		jpWin.setBounds(0, 600, 900, 100);
		jpWin.setVisible(true);
		JButton startFirst = new JButton("startBase");
		JButton startSecond = new JButton("start");
		JButton clear = new JButton("clear");
		jpWin.add(jlX);
		jpWin.add(x);
		jpWin.add(jlY);
		jpWin.add(y);
		jpWin.add(startFirst);
		jpWin.add(startSecond);
		jpWin.add(clear);
		jlX.setVisible(true);
		jlY.setVisible(true);
		x.setVisible(true);
		y.setVisible(true);
		startFirst.setVisible(true);
		startSecond.setVisible(true);
		clear.setVisible(true);
		startFirst.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(img == null)return;
				/*
				 * 开始进行演示
				 */
				exec.execute(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						System.out.println("startBasic");
						Vector<Vector<Integer>> mask = gbiSeg.runBasic(img,k,min);
						if(mask == null || mask.isEmpty())return;
						Image img = createImage(mask.size(),mask.get(0).size());
						Graphics g = img.getGraphics();
						for(int x = 0;x < mask.size();x++) {
							for(int y = 0;y < mask.lastElement().size();y++) {
								//模拟画点
								g.setColor(new Color((int)(((mask.get(x).get(y) * 17) ^ 2) % 255),(int)((mask.get(x).get(y) ^ 2) % 255),(int)(mask.get(x).get(y) % 255)));
								g.drawLine(x, y, x, y + 1);
							}
						}	
						jp.loadImage(img);
						System.out.println("overBasic");
					}
				});
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}

			@Override
			public void mouseExited(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent arg0) {}

			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		startSecond.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(img == null)return;
				/*
				 * 开始进行演示
				 */
				exec.execute(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						System.out.println("start");
						Vector<Region> regions = selSeg.runBasic(img,k,min);
						Image image = createImage(img.getWidth(null),img.getHeight(null));
						if(regions == null || regions.isEmpty())return;
						Graphics g = image.getGraphics();
						((Graphics2D) g).setStroke(new BasicStroke(Math.max(img.getWidth(null)/900, img.getHeight(null)/600)+1));
						g.drawImage(img, 0, 0, img.getWidth(null),img.getHeight(null), null);
						for(Region region : regions) {
							if(region.size < SegmentationOptionsSetter.MINPOINT)continue;
							if((region.maxX - region.minX)/(region.maxY - region.minY) > SegmentationOptionsSetter.MAXPORTION || (region.maxY - region.minY)/(region.maxX - region.minX) > SegmentationOptionsSetter.MAXPORTION)continue;
							g.setColor(new Color(region.minX%255,region.minY%255,region.maxX%255));
							g.drawRect(region.minX, region.minY, region.maxX - region.minX,region.maxY - region.minY);
						}	
						jp.loadImage(image);
						System.out.println("over");
					}
				});
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}
			
		});
		clear.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				if(img == null)return;
				/*
				 * 重绘
				 */
				clear();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}

			@Override
			public void mouseExited(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent arg0) {}

			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		MouseAdapter mouseEve = new MouseAdapter() {
			private int xLast;
			private int yLast;
			@Override
			public void mouseClicked(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				if(img == null) {
					x.setText("0");
					y.setText("0");
				}
				else {
					x.setText(String.valueOf((int) (e.getX() * img.getWidth(null) / 900)));
					y.setText(String.valueOf((int) (e.getY() * img.getHeight(null) / 600)));
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				x.setText("0");
				y.setText("0");
			}

			@Override
			public void mousePressed(MouseEvent e) {	
				xLast = e.getX();
				yLast = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(img == null)return;
				OneSthMarker one = new OneSthMarker();
				one.x1 = Math.min(xLast, e.getX()) * img.getWidth(null) / 900;
				one.y1 = Math.min(yLast, e.getY()) * img.getHeight(null) / 600;
				one.x2 = Math.max(xLast, e.getX()) * img.getWidth(null) / 900;
				one.y2 = Math.max(yLast, e.getY()) * img.getHeight(null) / 600;
				paintRect(one.x1,one.y1,one.x2-one.x1,one.y2-one.y1);
				jpOp.reflashPosition(one);
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {	
				if(img == null)return;
				paintRect(Math.min(xLast, e.getX()) * img.getWidth(null) / 900,Math.min(yLast, e.getY()) * img.getHeight(null) / 600,Math.abs(e.getX()-xLast) * img.getWidth(null) / 900,Math.abs(e.getY()-yLast) * img.getHeight(null) / 600);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				if(img == null) {
					x.setText("0");
					y.setText("0");
				}
				else {
					x.setText(String.valueOf((int) (e.getX()  * img.getWidth(null)) / 900));
					y.setText(String.valueOf((int) (e.getY()  * img.getHeight(null) / 600)));
				}
			}
		};
		jp.addMouseMotionListener(mouseEve);
		jp.addMouseListener(mouseEve);
		this.add(jpWin);
		/*
		 * 右边设置面板
		 */
		this.jpOp = new OptionView(this);
		this.jpOp.setLayout(null);
		this.add(this.jpOp);
		this.jpOp.setBounds(900, 0, 200, 700);
		this.jpOp.setVisible(true);
	}
	
	/**
	 * 在当前图像上绘制矩形框
	 * 且不改变原图像
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void paintRect(int x,int y,int width,int height) {
		Image img = createImage(this.img.getWidth(null),this.img.getHeight(null));
		Graphics g = img.getGraphics();
		g.drawImage(this.img, 0, 0, this.img.getWidth(null),this.img.getHeight(null), null);
		g.setColor(Color.RED);
		((Graphics2D) g).setStroke(new BasicStroke(Math.max(img.getWidth(null)/900, img.getHeight(null)/600)+1));
		g.drawRect(x, y, width, height);
		this.jp.loadImage(img);
	}
	
	public void loadImage(Image img) {
		this.jp.loadImage(img);
		this.img = img;
	}
	
	public void paintOneSth(OneSthMarker one) {
		Graphics2D g = (Graphics2D) this.img.getGraphics();
		g.drawImage(this.img, 0, 0, this.img.getWidth(null),this.img.getHeight(null), null);
		g.setColor(Color.BLUE);
		Font font=new Font("宋体",Font.PLAIN,20);  
        g.setFont(font); 
        g.drawString(one.label,one.x2,one.y2);
        g.setStroke(new BasicStroke(Math.max(img.getWidth(null)/900, img.getHeight(null)/600)+1));
        g.setColor(Color.RED);
        g.drawRect(one.x1, one.y1, one.x2-one.x1, one.y2-one.y1);
        this.loadImage(this.img);
	}
	
	public void clear() {
		this.jp.loadImage(img);
	}
	
	/**
	 * 读取文件
	 * @param index
	 */
	public void readImage(int index) {
		if(this.fileNameList.size() <= index || index < 0)return;
		try {
			BufferedImage img = ImageIO.read(new File(this.fileNameList.get(index)));
			this.loadImage(img);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MainView view = new MainView();
		view.setVisible(true);
		//view.loadImage(anotherImg);
	}
}

