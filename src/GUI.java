import javax.imageio.ImageIO;
import javax.swing.*;
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

public class GUI {
    private List<File> droppedFiles;
    private BufferedImage image;
    private JTextField widthTextField;//创建输入目标宽度的窗口
    private JTextField heightTextField;//创建输入目标高度的窗口
    private static int targetWidth;//目标宽度
    private static int targetHeight;//目标高度
    private JFrame frame; // 主窗口
    private JPanel dropPanel; // 用于显示结果图像的面板



    public GUI() {
        droppedFiles = new ArrayList<>();
    
        // Create JFrame object and set title and size
        frame = new JFrame("Carver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
    
        // Create JPanel for the drop area
        dropPanel = new JPanel();
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drag and Drop Image Here"));
    
        // Create JLabel to display dropped image
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
    
        // 创建文本标签
        JLabel widthLabel = new JLabel("输入目标宽度：");
        JLabel heightLabel = new JLabel("输入目标高度：");
    
        // 创建文本框
        widthTextField = new JTextField(10);
        heightTextField = new JTextField(10);
    
        dropPanel.setLayout(new FlowLayout());
        dropPanel.add(widthLabel);
        dropPanel.add(widthTextField);
        dropPanel.add(heightLabel);
        dropPanel.add(heightTextField);
    
        // 创建文本标签用于显示图片大小
        JLabel sizeLabel = new JLabel("Image Size: - x -");
        dropPanel.add(sizeLabel);
    
        // Set up DropTarget for the drop area
        new DropTarget(dropPanel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                event.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = event.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    try {
                        List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!droppedFiles.isEmpty()) {
                            File file = droppedFiles.get(0); // Only process the first dropped file
                            imageLabel.setIcon(new ImageIcon(file.getAbsolutePath()));
                            GUI.this.droppedFiles.add(file); // Add the file to the list
    
                            // 显示图片大小
                            BufferedImage image = ImageIO.read(file);
                            int width = image.getWidth();
                            int height = image.getHeight();
                            sizeLabel.setText("Image Size: " + width + " x " + height);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    
        dropPanel.add(imageLabel, BorderLayout.CENTER);
    
        // Create JPanel for the buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton processButton = new JButton("Process");
        processButton.addActionListener(new ProcessButtonListener(droppedFiles));
        buttonPanel.add(processButton);
    
        JButton carveButton = new JButton("Carve");
        carveButton.addActionListener(new CarveButtonListener(droppedFiles, this));
        buttonPanel.add(carveButton);
    
        JButton expandButton = new JButton("Expand");
        expandButton.addActionListener(e -> {
            // Implement the expand functionality
            JOptionPane.showMessageDialog(frame, "Expand functionality not implemented yet.");
        });
        buttonPanel.add(expandButton);
    
        JButton protectButton = new JButton("Select To Protect");
        protectButton.addActionListener(e -> {
            // Implement the select to protect functionality
            JOptionPane.showMessageDialog(frame, "Select to protect functionality not implemented yet.");
        });
        buttonPanel.add(protectButton);
    
        JButton deleteButton = new JButton("Select To Delete");
        deleteButton.addActionListener(e -> {
            // Implement the select to delete functionality
            JOptionPane.showMessageDialog(frame, "Select to delete functionality not implemented yet.");
        });
        buttonPanel.add(deleteButton);


        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        image = ImageIO.read(selectedFile);
                        imageLabel.setIcon(new ImageIcon(image));
                        GUI.this.droppedFiles.add(selectedFile);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        buttonPanel.add(loadButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (image != null) {
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showSaveDialog(frame);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        try {
                            ImageIO.write(image, "PNG", selectedFile);
                            JOptionPane.showMessageDialog(frame, "图像保存成功！");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        buttonPanel.add(saveButton);

    
        // Add components to the frame
        frame.add(dropPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    
        // Make the frame visible
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }


    public static int getTargetHeight(){
        return targetHeight;
    }

    public static int getTargetWidth(){
        return targetWidth;
    }

    public void displayResultImage(BufferedImage resultImage) {
        dropPanel.removeAll(); // 清除面板上的现有组件

        JLabel resultImageLabel = new JLabel(new ImageIcon(resultImage));
        resultImageLabel.setHorizontalAlignment(JLabel.CENTER);
        dropPanel.add(resultImageLabel, BorderLayout.CENTER);

        dropPanel.revalidate(); // 重新布局面板
        dropPanel.repaint(); // 重绘面板
    }
}



class ProcessButtonListener implements ActionListener {
    private List<File> droppedFiles;

    public ProcessButtonListener(List<File> droppedFiles) {
        this.droppedFiles = droppedFiles;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!droppedFiles.isEmpty()) {
            File file = droppedFiles.get(0); // 只处理第一个文件
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    ImageProcessor.process(file.getAbsolutePath());
                    return null;
                }

                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(null, "图像处理成功！");
                }
            };
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(null, "没有拖放图像文件。");
        }
    }
}


class CarveButtonListener implements ActionListener {
    private final List<File> droppedFiles;
    private final GUI gui;

    public CarveButtonListener(List<File> droppedFiles, GUI gui) {
        this.droppedFiles = droppedFiles;
        this.gui = gui;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!droppedFiles.isEmpty()) {
            File file = droppedFiles.get(0); // Only process the first dropped file

            try {
                BufferedImage image = ImageIO.read(file);
                int targetWidth = GUI.getTargetWidth();
                int targetHeight = GUI.getTargetHeight();

                SeamCarver seamCarver = new SeamCarver();
                BufferedImage resultImage = seamCarver.shrinkImage(image, targetWidth, targetHeight);

                gui.displayResultImage(resultImage); // 在GUI中显示结果图像

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}