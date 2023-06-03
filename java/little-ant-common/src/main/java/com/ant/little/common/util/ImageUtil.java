package com.ant.little.common.util;

import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/1/25
 * @Version 1.0
 **/
public class ImageUtil {
    public final static int SCALE_SIZE = 12;

    public static BufferedImage readImage(String fileName) throws IOException {
        final File imageFile = new File(fileName);
        final BufferedImage image = ImageIO.read(imageFile);
        return image;
    }

    public static int[][] readGrayImage(String fileName) throws IOException {
        final Raster raster = readImage(fileName).getData();
// --- Confirm that: raster.getTransferType() == DataBuffer.TYPE_BYTE
        int[][] imageArray = new int[raster.getHeight()][];//image.getWidth()];
        for (int y = 0, yLimit = raster.getHeight(); y < yLimit; y++) {
            imageArray[y] = new int[raster.getWidth()];
            for (int x = 0, xLimit = raster.getWidth(); x < xLimit; x++) {
                final Object dataObject = raster.getDataElements(x, y, null);
                final byte[] pixelData = (byte[]) dataObject;
                final int grayscalePixelValue = pixelData[0] & 0xFF;
                imageArray[y][x] = grayscalePixelValue / SCALE_SIZE;
            }
        }
        return imageArray;
    }

    //BufferedImage 转base64
    public static String GetBase64FromImage(BufferedImage img) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // 设置图片的格式
            ImageIO.write(img, "jpg", stream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] bytes = Base64.encodeBase64(stream.toByteArray());
        String base64 = new String(bytes);
        return "data:image/jpg;base64," + base64;
    }


    public static void main(String[] args) throws IOException {
        int image[][] = readGrayImage("/Users/yuwanglin/project/weixin-little-ant/python/game_map/mori_game_map_gray.png");
        System.out.printf("123");
    }
}
