import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessor {
    static double[][] sobelX = {{1,0,-1},{2,0,-2},{1,0,-1}};
    static double[][] sobelY = {{1,2,1},{0,0,0},{-1,-2,-1}};
    static int r = 0;
    static int g = 1;
    static int b = 2;
    public static void process(String imageFile){
        BufferedImage bf = readImage(imageFile);
        int[][][][] imageArray = convert2DArrayTO4DArray(bf);

        //pdf上给出的算法的改进版，通过利用sobel算子分别计算RGB三色的“能量”，求和后开平方得到图片的能量
        //int[][] gradient = edgeDetect(imageArray);

        //OpenCV的sobel算子边缘检测算法，通过先将图片由RGB转化为灰度图，再用灰度图求能量
        int[][] gradient = edgeDetectGray(imageArray);

        BufferedImage gradientImage = createGrayScaleImage(gradient);
        displayImage(gradientImage);
        try {
            writeImageFile(gradientImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static BufferedImage readImage(String imageFile){
        File file = new File(imageFile);
        BufferedImage bf = null;
        try {
            bf = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bf;
    }

    public static int[][][][] convert2DArrayTO4DArray(BufferedImage bf) {
        // 获取图片宽度和高度
        int width = bf.getWidth();   // 图片宽度
        int height = bf.getHeight();  //图片高度
        int channel = 3; // 3个通道

        int[] data = new int[width*height];
        bf.getRGB(0, 0, width, height, data, 0, width);
        // 将二维数组转换为三维数组
        int[][][][] rgb4DArray = new int[1][width][height][channel]; // 图像数量*宽度*高度*通道数


        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                rgb4DArray[0][j][i][r] = (data[i*width + j] & 0xff0000) >> 16;
                rgb4DArray[0][j][i][g] = (data[i*width + j] & 0xff00) >> 8;
                rgb4DArray[0][j][i][b] = (data[i*width + j] & 0xff);
            }
        }
        return rgb4DArray;
    }

    public static int[][] edgeDetect(int[][][][] img){
        int width = img[0].length;
        int height = img[0][0].length;
        int size = 3;

        int[][] redX = new int[width][height];
        int[][] redY = new int[width][height];
        int[][] greenX = new int[width][height];
        int[][] greenY = new int[width][height];
        int[][] blueX = new int[width][height];
        int[][] blueY = new int[width][height];

        for(int x = 0;x < width-size+1;x++){
            for(int y = 0;y < height-size+1;y++){
                int tempX = 0;
                int tempY = 0;
                for(int i = 0;i < size;i++){
                    for(int j = 0;j < size;j++){
                        tempX += img[0][x+i][y+j][r]*sobelX[i][j];
                        tempY += img[0][x+i][y+j][r]*sobelY[i][j];
                    }
                }

                redX[x + 1][y + 1] = tempX;
                redY[x + 1][y + 1] = tempY;
            }
        }
        for(int x = 0;x < width-size+1;x++){
            for(int y = 0;y < height-size+1;y++){
                int tempX = 0;
                int tempY = 0;
                for(int i = 0;i < size;i++){
                    for(int j = 0;j < size;j++){
                        tempX += img[0][x+i][y+j][g]*sobelX[i][j];
                        tempY += img[0][x+i][y+j][g]*sobelY[i][j];
                    }
                }

                greenX[x + 1][y + 1] = tempX;
                greenY[x + 1][y + 1] = tempY;
            }
        }
        for(int x = 0;x < width-size+1;x++){
            for(int y = 0;y < height-size+1;y++){
                int tempX = 0;
                int tempY = 0;
                for(int i = 0;i < size;i++){
                    for(int j = 0;j < size;j++){
                        tempX += img[0][x+i][y+j][b]*sobelX[i][j];
                        tempY += img[0][x+i][y+j][b]*sobelY[i][j];
                    }
                }

                blueX[x + 1][y + 1] = tempX;
                blueY[x + 1][y + 1] = tempY;
            }
        }

        int[][] gradient = new int[width][height];

        //设置阈值RMax，在边缘检测时可以由阈值判断边缘
        //int RMax = 200;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int tempX = redX[i][j] * redX[i][j] + greenX[i][j] * greenX[i][j] + blueX[i][j] * blueX[i][j];
                int tempY = redY[i][j] * redY[i][j] + greenY[i][j] * greenY[i][j] + blueY[i][j] * blueY[i][j];
                int temp = (int) Math.sqrt(tempX + tempY);
                //if(temp >= RMax) gradient[i][j] = 255;
                //else gradient[i][j] = 0;
                gradient[i][j] = temp;
            }
        }

        //边缘像素赋值为255
        for (int i = 0; i < width; i++) {
            gradient[i][0] = gradient[i][height-1] = 255;
        }
        for (int i = 0; i < height; i++) {
            gradient[0][i] = gradient[width-1][i] = 255;
        }
        return gradient;
    }
    public static int[][] edgeDetectGray(int[][][][] img){
        int width = img[0].length;
        int height = img[0][0].length;
        int size = 3;

        int[][] gray = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int red = img[0][i][j][r];
                int green = img[0][i][j][g];
                int blue = img[0][i][j][b];
                gray[i][j] = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
            }
        }

        int[][] gradientX = new int[width][height];
        int[][] gradientY = new int[width][height];

        for(int x = 0;x < width-size+1;x++){
            for(int y = 0;y < height-size+1;y++){
                int tempX = 0;
                int tempY = 0;
                for(int i = 0;i < size;i++){
                    for(int j = 0;j < size;j++){
                        tempX += gray[x+i][y+j] * sobelX[i][j];
                        tempY += gray[x+i][y+j] * sobelY[i][j];
                    }
                }

                gradientX[x + 1][y + 1] = tempX;
                gradientY[x + 1][y + 1] = tempY;
            }
        }

        int[][] power = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //简化运算次数的版本，效果近似
                //int tempX = Math.abs(gradientX[i][j]);
                //int tempY = Math.abs(gradientY[i][j]);
                //power[i][j] = (tempX + tempY);

                int tempX = gradientX[i][j] * gradientX[i][j];
                int tempY = gradientY[i][j] * gradientY[i][j];
                power[i][j] = (int) Math.sqrt(tempX + tempY);
            }
        }

        return power;
    }

    public static BufferedImage createGrayScaleImage(int[][] grayScaleArray) {
        int width = grayScaleArray.length;
        int height = grayScaleArray[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int gray = grayScaleArray[x][y];
                gray = Math.min(255,gray);
                int color = (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, color);
            }
        }

        return image;
    }
    public static void displayImage(BufferedImage image) {
        // 获取屏幕分辨率
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double screenHeight = screenSize.getHeight();

        // 创建JFrame来作为主窗口
        JFrame frame = new JFrame("梯度图");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建一个JPanel，你将在这个JPanel上绘制图像
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 在JPanel上绘制图像
                if (image != null) {
                    double imageWidth = image.getWidth();
                    double imageHeight = image.getHeight();

                    double scale = 1.0;
                    if (imageWidth > screenWidth || imageHeight > screenHeight) {
                        scale = Math.min(screenWidth / imageWidth, screenHeight / imageHeight);
                    }

                    int newWidth = (int)(imageWidth * scale);
                    int newHeight = (int)(imageHeight * scale);

                    // 使用Graphics2D来绘制缩放的图像
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                if (image != null) {
                    double imageWidth = image.getWidth();
                    double imageHeight = image.getHeight();

                    double scale = 1.0;
                    if (imageWidth > screenWidth || imageHeight > screenHeight) {
                        scale = Math.min(screenWidth / imageWidth, screenHeight / imageHeight);
                    }

                    int newWidth = (int)(imageWidth * scale);
                    int newHeight = (int)(imageHeight * scale);

                    return new Dimension(newWidth, newHeight);
                } else {
                    return super.getPreferredSize(); // 默认尺寸
                }
            }
        };

        frame.add(panel); // 将面板添加到窗口

        // 设置窗口的其他属性
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }

    public static void writeImageFile(BufferedImage bi) throws IOException {
        File outputfile = new File("saved.png");
        ImageIO.write(bi, "png", outputfile);
    }
}
