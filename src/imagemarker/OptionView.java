package imagemarker;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import imagemarker.SelectiveSearchSegmentation.Region;

/**
 * 设置栏
 * @author ene
 *
 */
public class OptionView extends JPanel{
	/**
	 * 上层控件，用来更新画布
	 */
	private static final long serialVersionUID = 1L;
	private MainView parent;
	/*
	 * 需要实时更新的组件
	 */
	/**
	 * 检测文件的data是否已经创建
	 */
	public JLabel dataAlive;
	/**
	 * 是否跳过未创建data的图片
	 */
	public JCheckBox checkSkip;
	/**
	 * 检查是否使用SelectiveSearch
	 */
	public JCheckBox checkSearch;
	/**
	 * 检查是否添加图像数据
	 */
	public JCheckBox checkAddData;
	/**
	 * 计数当前读取的图片个数
	 */
	private JLabel picNumCounter;
	public int picNumAll = 0;
	public int picNumNow = 0;
	/*
	 * 框选的位置信息
	 */
	private InputTextField x1;
	private InputTextField y1;
	private InputTextField x2;
	private InputTextField y2;
	private InputTextField inputLabel;
	private InputTextField inputIndex;
	/**
	 * 记录者
	 */
	private ImageMarker marker = new ImageMarker();
	/**
	 * 当前记录的mark
	 */
	private OneSthMarker one;
	/**
	 * 输入文件路径
	 */
	private Set<String> inputPath = new HashSet<String>();
	
	/*
	 * 小于picNumNow已处理完毕
	 */
	private boolean left = false;
	/*
	 * 大于等于picNumNow已处理完毕
	 */
	private boolean right = false;
	
	OptionView(MainView parent){
		this.parent = parent;
		this.dataAlive = new JLabel("null");
		this.dataAlive.setBackground(Color.RED);
		this.dataAlive.setBounds(0, 0, 180, 35);
		this.dataAlive.setVisible(true);
		this.add(this.dataAlive);
		this.picNumCounter = new JLabel("当前处理进度: " + String.valueOf(this.picNumNow) + "/" + String.valueOf(this.picNumAll));
		this.picNumCounter.setVisible(true);
		this.picNumCounter.setBounds(0, 35, 180, 35);
		this.add(this.picNumCounter);
		JButton inputPathButton = new JButton("添加输入文件路径");
		this.add(inputPathButton);
		inputPathButton.setVisible(true);
		inputPathButton.setBounds(0, 70, 180, 35);
		inputPathButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {}

			@Override
			public void mouseEntered(MouseEvent arg0) {}

			@Override
			public void mouseExited(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent arg0) {
				JFileChooser fc=new JFileChooser();
				fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择目录
				String path=null;
				int flag = 0;
				try{     
				            flag=fc.showOpenDialog(null);     
				        }    
				        catch(HeadlessException head){     
				             System.out.println("Open File Dialog ERROR!");    
				        }        
				        if(flag == JFileChooser.APPROVE_OPTION){
				             //获得该文件     
				            path=fc.getSelectedFile().getPath();
				         }   
				if(path == null||inputPath.contains(path))return;
				left=false;
				inputPath.add(path);
				for(String fileName : (new File(path)).list()) {
					String back = fileName.substring(fileName.length()-3).toLowerCase();
					if(back.equals("jpg") || back.equals("png"))
						parent.fileNameList.addElement(path +"\\" +  fileName);
				}
				if(picNumAll == 0) {
					picNumAll = parent.fileNameList.size();
					openNextImage();
				}
				else {
					picNumAll = parent.fileNameList.size();
					picNumCounter.setText("当前处理进度: " + String.valueOf(picNumNow) + "/" + String.valueOf(picNumAll));
				}
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		JButton outputPathButton = new JButton("输出文件路径");
		this.add(outputPathButton);
		outputPathButton.setVisible(true);
		outputPathButton.setBounds(0, 105, 180, 35);
		outputPathButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {}

			@Override
			public void mouseEntered(MouseEvent arg0) {}

			@Override
			public void mouseExited(MouseEvent arg0) {}

			@Override
			public void mousePressed(MouseEvent arg0) {
				JFileChooser fc=new JFileChooser();
				fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择目录
				String path=null;
				int flag = 0;
				try{     
				            flag=fc.showOpenDialog(null);     
				    }    
				    catch(HeadlessException head){     
				         System.out.println("Open File Dialog ERROR!");    
				    }        
				    if(flag == JFileChooser.APPROVE_OPTION){
				         //获得该文件    
				        path=fc.getSelectedFile().getPath();
				        if(path == null)return;
				        marker.setPath(path);
				     }
				 if(inputPath.isEmpty())return;
				 picNumNow--;
				 openNextImage();
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		this.checkSearch = new JCheckBox("使用SelectiveSearch",false);
		this.add(this.checkSearch);
		this.checkSearch.setVisible(true);
		this.checkSearch.setBounds(0, 140, 180, 35);
		this.checkSkip = new JCheckBox("跳过已创建data的图片",true);
		this.add(this.checkSkip);
		this.checkSkip.setVisible(true);
		this.checkSkip.setBounds(0, 175, 180, 35);
		this.checkAddData = new JCheckBox("记录图像数据",false);
		this.add(this.checkAddData);
		this.checkAddData.setVisible(true);
		this.checkAddData.setBounds(0, 210, 180, 35);
		/*
		 * 包含框选的位置信息的JPanel
		 */
		JPanel positionData = new JPanel();
		positionData.setLayout(new GridLayout(2,2));
		positionData.setVisible(true);
		positionData.setBounds(0, 245, 180, 70);
		this.add(positionData);
		x1 = new InputTextField("x1:","");
		y1 = new InputTextField("y1:","");
		x2 = new InputTextField("x2:","");
		y2 = new InputTextField("y2:","");
		positionData.add(this.x1);
		positionData.add(this.y1);
		positionData.add(this.x2);
		positionData.add(this.y2);
		x1.setVisible(true);
		y1.setVisible(true);
		x2.setVisible(true);
		y2.setVisible(true);
		this.inputLabel = new InputTextField("label:","");
		this.add(this.inputLabel);
		this.inputLabel.setVisible(true);
		this.inputLabel.setBounds(0, 315, 180, 35);
		this.inputLabel.input.addInputMethodListener(new InputMethodListener() {

			@Override
			public void caretPositionChanged(InputMethodEvent event) {}

			@Override
			public void inputMethodTextChanged(InputMethodEvent event) {
				// TODO Auto-generated method stub
				int index = marker.readBook(inputLabel.getText());
				if(index != Integer.MIN_VALUE)
					inputIndex.setText(String.valueOf(index));
			}
			
		});
		this.inputIndex = new InputTextField("index:","");
		this.add(this.inputIndex);
		this.inputIndex.setVisible(true);
		this.inputIndex.setBounds(0, 350, 180, 35);
		JButton addButton = new JButton("add");
		this.add(addButton);
		addButton.setVisible(true);
		addButton.setBounds(0, 385, 180, 35);
		/*
		 * add加入数据成功后把位置信息清空
		 */
		addButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if(one != null) {
					one.label = inputLabel.getText();
					try {
						one.index = new Integer(inputIndex.getText()).intValue();
					}
					catch(NumberFormatException ex) {
						return;
					}
				}
				else 
					return;
				if(one.checkFilled()) {
					marker.add(one);
					parent.clear();
					one = null;
					x1.setText("");
					y1.setText("");
					x2.setText("");
					y2.setText("");
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {}
			
		});
		JButton finishButton = new JButton("finish");
		this.add(finishButton);
		finishButton.setVisible(true);
		finishButton.setBounds(0, 420, 180, 105);
		finishButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if(marker.isEmpty())return;
				if(checkSearch.isSelected())
					getSelectiveSearchResult(marker);
				else 
					marker.mark(parent.img,parent.fileNameList.get(picNumNow-1),checkAddData.isSelected());
				parent.clear();
				clear();
			}

			@Override
			public void mouseReleased(MouseEvent e) {}
			
		});
		JPanel jpDirection = new JPanel();
		this.add(jpDirection);
		jpDirection.setLayout(new GridLayout(1,2));
		jpDirection.setVisible(true);
		JButton left = new JButton("last");
		JButton right = new JButton("next");
		jpDirection.add(left);
		left.setVisible(true);
		left.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if(picNumNow >= 2 && picNumAll > 1)
					openLastImage();
			}

			@Override
			public void mouseReleased(MouseEvent e) {}
			
		});
		jpDirection.add(right);
		right.setVisible(true);
		right.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				if(picNumNow < picNumAll)openNextImage();
			}
			
		});
		jpDirection.setBounds(0, 525, 180, 75);
	}
	
	/**
	 * 更新坐标
	 * @param marker
	 */
	public void reflashPosition(OneSthMarker marker) {
		this.x1.setText(String.valueOf(marker.x1));
		this.y1.setText(String.valueOf(marker.y1));
		this.x2.setText(String.valueOf(marker.x2));
		this.y2.setText(String.valueOf(marker.y2));
		this.one = marker;
	}
	
	/**
	 * 打开下一张图片
	 */
	private void openNextImage() {
		if(left&&right&&checkSkip.isSelected())return;
		if(picNumNow > picNumAll-1) {
			right = true;
			openLastImage();
			return;
		}
		if(this.inputPath.isEmpty())return;
		this.clear();
		picNumNow++;
		if(this.marker.checkFileExist(this.parent.fileNameList.get(picNumNow-1).substring(this.parent.fileNameList.get(picNumNow-1).lastIndexOf("\\")+1)))
			this.dataAlive.setText("exist");
		else
			this.dataAlive.setText("non-exist");
		if(this.checkSkip.isSelected() && this.dataAlive.getText().equals("exist"))
			openNextImage();
		else {
			picNumCounter.setText("当前处理进度: " + String.valueOf(picNumNow) + "/" + String.valueOf(picNumAll));
			this.marker.clear();
			this.parent.readImage(picNumNow-1);
			left = false;
			right = false;
		}
	}
	
	/**
	 * 打开上一张图片
	 */
	private void openLastImage() {
		//System.out.println(String.valueOf(picNumNow));
		if(left&&right&&checkSkip.isSelected())return;
		if(picNumNow < 2) {
			left = true;
			openNextImage();
			return;
		}
		if(this.inputPath.isEmpty())return;
		this.clear();
		picNumNow--;
		if(this.marker.checkFileExist(this.parent.fileNameList.get(picNumNow-1).substring(this.parent.fileNameList.get(picNumNow-1).lastIndexOf("\\")+1)))
			this.dataAlive.setText("exist");
		else
			this.dataAlive.setText("non-exist");
		if(this.checkSkip.isSelected() && this.dataAlive.getText().equals("exist"))
			openLastImage();
		else {
			picNumCounter.setText("当前处理进度: " + String.valueOf(picNumNow) + "/" + String.valueOf(picNumAll));
			this.marker.clear();
			this.parent.readImage(picNumNow-1);
			left = false;
			right = false;
		}
	}
	
	/**
	 * 清空当前信息
	 */
	private void clear() {
		this.one = null;
		this.x1.setText("");
		this.y1.setText("");
		this.x2.setText("");
		this.y2.setText("");
		this.inputLabel.setText("");
		this.inputIndex.setText("");
	}
	
	/**
	 * 文本+输入框
	 * @author ene
	 *
	 */
	protected class InputTextField extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public JLabel label;
		public JTextField input;
		InputTextField(){
			this.setLayout(new GridLayout(1,4));
			this.label = new JLabel();
			this.input = new JTextField();
			this.label.setVisible(true);
			this.input.setVisible(true);
			this.add(this.label);
			this.add(this.input);
		}
		InputTextField(String strLabel,String strInput){
			this.setLayout(new GridLayout(1,2));
			this.label = new JLabel(strLabel);
			this.input = new JTextField(strInput);
			this.label.setVisible(true);
			this.input.setVisible(true);
			this.add(this.label);
			this.add(this.input);
		}
		public void setText(String text) {
			input.setText(text);
		}
		public String getText() {
			return input.getText();
		}
	}
	
	/**
	 * 把SelectiveSearch的结果放在marker
	 * 另开一个线程，防止阻塞
	 * 
	 * 注意:
	 * IOU计算，交集/两者中最小值
	 * 0.1 < IOU < 0.3作背景
	 * @param marker
	 */
	public void getSelectiveSearchResult(ImageMarker marker) {
		// TODO Auto-generated method stub
		MainView.exec.execute(new Runnable() {
			@Override
			public void run() {
				ImageMarker m = marker.presentOwnership();
				int index = m.readBook("背景");
				if(index == Integer.MIN_VALUE)
					index = -1;
				Vector<Region> regions = parent.selSeg.runBasic(parent.img,parent.k,parent.min);
				Vector<Integer> sizes = new Vector<Integer>();
				for(OneSthMarker one:m.markers)
					sizes.addElement(new Integer((one.x2-one.x1)*(one.y2 - one.y1)));
				for(Region region : regions) {
					if(region.size < SegmentationOptionsSetter.MINPOINT)continue;
					if((region.maxX - region.minX)/(region.maxY - region.minY) > SegmentationOptionsSetter.MAXPORTION || (region.maxY - region.minY)/(region.maxX - region.minX) > SegmentationOptionsSetter.MAXPORTION)continue;
					//IOU < 0.3作为背景
					boolean contin = true;
					for(int indexSecond = 0;indexSecond < sizes.size();indexSecond++) {
						int sizeBoth = (Math.min(region.maxX, m.markers.get(indexSecond).x2) - Math.max(region.minX, m.markers.get(indexSecond).x1));
						if(sizeBoth < 0)
							continue;
						sizeBoth *= (Math.min(region.maxY, m.markers.get(indexSecond).y2) - Math.max(region.minY, m.markers.get(indexSecond).y1));
						//System.out.println((double)sizeBoth / (region.size + sizes.get(indexSecond) - sizeBoth));
						if((double)sizeBoth / Math.min(region.size,sizes.get(indexSecond)) < 0.3 && (double)sizeBoth / Math.min(region.size,sizes.get(indexSecond)) > 0.1) {
							contin = false;
							break;
						}
					}
					if(contin)continue;
					OneSthMarker one = new OneSthMarker();
					one.x1 = region.minX;
					one.y1 = region.minY;
					one.x2 = region.maxX;
					one.y2 = region.maxY;
					one.label = "背景";
					one.index = index;
					m.add(one);
					//System.out.println("$$");
					//System.out.println(String.valueOf(one.x1));
					//System.out.println(String.valueOf(one.y1));
					//System.out.println(String.valueOf(one.x2));
					//System.out.println(String.valueOf(one.y2));
				}	
				//System.out.println("over");
				m.mark(parent.img,parent.fileNameList.get(picNumNow-1),checkAddData.isSelected());
				m.clear();
			}
		});
	}
}
