import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {
    private List<File> droppedFiles;

    public GUI() {
        droppedFiles = new ArrayList<>();
        createAndShow();
    }

    private void createAndShow() {
        // Create JFrame object and set title and size
        JFrame frame = new JFrame("Image Processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
    
        // Create JPanel for the drop area
        JPanel dropPanel = new JPanel(new BorderLayout());
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drop Area"));
    
        // Create JLabel to display dropped image
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
    
        // Set up DropTarget for the drop area
        DropTarget dropTarget = new DropTarget(dropPanel, new DropTargetAdapter() {
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
        carveButton.addActionListener(e -> {
            // Implement the carve functionality
            JOptionPane.showMessageDialog(frame, "Carving functionality not implemented yet.");
        });
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
    
        // Add components to the frame
        frame.add(dropPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    
        // Make the frame visible
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
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