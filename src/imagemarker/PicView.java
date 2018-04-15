package imagemarker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * 显示图片
 * loadImage更新图片
 * @author ene
 *
 */
public class PicView extends JPanel{
	/**
	 * 显示图片
	 */
	private static final long serialVersionUID = 1L;
	private Image img;
	PicView(Image img){
		this.img = img;
	}
	PicView(){
		this.img = null;
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(this.img == null)return;
        g.drawImage(this.img, 0, 0, getWidth(), getHeight(), null);
	}
	public void paintRect(int x,int y,int width,int height) {
		Graphics g = this.getGraphics();
		if (g == null) {
			return;
		}
		g.setColor(Color.RED);
		g.drawRect(x, y, width, height);
		super.update(g);
	}
	//实现图片的更新
	public void loadImage(Image img)
	{
		this.img=img;
		this.setSize(900, 600);
		this.repaint();
	}
}
