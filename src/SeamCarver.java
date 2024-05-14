import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class SeamCarver {
    public BufferedImage shrinkImage(BufferedImage image, int targetWidth, int targetHeight) {
        int currentWidth = image.getWidth();
        int currentHeight = image.getHeight();

        while ( currentHeight > targetHeight) {
            int[][] energyMap = computeEnergyMap(image);
            int[][] cumulativeEnergyMap = computeHorizontalCumulativeEnergyMap(energyMap);
            int[] seam = findHorizontalSeam(cumulativeEnergyMap);

            image = removeHorizontalSeam(image, seam);
            currentHeight--;
        }
        while ( currentWidth > targetWidth) {
            int[][] energyMap = computeEnergyMap(image);
            int[][] cumulativeEnergyMap = computeCumulativeVerticalEnergyMap(energyMap);
            int[] seam = findVerticalEnergySeam(cumulativeEnergyMap);

            image = removeVerticalSeam(image, seam);
            currentWidth--;
        }

        return image;
    }

    public BufferedImage expandImage(BufferedImage image, int targetWidth,int targetHeight){
        int Width = image.getWidth();
        int Height = image.getHeight();

        int numSeamsY = targetHeight - Height;//计算纵向扩展需要的seam数量
        int numSeamsX = targetWidth - Width;//计算横向扩展需要的seam数量
        int[][] energyMap = computeEnergyMap(image);

        int[][] cumulativeEnergyMapX = computeHorizontalCumulativeEnergyMap(energyMap);//计算纵向扩展所需的能量累积图
        List<int[]> seamsX = findHorizontalSeams(cumulativeEnergyMapX, numSeamsY);
        //displayPaths(seamsY,image);

        for (int i = 0; i < seamsX.size(); i++) {
            int[] seam = seamsX.get(i);
            BufferedImage temp = new BufferedImage(Width, Height + 1, BufferedImage.TYPE_INT_RGB);
            for (int j = 0; j < Width; j++) {
                int a = seam[j];
                for (int k = 0; k < Height + 1; k++) {
                    if (k < a) {
                        temp.setRGB(j, k, image.getRGB(j, k));
                    } else if (k == a) {
                        temp.setRGB(j, k, image.getRGB(j, k));
                    } else {
                        temp.setRGB(j, k, image.getRGB(j, k - 1));
                    }
                }
            }
            image = temp;
            Height++;
        }

        energyMap = computeEnergyMap(image);//重新计算energyMap
        int[][] cumulativeEnergyMapY = computeCumulativeVerticalEnergyMap(energyMap);//计算横向扩展所需的能量累积图
        List<int[]> seamsY = findVerticalSeams(cumulativeEnergyMapY, numSeamsX);

        for (int i = 0; i < seamsY.size(); i++) {
            int[] seam = seamsY.get(i);
            BufferedImage temp = new BufferedImage(Width + 1, Height, BufferedImage.TYPE_INT_RGB);
            for (int j = 0; j < Height; j++) {
                int a = seam[j];
                for (int k = 0; k < Width + 1; k++) {
                    if (k < a) {
                        temp.setRGB(k, j, image.getRGB(k, j));
                    } else if (k == a) {
                        temp.setRGB(k, j, image.getRGB(k, j));
                    } else {
                        temp.setRGB(k, j, image.getRGB(k - 1, j));
                    }
                }
            }
            image = temp;
            Width++;
        }

        return image;
    }
/*
    public static int[][] rotateArray90Degrees(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        int[][] rotatedMatrix = new int[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                rotatedMatrix[col][rows - row - 1] = matrix[row][col];
            }
        }

        return rotatedMatrix;
    }
*/
    private static int[][] computeEnergyMap(BufferedImage image) {

        int [][]test;
        System.out.println(image.getWidth() + " " + image.getHeight());
        test = ImageProcessor.edgeDetect(ImageProcessor.convert2DArrayTO4DArray(image));
        //test = ImageProcessor.edgeDetectGray(ImageProcessor.convert2DArrayTO4DArray(image));
        System.out.println(test.length);
        System.out.println(test[0].length);
        return test;
    }

    private static int[][] computeHorizontalCumulativeEnergyMap(int[][] energyMap) {
        int rows = energyMap.length;
        int cols = energyMap[0].length;
        int[][] cumulativeEnergyMap = new int[rows][cols];
        System.arraycopy(energyMap[0], 0, cumulativeEnergyMap[0], 0, cols);

        for (int i = 1; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int minEnergy = Integer.MAX_VALUE;


                int prevRow = i - 1;
                int prevCol = Math.max(j - 1, 0);
                int nextCol = Math.min(j + 1, cols - 1);


                minEnergy = Math.min(minEnergy, cumulativeEnergyMap[prevRow][prevCol]);
                minEnergy = Math.min(minEnergy, cumulativeEnergyMap[prevRow][j]);
                minEnergy = Math.min(minEnergy, cumulativeEnergyMap[prevRow][nextCol]);


                cumulativeEnergyMap[i][j] = energyMap[i][j] + minEnergy;
            }
        }

        return cumulativeEnergyMap;
    }
    public int[][] computeCumulativeVerticalEnergyMap(int[][] energyMap) {
        int rows = energyMap.length;
        int cols = energyMap[0].length;
        int[][] cumulativeVerticalEnergyMap = new int[rows][cols];


        for (int i = 0; i < rows; i++) {
            cumulativeVerticalEnergyMap[i][0] = energyMap[i][0];
        }

        for (int j = 1; j < cols; j++) {
            for (int i = 0; i < rows; i++) {
                int minEnergy = Integer.MAX_VALUE;


                int prevCol = j - 1;
                int prevRow = Math.max(i - 1, 0);
                int nextRow = Math.min(i + 1, rows - 1);


                minEnergy = Math.min(minEnergy, cumulativeVerticalEnergyMap[prevRow][prevCol]);
                minEnergy = Math.min(minEnergy, cumulativeVerticalEnergyMap[i][prevCol]);
                minEnergy = Math.min(minEnergy, cumulativeVerticalEnergyMap[nextRow][prevCol]);


                cumulativeVerticalEnergyMap[i][j] = energyMap[i][j] + minEnergy;
            }
        }

        return cumulativeVerticalEnergyMap;
    }

    private static int[] findHorizontalSeam(int[][] cumulativeEnergyMap) {
        int rows = cumulativeEnergyMap.length;
        int cols = cumulativeEnergyMap[0].length;


        int minEnergyCol = 0;
        int minEnergy = cumulativeEnergyMap[rows - 1][0];
        for (int j = 1; j < cols; j++) {
            if (cumulativeEnergyMap[rows - 1][j] < minEnergy) {
                minEnergy = cumulativeEnergyMap[rows - 1][j];
                minEnergyCol = j;
            }
        }

        int[] minEnergyPath = new int[rows];
        minEnergyPath[rows - 1] = minEnergyCol;


        for (int i = rows - 2; i >= 0; i--) {
            int currentCol = minEnergyPath[i + 1];
            int prevCol = currentCol;
            int nextCol = currentCol;


            if (currentCol > 0) {
                prevCol = currentCol - 1;
            }
            if (currentCol < cols - 1) {
                nextCol = currentCol + 1;
            }


            if (cumulativeEnergyMap[i][prevCol] <= cumulativeEnergyMap[i][currentCol]
                    && cumulativeEnergyMap[i][prevCol] <= cumulativeEnergyMap[i][nextCol]) {
                minEnergyPath[i] = prevCol;
            } else if (cumulativeEnergyMap[i][nextCol] <= cumulativeEnergyMap[i][currentCol]
                    && cumulativeEnergyMap[i][nextCol] <= cumulativeEnergyMap[i][prevCol]) {
                minEnergyPath[i] = nextCol;
            } else {
                minEnergyPath[i] = currentCol;
            }
        }

        return minEnergyPath;
    }
    public int[] findVerticalEnergySeam(int[][] cumulativeVerticalEnergyMap) {
        int rows = cumulativeVerticalEnergyMap.length;
        int cols = cumulativeVerticalEnergyMap[0].length;


        int minEnergyRow = 0;
        int minEnergy = cumulativeVerticalEnergyMap[0][cols - 1];
        for (int i = 1; i < rows; i++) {
            if (cumulativeVerticalEnergyMap[i][cols - 1] < minEnergy) {
                minEnergy = cumulativeVerticalEnergyMap[i][cols - 1];
                minEnergyRow = i;
            }
        }

        int[] minEnergyPath = new int[cols];
        minEnergyPath[cols - 1] = minEnergyRow;

        // 从倒数第二列开始，逐列向左寻找累积能量最小的邻居行
        for (int j = cols - 2; j >= 0; j--) {
            int currentRow = minEnergyPath[j + 1];
            int prevRow = currentRow;
            int nextRow = currentRow;

            // 限制索引范围，避免数组越界
            if (currentRow > 0) {
                prevRow = currentRow - 1;
            }
            if (currentRow < rows - 1) {
                nextRow = currentRow + 1;
            }

            // 选择累积能量最小的邻居行
            if (cumulativeVerticalEnergyMap[prevRow][j] <= cumulativeVerticalEnergyMap[currentRow][j]
                    && cumulativeVerticalEnergyMap[prevRow][j] <= cumulativeVerticalEnergyMap[nextRow][j]) {
                minEnergyPath[j] = prevRow;
            } else if (cumulativeVerticalEnergyMap[nextRow][j] <= cumulativeVerticalEnergyMap[currentRow][j]
                    && cumulativeVerticalEnergyMap[nextRow][j] <= cumulativeVerticalEnergyMap[prevRow][j]) {
                minEnergyPath[j] = nextRow;
            } else {
                minEnergyPath[j] = currentRow;
            }
        }

        return minEnergyPath;
    }

    private static BufferedImage removeHorizontalSeam(BufferedImage image, int[] seam) {
        int width = image.getWidth() ;
        int height = image.getHeight() - 1;

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < width; y++) {
            int seamX = seam[y];

            for (int x = 0; x < height; x++) {
                if (x < seamX) {
                    newImage.setRGB(y, height - 1 - x, image.getRGB(y, height - x));
                } else {
                    newImage.setRGB(y, height - 1 - x, image.getRGB(y,height - x - 1));
                }
            }
        }

        return newImage;
    }
    private static BufferedImage removeVerticalSeam(BufferedImage image, int[] seam) {
        int width = image.getWidth() - 1;
        int height = image.getHeight() ;

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < height; x++) {
            int seamY = seam[x];

            for (int y = 0; y < width; y++) {
                if (y < seamY) {
                    newImage.setRGB(y, height - 1 - x, image.getRGB(y, height - 1 - x));
                } else {
                    newImage.setRGB(y, height - 1 - x, image.getRGB(y + 1,height - x - 1));
                }
            }
        }

        return newImage;
    }

    public static List<int[]> findHorizontalSeams(int[][] cumulativeEnergyMap, int numSeams) {
        int rows = cumulativeEnergyMap.length;
        int cols = cumulativeEnergyMap[0].length;

        List<int[]> selectedPaths = new ArrayList<>();
        boolean[][] usedCols = new boolean[rows][cols];

        // 找到最后一行能量最小的点并排序
        Integer[] endCols = new Integer[cols];
        for (int j = 0; j < cols; j++) {
            endCols[j] = j;
        }
        Arrays.sort(endCols, Comparator.comparingInt(j -> cumulativeEnergyMap[rows - 1][j]));

        // 找到指定数量的最小能量路径
        for (int n = 0; n < numSeams; n++) {
            int[] minEnergyPath = null;
            int minEnergy = Integer.MAX_VALUE;

            for (int col : endCols) {
                if (usedCols[rows - 1][col]) {
                    continue; // 跳过已经使用的列
                }

                int[] path = new int[rows];
                path[rows - 1] = col;
                int energy = cumulativeEnergyMap[rows - 1][col];

                boolean validPath = true;

                // 从底部回溯到顶部
                for (int i = rows - 2; i >= 0; i--) {
                    int currentCol = path[i + 1];
                    int bestCol = currentCol;

                    // 检查上方、中间和下方的三个位置，选择未被使用且累积能量最小的位置
                    if (currentCol > 0 && !usedCols[i][currentCol - 1] && cumulativeEnergyMap[i][currentCol - 1] < cumulativeEnergyMap[i][bestCol]) {
                        int tempCol = currentCol - 1;
                        if (usedCols[i][tempCol]) continue;
                        bestCol = currentCol - 1;
                    }
                    if (currentCol < cols - 1 && !usedCols[i][currentCol + 1] && cumulativeEnergyMap[i][currentCol + 1] < cumulativeEnergyMap[i][bestCol]) {
                        int tempCol = currentCol + 1;
                        if (usedCols[i][tempCol]) continue;
                        bestCol = currentCol + 1;
                    }

                    path[i] = bestCol;
                    energy += cumulativeEnergyMap[i][bestCol];
                }

                if (validPath && energy < minEnergy) {
                    minEnergy = energy;
                    minEnergyPath = path;
                }
            }

            // 标记最小路径的列为已使用
            if (minEnergyPath != null) {
                for (int i = 0; i < rows; i++) {
                    usedCols[i][minEnergyPath[i]] = true;
                }
                selectedPaths.add(minEnergyPath);
            }
        }

        selectedPaths.sort((path1, path2) -> Integer.compare(path2[rows - 1], path1[rows - 1]));
        return selectedPaths;
    }
    public static List<int[]> findVerticalSeams(int[][] cumulativeEnergyMap, int numSeams) {
        int rows = cumulativeEnergyMap.length;
        int cols = cumulativeEnergyMap[0].length;

        List<int[]> selectedPaths = new ArrayList<>();
        boolean[][] usedRows = new boolean[rows][cols];

        // 找到最后一列能量最小的点并排序
        Integer[] endRows = new Integer[rows];
        for (int i = 0; i < rows; i++) {
            endRows[i] = i;
        }
        Arrays.sort(endRows, Comparator.comparingInt(i -> cumulativeEnergyMap[i][cols - 1]));

        // 找到指定数量的最小能量路径
        for (int n = 0; n < numSeams; n++) {
            int[] minEnergyPath = null;
            int minEnergy = Integer.MAX_VALUE;

            for (int row : endRows) {
                if (usedRows[row][cols - 1]) {
                    continue; // 跳过已经使用的行
                }

                int[] path = new int[cols];
                path[cols - 1] = row;
                int energy = cumulativeEnergyMap[row][cols - 1];

                boolean validPath = true;

                // 从右向左回溯到左边
                for (int j = cols - 2; j >= 0; j--) {
                    int currentRow = path[j + 1];
                    int bestRow = currentRow;

                    // 检查左上、左和左下的三个位置，选择未被使用且累积能量最小的位置
                    if (currentRow > 0 && !usedRows[currentRow - 1][j] && cumulativeEnergyMap[currentRow - 1][j] < cumulativeEnergyMap[bestRow][j]) {
                        int tempRow = currentRow - 1;
                        if (usedRows[tempRow][j]) continue;
                        bestRow = currentRow - 1;
                    }
                    if (currentRow < rows - 1 && !usedRows[currentRow + 1][j] && cumulativeEnergyMap[currentRow + 1][j] < cumulativeEnergyMap[bestRow][j]) {
                        int tempRow = currentRow + 1;
                        if (usedRows[tempRow][j]) continue;
                        bestRow = currentRow + 1;
                    }

                    path[j] = bestRow;
                    energy += cumulativeEnergyMap[bestRow][j];
                }

                if (validPath && energy < minEnergy) {
                    minEnergy = energy;
                    minEnergyPath = path;
                }
            }

            // 标记最小路径的行为已使用
            if (minEnergyPath != null) {
                for (int j = 0; j < cols; j++) {
                    usedRows[minEnergyPath[j]][j] = true;
                }
                selectedPaths.add(minEnergyPath);
            }
        }

        // 对selectedPaths按每条路径的最后一个点的位置由大到小排序
        selectedPaths.sort((path1, path2) -> Integer.compare(path2[cols - 1], path1[cols - 1]));

        return selectedPaths;
    }
    public static void displayPaths(List<int[]> paths, BufferedImage image){
        for (int i = 0; i < paths.size(); i++) {
            int[] temp = paths.get(i);
            for (int j = 0; j < image.getHeight(); j++) {
                image.setRGB(temp[j],j, Color.red.getRGB());
            }
        }
        ImageProcessor.displayImage(image);
    }
}
