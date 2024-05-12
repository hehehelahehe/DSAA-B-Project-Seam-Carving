import java.awt.image.BufferedImage;


public class SeamCarver {
    public BufferedImage shrinkImage(BufferedImage image, int targetWidth, int targetHeight) {
        int currentWidth = image.getWidth();
        int currentHeight = image.getHeight();

        while (currentWidth > targetWidth || currentHeight > targetHeight) {
            int[][] energyMap = computeEnergyMap(image);
            int[][] cumulativeEnergyMap = computeCumulativeEnergyMap(energyMap);
            int[] seam = findHorizontalSeam(cumulativeEnergyMap);

            image = removeHorizontalSeam(image, seam);
            currentHeight--;
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
        System.out.println(test.length);
        System.out.println(test[0].length);
        // Compute the energy map of the image (e.g., using gradient-based methods)
        return test;
        //return ImageProcessor.edgeDetectGray(ImageProcessor.convert2DArrayTO4DArray(image));
    }

    private static int[][] computeCumulativeEnergyMap(int[][] energyMap) {
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


}
