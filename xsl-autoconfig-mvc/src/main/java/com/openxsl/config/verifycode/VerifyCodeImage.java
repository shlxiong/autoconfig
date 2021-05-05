package com.openxsl.config.verifycode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class VerifyCodeImage {
	public static final String ATTR_CODE = "__VERIFY_CODE__";
	public static final String ATTR_GENER_TIME = "__GENER_TIMESTAMP__";
	public static final String ATTR_VERIFY_TIME = "__GENER_TIMESTAMP__";
	
	private final Random rand = new Random();
    private char[] aa={'零','壹','贰','叁','肆','伍','陆','柒','捌','玖'};
    private char[] action= {'加','减'};
    private int result = 0;
    private boolean reset = false;
    int width = 200;
    int height = 100;
    private boolean simple;
    
    public VerifyCodeImage() {}
    public VerifyCodeImage(int width, int height, boolean simple){
    	this.width = width;
    	this.height = height;
    	this.simple = simple;
    }
    
    public static void main(String[] args) {
    	VerifyCodeImage code = new VerifyCodeImage(150, 50, true);
		JFrame jFrame = new JFrame();
        jFrame.setBounds(400, 400, 250, 250);
          
        ImageIcon img = new ImageIcon(code.getVerificationCode()); 
        JLabel background = new JLabel(img);
          
        jFrame.add(background);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        System.out.println(code.getResult());
    }
    
    /** 
     * 第二种验证码的计算方式,两位数的加减法 
     * @return 一个新的验证码图片 
     */  
    public BufferedImage getVerificationCode() {  
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics(); 
          
        //画干扰点  
        int alpha = 255;
        this.createRandomPoint(width, height, 50, alpha, g);
        this.createRandomLine(width, height, 4, alpha, g);
        // 设定背景色   
        Color background = this.getColor(180);  //透明度
        g.setColor(background);  
        g.fillRect(0, 0, width, height);
        //画边框   
        g.setColor(background);  
        g.drawRect(0, 0, width-1, height-1);
        //将认证码显示到图像中,如果要生成更多位的认证码  
        String content = this.getContent();
        int len = content.length();
        int[] xs = this.getRadomWidths(width, len);  
        int[] ys = this.getRadomHeights(height, len);  
        for (int i=0; i<len; i++) {
        	g.setColor(this.getColor(background, 230));
            if (i == 2) {//运算符号显示大一些  
                g.setFont(new Font("宋体", Font.BOLD, getIntRandom(23,25)));
            } else {
            	if (simple) {
            		g.setFont(new Font("宋体", Font.BOLD, getIntRandom(20,23)));
            	} else {
            		g.setFont(new Font("宋体", Font.PLAIN, getIntRandom(20,23)));
            	}
            }
            String s = String.valueOf(content.charAt(i));
            int degree = 0;
            if (i != 2){
            	degree = this.getIntRandom(10, 50);
            	if (rand.nextInt(2) != 0) {
            		degree += 300;
            	}
            }
            rotateString(s, xs[i], ys[i], degree, g);  
        }
        //画干扰点 
        alpha = 100;
        this.createRandomPoint(width, height,100, alpha, g);         
        this.createRandomLine(width, height, 8, alpha, g);
        // 释放图形上下文  
        g.dispose();
        return image;
    }  
    
    /** 
     * 两位数加减法字符数组 
     */  
    private String getContent() {
    	StringBuilder buffer = new StringBuilder();
    	int num1 = 0, num2 = 0;
    	while (num1 < 10) {
    		num1 = rand.nextInt(100);
    	}
    	while (num2 < 10) {
    		num2 = rand.nextInt(100);
    	}
    	
    	final int operator = rand.nextInt(2);
    	if (operator != 0) {  //减法
    		int max = Math.max(num1, num2);
    		int min = Math.min(num1, num2);
    		this.result = max - min;
    		num1 = max;
    		num2 = min;
    	} else {  //加法
    		result = num1 + num2;
    	}
    	
    	if (simple) {
    		buffer.append(num1);
    		buffer.append(action[operator]);
    		buffer.append(num2);
    	} else {
    		buffer.append(aa[num1/10]).append(aa[num1%10]);
    		buffer.append(action[operator]);
	    	buffer.append(aa[num2/10]).append(aa[num2%10]);
    	}
    	
    	reset = true;
    	return buffer.toString();
    }
    
    public int getResult() {
    	if (reset) {
    		reset = false;
    		return result;
    	} else {
    		throw new IllegalStateException("还没生成计算公式");
    	}
    }
    
    /** 
     * 旋转并且画出指定字符串 
     * @param s 需要旋转的字符串 
     * @param x 字符串的x坐标 
     * @param y 字符串的Y坐标 
     * @param g 画笔g 
     * @param degree 旋转的角度 
     */  
    private void rotateString(String s, int x, int y, int degree, Graphics g) {  
        Graphics2D g2d = (Graphics2D) g.create();                                    
        //平移原点到图形环境的中心  ,这个方法的作用实际上就是将字符串移动到某一个位置  
        g2d.translate(getIntRandom(x, x+2), getIntRandom(y, y+2));
        g2d.rotate(degree* Math.PI / 180);
        g2d.drawString(s, 0 , 0);  
    }  
      
    /** 
     * 随机产生干扰点 
     * @param width 
     * @param height 
     * @param many 
     * @param g 
     * @param alpha 透明度0~255 0表示全透 
     */  
    private void createRandomPoint(int width, int height, int many, int alpha, Graphics g) {  // 随机产生干扰点  
        for (int i=0; i<many; i++) {  
            int x = rand.nextInt(width);   
            int y = rand.nextInt(height);   
            g.setColor(getColor(alpha));  
            g.drawOval(x,y,rand.nextInt(3),rand.nextInt(3));   
        }   
    }  
	/** 
	 * 随机产生干扰线条 
	 * @param width 
	 * @param height 
	 * @param minMany 最少产生的数量 
	 * @param g 
	 * @param alpha 透明度0~255 0表示全透 
	 */  
    private void createRandomLine(int width, int height, int minMany, int alpha, Graphics g) {
        for (int i=0;i<getIntRandom(minMany, minMany+6);i++) {
            int x1 =getIntRandom(0, (int)(width*0.6));
            int y1 =getIntRandom(0, (int)(height*0.6));
            int x2 =getIntRandom((int)(width*0.4), width);
            int y2 =getIntRandom((int)(height*0.2), height);
            g.setColor(getColor(alpha));  
            g.drawLine(x1, y1, x2, y2);  
        }   
    }  
      
    /**
     * @return 随机返一个指定区间的数字 
     */  
    private int getIntRandom(int start, int end) {
        if (end<start) {  
           int t=end;  
           end=start;  
           start=t;  
        }  
        int i = start + (int)(Math.random()*(end-start));  
        return i;  
    }  
      
    /**
     * 随机返回一种颜色,透明度0~255 0表示全透 
     * @param alpha 透明度0~255 0表示全透 
     */  
    private Color getColor(int alpha) {  
        int R = (int) (Math.random() * 255);  
        int G = (int) (Math.random() * 255);  
        int B = (int) (Math.random() * 255);  
        return new Color(R,G,B, alpha);  
    }  
      
    /**
     * 返回一种与给定颜色相类似的颜色
     * 
     * @param alpha 透明度0~255 0表示全透 
     */  
    private Color getColor(Color c, int alpha) {  
        int R = getIntRandom(-140,140);
        int G = getIntRandom(-140,140);
        int B = getIntRandom(-140,140);
        R = getCloserRandom(c.getRed(), R);  
        B = getCloserRandom(c.getBlue(), B);  
        G = getCloserRandom(c.getGreen(), G);  
        return new Color(R,G,B, alpha);  
    }
    
    /** 
     * 在颜色值和给定的随机数之间返回一个随机颜色值0~255 
     * @param colorValue 
     * @param randomValue 
     * @param deep,默认为0 
     * @return 
     */  
    private int getCloserRandom(int colorValue,int randomValue){          
        if (colorValue+randomValue>255) {   
            return getCloserRandom(colorValue, randomValue-getIntRandom(1, randomValue+20));
        } else if(colorValue+randomValue<0) {  
            return getCloserRandom(colorValue, randomValue+getIntRandom(1, randomValue+20));
        } else if(Math.abs(randomValue)<60) {  
            return getCloserRandom(colorValue, getIntRandom(-255,255));  
        } else {
            return colorValue+randomValue;  
        }  
    }
      
//    /** 
//     * 对图片选择,这里保留以方便以后使用 
//     * @param bufferedimage 
//     * @param degree 
//     * @return 一张旋转后的图片 
//     */  
//    public BufferedImage rolateImage(BufferedImage bufferedimage, int degree) {                  
//        BufferedImage img;  
//        int w = bufferedimage.getWidth();  
//        int h = bufferedimage.getHeight();  
//        int type = BufferedImage.TYPE_INT_RGB;  
//        Graphics2D graphics2d;  
//        graphics2d = (img = new BufferedImage(w, h, type)).createGraphics();  
//        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  
//                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);  
//        graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);  
//        graphics2d.drawImage(bufferedimage,null, null);   
//        return img;  
//    }
    
    /** 
     *  
     * @param many 
     * @return 画图的时候随机的高度的数组 
     */  
    private int[] getRadomHeights(int height,int many){  
        int[] temp=new int[many];   
        for(int i=0;i<many;i++){  
            temp[i] = this.getIntRandom((int)(height*0.4), (int)(height*0.8));
        }  
        return temp;  
    } 
    
    /** 
     * @param many 
     * @return 画图的时候起始x坐标的数组 
     */  
    private int[] getRadomWidths(int width,int many) {
   	 	int[] temp = new int[many];
   	 	temp[0] = getRadonWidth(many, 0, width);
   	 	for (int i=1; i<many; i++){  
   	 		temp[i] = getRadonWidth(many, temp[i-1], width);  
   	 	}  
   	 	return temp;        
    }  
      
    private int getRadonWidth(int many,int minWidth, int maxWidth) {  
      int minJianju = maxWidth/(many+2);  
      int maxJianju = maxWidth/(many);  
      int temp = maxJianju - minJianju;  
      return (int)(Math.random()*temp) + minWidth + minJianju;        
    }  
  
  
}
