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

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);    
        dropPanel.add(imageLabel, BorderLayout.CENTER);

        //创建用于输入目标长宽的窗口
        infoPanel = new JPanel();
        widthTextField = new JTextField(10);
        heightTextField = new JTextField(10);
        infoPanel.setLayout(new FlowLayout());
        infoPanel.add(widthLabel);
        infoPanel.add(widthTextField);
        infoPanel.add(heightLabel);
        infoPanel.add(heightTextField);

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
        processButton.addActionListener(new ProcessButtonListener(this));
        buttonPanel.add(processButton);

        JButton carveButton = new JButton("Carve");
        carveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int targetWidth = Integer.parseInt(widthTextField.getText());
                    int targetHeight = Integer.parseInt(heightTextField.getText());
                    performImageCarving();
                } catch (NumberFormatException ex) { // Rename the parameter to 'ex'
                    JOptionPane.showMessageDialog(null, "请输入有效的整数作为目标宽度和目标高度！", "错误", JOptionPane.ERROR_MESSAGE);
                }
                
            }
        });
        buttonPanel.add(carveButton);

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

    private void performImageCarving() {
        if (droppedFile != null) {
            try {
                this.image = ImageIO.read(droppedFile);
                int targetWidth = Integer.parseInt(widthTextField.getText());
                int targetHeight = Integer.parseInt(heightTextField.getText());
    
                this.carvedImage = seamCarver.shrinkImage(image, targetWidth, targetHeight);
    
                // 更新图像和标签显示
                this.imageLabel.setIcon(new ImageIcon(carvedImage));
                this.sizeLabel.setText("Image Size: " + carvedImage.getWidth() + " x " + carvedImage.getHeight());
                this.frame.revalidate();
                this.frame.repaint();
                this.image = this.carvedImage;
    
                JOptionPane.showMessageDialog(null, "图像处理成功！");
            } catch (IOException e) {
                e.printStackTrace();
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
                startPoint = e.getPoint();
                System.out.println("startPoint: " + startPoint.getX() + ", " + startPoint.getY());
            }
        
            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                System.out.println("endPoint: " + endPoint.getX() + ", " + endPoint.getY());
                calculateDimensions();
                drawRectangle();
            }
        };
        if (SelectToProtect == true) {
            this.imageLabel.addMouseListener(myMouseListener);
            
        }
        else{
            this.imageLabel.removeMouseListener(myMouseListener);
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
                startPoint = e.getPoint();
                System.out.println("startPoint: " + startPoint.getX() + ", " + startPoint.getY());
            }
        
            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                System.out.println("endPoint: " + endPoint.getX() + ", " + endPoint.getY());
                calculateDimensions();
                drawRectangle();
            }
        };
        if (SelectToDelete == true) {
            this.imageLabel.addMouseListener(myMouseListener);
            
        }
        else{
            this.imageLabel.removeMouseListener(myMouseListener);
            if (rectangleDrawn) {
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
    
            rectangleDrawn = true; // 更新标志变量为矩形已绘制
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

//     JButton expandButton = new JButton("Expand");
    //     expandButton.addActionListener(e -> {
    //         // Implement the expand functionality
    //         JOptionPane.showMessageDialog(frame, "Expand functionality not implemented yet.");
    //     });
    //     buttonPanel.add(expandButton);
    
    //     JButton protectButton = new JButton("Select To Protect");
    //     protectButton.addActionListener(e -> {
    //         // Implement the select to protect functionality
    //         JOptionPane.showMessageDialog(frame, "Select to protect functionality not implemented yet.");
    //     });
    //     buttonPanel.add(protectButton);
    
    //     JButton deleteButton = new JButton("Select To Delete");
    //     deleteButton.addActionListener(e -> {
    //         // Implement the select to delete functionality
    //         JOptionPane.showMessageDialog(frame, "Select to delete functionality not implemented yet.");
    //     });
    //     buttonPanel.add(deleteButton);


    //     JButton loadButton = new JButton("Load");
    //     loadButton.addActionListener(new ActionListener() {
    //         @Override
    //         public void actionPerformed(ActionEvent e) {
    //             JFileChooser fileChooser = new JFileChooser();
    //             int result = fileChooser.showOpenDialog(frame);
    //             if (result == JFileChooser.APPROVE_OPTION) {
    //                 File selectedFile = fileChooser.getSelectedFile();
    //                 try {
    //                     image = ImageIO.read(selectedFile);
    //                     imageLabel.setIcon(new ImageIcon(image));
    //                     GUI.this.droppedFiles.add(selectedFile);
    //                 } catch (IOException ex) {
    //                     ex.printStackTrace();
    //                 }
    //             }
    //         }
    //     });
    //     buttonPanel.add(loadButton);

    //     JButton saveButton = new JButton("Save");
    //     saveButton.addActionListener(new ActionListener() {
    //         @Override
    //         public void actionPerformed(ActionEvent e) {
    //             if (image != null) {
    //                 JFileChooser fileChooser = new JFileChooser();
    //                 int result = fileChooser.showSaveDialog(frame);
    //                 if (result == JFileChooser.APPROVE_OPTION) {
    //                     File selectedFile = fileChooser.getSelectedFile();
    //                     try {
    //                         ImageIO.write(image, "PNG", selectedFile);
    //                         JOptionPane.showMessageDialog(frame, "图像保存成功！");
    //                     } catch (IOException ex) {
    //                         ex.printStackTrace();
    //                     }
    //                 }
    //             }
    //         }
    //     });
    //     buttonPanel.add(saveButton);

    
    //     // Add components to the frame
    //     frame.add(dropPanel, BorderLayout.CENTER);
    //     frame.add(buttonPanel, BorderLayout.SOUTH);
    
    //     // Make the frame visible
    //     frame.setVisible(true);

    // }

    // public static void main(String[] args) {
    //     SwingUtilities.invokeLater(GUI::new);
    // }


    // public static int getTargetHeight(){
    //     return targetHeight;
    // }

    // public static int getTargetWidth(){
    //     return targetWidth;
    // }

    // public void displayResultImage(BufferedImage resultImage) {
    //     dropPanel.removeAll(); // 清除面板上的现有组件

    //     JLabel resultImageLabel = new JLabel(new ImageIcon(resultImage));
    //     resultImageLabel.setHorizontalAlignment(JLabel.CENTER);
    //     dropPanel.add(resultImageLabel, BorderLayout.CENTER);

    //     dropPanel.revalidate(); // 重新布局面板
    //     dropPanel.repaint(); // 重绘面板
    // }
// class CarveButtonListener implements ActionListener {
//     private final List<File> droppedFiles;
//     private final GUI gui;

//     public CarveButtonListener(List<File> droppedFiles, GUI gui) {
//         this.droppedFiles = droppedFiles;
//         this.gui = gui;
//     }

//     @Override
//     public void actionPerformed(ActionEvent e) {
//         if (!droppedFiles.isEmpty()) {
//             File file = droppedFiles.get(0); // Only process the first dropped file

//             try {
//                 BufferedImage image = ImageIO.read(file);
//                 int targetWidth = GUI.getTargetWidth();
//                 int targetHeight = GUI.getTargetHeight();

//                 SeamCarver seamCarver = new SeamCarver();
//                 BufferedImage resultImage = seamCarver.shrinkImage(image, targetWidth, targetHeight);

//                 gui.displayResultImage(resultImage); // 在GUI中显示结果图像

//             } catch (IOException ex) {
//                 ex.printStackTrace();
//             }
//         }
//     }
// }