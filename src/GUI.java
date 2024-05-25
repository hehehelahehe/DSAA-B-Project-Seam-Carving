
import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter; // Import the MouseAdapter class

public class GUI {
    private File droppedFile;
    private BufferedImage image;
    private BufferedImage originalImage; // 添加一个成员变量来存储原始图像文件
    private BufferedImage carvedImage; // 添加一个成员变量来存储最后刻录的图像文件
    private BufferedImage expandedImage;//添加一个变量来存储最后放大的图像文件
    private JTextField widthTextField;//创建输入目标宽度的窗口
    private JTextField heightTextField;//创建输入目标高度的窗口
    private JLabel widthLabel = new JLabel("输入目标宽度：");
    private JLabel heightLabel = new JLabel("输入目标高度：");
    private JLabel sizeLabel = new JLabel("Image Size: - x -");
    private static int targetWidth;//目标宽度
    private static int targetHeight;//目标高度
    private JFrame frame; // 主窗口
    private JPanel dropPanel; // 用于显示结果图像的面板
    private JPanel buttonPanel; // 用于显示按钮的面板
    private JPanel infoPanel; // 用于显示信息的面板
    private JLabel imageLabel; // 用于显示图像的标签
    private JPanel hintPanel;//用于显示程序使用方法的面板
    private JLabel hintLabel;//用于显示程序使用方法的标签
    private SeamCarver seamCarver = new SeamCarver();
    private boolean SelectToProtect = false;
    private boolean SelectToDelete = false;
    private static Point startPoint;
    private static Point endPoint;
    private int selectedWidth;
    private int selectedHeight;
    private boolean rectangleDrawn = false;


    
    public GUI(){
        // Create JFrame object and set title and size
        this.frame = new JFrame("Carver");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(1200, 900);
        this.frame.setLayout(new BorderLayout());

        // Create JPanel for the drop area
        this.dropPanel = new JPanel();
        this.dropPanel.setBorder(BorderFactory.createTitledBorder("Drag and Drop Image Here"));

        this.imageLabel = new JLabel();
        this.imageLabel.setHorizontalAlignment(JLabel.CENTER);    
        this.dropPanel.add(imageLabel, BorderLayout.CENTER);

        //创建用于显示程序使用方法的窗口和标签
        this.hintPanel = new JPanel();
        this.hintLabel = new JLabel("<html><font size = 5>使用方法：<br>Process：处理图片<br>Carve：裁剪图片<br>Load：载入图片<br>Save：保存图片<br>Select To Protect：选择保护区域<br>Select To Delete：选择优先删除区域</html>");
        this.hintPanel.add(hintLabel);

        //创建用于输入目标长宽的窗口
        this.infoPanel = new JPanel();
        this.widthTextField = new JTextField(10);
        this.heightTextField = new JTextField(10);
        this.infoPanel.setLayout(new FlowLayout());
        this.infoPanel.add(widthLabel);
        this.infoPanel.add(widthTextField);
        this.infoPanel.add(heightLabel);
        this.infoPanel.add(heightTextField);

        //创建文本标签显示图片大小
        infoPanel.add(sizeLabel);

         // 将dropPanel设置为可接受拖拽操作
         dropPanel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0); // 只处理拖入的第一个文件
                        processDroppedFile(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Create JPanel for the buttons
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton processButton = new JButton("Process");
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    
                    performImageProcessing();
                } catch (NumberFormatException ex) { // Rename the parameter to 'ex'
                    JOptionPane.showMessageDialog(null, "请输入有效的整数作为目标宽度和目标高度！", "错误", JOptionPane.ERROR_MESSAGE);
                }
                
            }
        });
        buttonPanel.add(processButton);

        JButton carveButton = new JButton("Carve");
        carveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                 
                    performImageCarving();
                } catch (NumberFormatException ex) { // Rename the parameter to 'ex'
                    JOptionPane.showMessageDialog(null, "请输入有效的整数作为目标宽度和目标高度！", "错误", JOptionPane.ERROR_MESSAGE);
                }
                
            }
        });
        buttonPanel.add(carveButton);

        JButton expandButton = new JButton("Expand");
        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    
                    performImageExpanding();
                } catch (NumberFormatException ex) { // Rename the parameter to 'ex'
                    JOptionPane.showMessageDialog(null, "请输入有效的整数作为目标宽度和目标高度！", "错误", JOptionPane.ERROR_MESSAGE);
                }
                
            }
        
        });
        buttonPanel.add(expandButton);

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(dropPanel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    loadImage(selectedFile);
                }
            }
        });
        buttonPanel.add(loadButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    saveImage();               
            }
        });
        buttonPanel.add(saveButton);

        JButton protectButton = new JButton("Select To Protect");
        protectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SelectToProtect == false) {
                    SelectToProtect = true;
                    SelectToDelete = false;
                    if (rectangleDrawn) {
                        image = originalImage;
                        imageLabel.setIcon(new ImageIcon(image));
                        sizeLabel.setText("Image Size: " + image.getWidth() + " x " + image.getHeight());
                        dropPanel.repaint();
                        rectangleDrawn = false;
                    }
                }
                else{
                    SelectToProtect = false;                    
                }
                switchOfProtectMode();
            }
        });
        buttonPanel.add(protectButton);

        JButton deleteButton = new JButton("Select To Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SelectToDelete == false) {
                    SelectToDelete = true;
                    SelectToProtect = false;
                    if (rectangleDrawn) {
                        image = originalImage;
                        imageLabel.setIcon(new ImageIcon(image));
                        sizeLabel.setText("Image Size: " + image.getWidth() + " x " + image.getHeight());
                        dropPanel.repaint();
                        rectangleDrawn = false;
                    }
                }
                else{
                    SelectToDelete = false;                    
                }
                switchOfDeleteMode();
            }
        });
        buttonPanel.add(deleteButton);            
        frame.add(dropPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(infoPanel, BorderLayout.NORTH);
        frame.add(hintPanel,BorderLayout.WEST);
        frame.setVisible(true);
        registerDragAndDrop();
    }

    private void processDroppedFile(File file) {
        try {
            this.droppedFile = file;

            // 读取图像文件并显示在imageLabel中
            this.image = ImageIO.read(file);
            this.imageLabel.setIcon(new ImageIcon(this.image));

            // 获取图像的宽度和高度并更新标签
            int width = this.image.getWidth();
            int height = this.image.getHeight();
            sizeLabel.setText("Image Size: " + width + " x " + height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performImageProcessing(){
        if (droppedFile != null) {
           
                int targetWidth = Integer.parseInt(widthTextField.getText());
                int targetHeight = Integer.parseInt(heightTextField.getText());
                
                this.carvedImage = seamCarver.processImage(image, targetWidth, targetHeight);
    
                    // 更新图像和标签显示
                this.imageLabel.setIcon(new ImageIcon(carvedImage));
                this.sizeLabel.setText("Image Size: " + carvedImage.getWidth() + " x " + carvedImage.getHeight());
                this.frame.revalidate();
                this.frame.repaint();
                this.image = this.carvedImage;
        
                JOptionPane.showMessageDialog(null, "图像处理成功！");
                
   
        }

    }

    private void performImageCarving() {
        if (droppedFile != null) {
            
                int targetWidth = Integer.parseInt(widthTextField.getText());
                int targetHeight = Integer.parseInt(heightTextField.getText());
                if (targetWidth > image.getWidth() || targetHeight > image.getHeight()){
                    JOptionPane.showMessageDialog(null, "目标宽度或目标高度大于原图像的宽度或高度，请重新输入！", "错误", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    if (SelectToDelete) {
                        if (rectangleDrawn == false) {
                            JOptionPane.showMessageDialog(null, "请先选取要删除的区域！或先退出优先删除模式再进行裁剪", "错误", JOptionPane.ERROR_MESSAGE);
                        }
                        this.carvedImage = seamCarver.shrinkImage(originalImage, targetWidth, targetHeight,startPoint,endPoint,false);
                        this.originalImage = this.carvedImage;

                        // 更新图像和标签显示
                        this.imageLabel.setIcon(new ImageIcon(carvedImage));
                        this.sizeLabel.setText("Image Size: " + carvedImage.getWidth() + " x " + carvedImage.getHeight());
                        this.frame.revalidate();
                        this.frame.repaint();
                        this.image = this.carvedImage;
                        this.rectangleDrawn = false;
                    
                    }else if (SelectToProtect){
                        if (rectangleDrawn == false) {
                            JOptionPane.showMessageDialog(null, "请先选取要保护的区域！或先退出保护模式再进行裁剪", "错误", JOptionPane.ERROR_MESSAGE);
                            
                        }
                        this.carvedImage = seamCarver.shrinkImage(originalImage, targetWidth, targetHeight,startPoint,endPoint,true);
                        this.originalImage = this.carvedImage;

                        // 更新图像和标签显示
                        this.imageLabel.setIcon(new ImageIcon(carvedImage));
                        this.sizeLabel.setText("Image Size: " + carvedImage.getWidth() + " x " + carvedImage.getHeight());
                        this.frame.revalidate();
                        this.frame.repaint();
                        this.image = this.carvedImage;
                        this.rectangleDrawn = false;
                    }else{
                        this.carvedImage = seamCarver.shrinkImage(image, targetWidth, targetHeight);
                        //一次删除多条最小能量路径
                        //this.carvedImage = seamCarver.shrinkImageMultiple(image, targetWidth, targetHeight);
    
                        // 更新图像和标签显示
                        this.imageLabel.setIcon(new ImageIcon(carvedImage));
                        this.sizeLabel.setText("Image Size: " + carvedImage.getWidth() + " x " + carvedImage.getHeight());
                        this.frame.revalidate();
                        this.frame.repaint();
                        this.image = this.carvedImage;

                    }
                            
                    JOptionPane.showMessageDialog(null, "图像处理成功！");
                }
        }
    }

    private void performImageExpanding(){
        if (droppedFile != null) {
                targetWidth = Integer.parseInt(widthTextField.getText());
                targetHeight = Integer.parseInt(heightTextField.getText());
                if (targetWidth < image.getWidth() || targetHeight < image.getHeight()){
                    JOptionPane.showMessageDialog(null, "目标宽度或目标高度小于原图像的宽度或高度，请重新输入！", "错误", JOptionPane.ERROR_MESSAGE);
                }
                else{this.expandedImage = seamCarver.expandImage(this.image, targetWidth, targetHeight);
    
                    // 更新图像和标签显示
                    this.imageLabel.setIcon(new ImageIcon(this.expandedImage));
                    this.sizeLabel.setText("Image Size: " + expandedImage.getWidth() + " x " + expandedImage.getHeight());
                    this.frame.revalidate();
                    this.frame.repaint();
                    this.image = this.expandedImage;
        
                    JOptionPane.showMessageDialog(null, "图像处理成功！");
                }
        }
    }

    public File getDroppedFile() {
        return droppedFile;
    }

    public void setDroppedFile(File file) {
        droppedFile = file;
        // 更新图像和标签显示
        try {
            this.image = ImageIO.read(droppedFile);
            this.imageLabel.setIcon(new ImageIcon(image));
            this.sizeLabel.setText("Image Size: " + image.getWidth() + " x " + image.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerDragAndDrop() {
        DropTarget dropTarget = new DropTarget(dropPanel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0); // 只处理第一个文件
                        setDroppedFile(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dropPanel.setDropTarget(dropTarget);
    }

    private void loadImage(File file) {
        try {
            this.image = ImageIO.read(file);
            this.imageLabel.setIcon(new ImageIcon(this.image));
            this.imageLabel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            this.sizeLabel.setText("Image Size: " + image.getWidth() + " x " + image.getHeight());
            this.droppedFile = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImage() {
        if (droppedFile != null && carvedImage != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Image");
    
            String fileName = droppedFile.getName();
            String extension = ".png";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                extension = fileName.substring(dotIndex + 1);
            }
    
            fileChooser.setSelectedFile(new File(fileName));
    
            int userChoice = fileChooser.showSaveDialog(frame);
    
            if (userChoice == JFileChooser.APPROVE_OPTION) {
                File saveFile = fileChooser.getSelectedFile();
    
                if (saveFile.exists()) {
                    JOptionPane.showMessageDialog(frame, "文件已存在，请选择其他文件名！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                String filePath = saveFile.getAbsolutePath();
    
                try {
                    // 将成员变量carvedImage保存为文件
                    ImageIO.write(carvedImage, extension, new File(filePath));
                    JOptionPane.showMessageDialog(frame, "图像保存成功！");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void switchOfProtectMode() {        
        MouseListener myMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!rectangleDrawn) {
                    startPoint = e.getPoint();
                    
                }
                
            }
        
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!rectangleDrawn) {
                    endPoint = e.getPoint();
                    
                    calculateDimensions();
                    drawRectangle();
                }
              
            }
        };
        if (SelectToProtect == true) {
            
            this.imageLabel.addMouseListener(myMouseListener);
            JOptionPane.showMessageDialog(frame, "已进入选取模式！您所选的图片区域将在Carve操作中被保留。再次点击按钮可取消选取或退出选取模式");
            
        }
        else{
            this.imageLabel.removeMouseListener(myMouseListener);
            JOptionPane.showMessageDialog(frame,"已退出选取模式。");
            if (rectangleDrawn) {
                this.imageLabel.setIcon(new ImageIcon(originalImage));               
                this.dropPanel.repaint();
                this.rectangleDrawn = false; // 更新标志变量为矩形未绘
            }
        }
    }

    private void switchOfDeleteMode() {        
        MouseListener myMouseListener = new MouseAdapter() {
            
            @Override
            public void mousePressed(MouseEvent e) {                
                if (!rectangleDrawn) {
                    startPoint = e.getPoint();
                    
                }              
            }
        
            @Override
            public void mouseReleased(MouseEvent e) {        
                if (!rectangleDrawn) {
                    endPoint = e.getPoint();
                    
                    calculateDimensions();
                    drawRectangle();
                }              
            }
        };
        if (SelectToDelete == true) {
            this.imageLabel.addMouseListener(myMouseListener);
            JOptionPane.showMessageDialog(frame, "已进入选取模式！您所选的图片区域将在Carve操作中被优先删去。再次点击按钮可取消选取或退出选取模式");
        }
        else{
            this.imageLabel.removeMouseListener(myMouseListener);
            JOptionPane.showMessageDialog(frame,"已退出选取模式。");
            if (this.rectangleDrawn) {
                this.imageLabel.setIcon(new ImageIcon(originalImage));               
                this.dropPanel.repaint();
                this.rectangleDrawn = false; // 更新标志变量为矩形未绘
            }
        }
    }

    private void calculateDimensions() {
        this.selectedWidth = Math.abs(startPoint.x - endPoint.x);
        this.selectedHeight = Math.abs(startPoint.y - endPoint.y);
    }

    private void drawRectangle() {
        if (imageLabel.getIcon() != null) {
            ImageIcon imageIcon = (ImageIcon) imageLabel.getIcon();
            Image image = imageIcon.getImage();
            this.originalImage = this.image;
    
            BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
    

    
            if (SelectToDelete) {
                g2d.setColor(Color.RED);
            } else if (SelectToProtect){
                g2d.setColor(Color.GREEN);
            }
            
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            g2d.drawRect(x, y, selectedWidth,selectedHeight);
            g2d.dispose();
    
            imageLabel.setIcon(new ImageIcon(bufferedImage));
            dropPanel.repaint();
    
            this.rectangleDrawn = true; // 更新标志变量为矩形已绘制
        }
    }
    
    public static Point getStartPoint(){
        return startPoint;
    }

    public static Point getEndPoint(){
        return endPoint;
    }

    
}



class ProcessButtonListener implements ActionListener {
    private GUI gui;

    public ProcessButtonListener(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File droppedFile = gui.getDroppedFile(); // 获取GUI实例中的droppedFile
        if (droppedFile != null) {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    ImageProcessor.process(droppedFile.getAbsolutePath());
                    return null;
                }

                @Override
                protected void done() {
                    gui.setDroppedFile(droppedFile);
                    JOptionPane.showMessageDialog(null, "图像处理成功！");
                    
                }
            };
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(null, "没有拖放图像文件。");
        }
    }

    

    
}
