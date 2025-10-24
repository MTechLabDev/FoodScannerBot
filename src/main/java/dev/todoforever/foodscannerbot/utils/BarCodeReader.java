package dev.todoforever.foodscannerbot.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

public final class BarCodeReader {
    private static final Map<DecodeHintType, Object> HINTS = Map.of(
            DecodeHintType.TRY_HARDER, Boolean.TRUE,
            DecodeHintType.ALSO_INVERTED, Boolean.TRUE,
            DecodeHintType.POSSIBLE_FORMATS, List.of(
                    BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                    BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                    BarcodeFormat.CODE_128, BarcodeFormat.CODE_39, BarcodeFormat.ITF,
                    BarcodeFormat.QR_CODE, BarcodeFormat.DATA_MATRIX
            )
    );

    private BarCodeReader() {}

    public static String read(String path) {
        try { return read(ImageIO.read(new File(path))); }
        catch (Exception e) { return null; }
    }

    public static String read(BufferedImage img) {
        if (img == null) return null;
        int[] angles = {0, 90, 180, 270};
        for (int a : angles) {
            BufferedImage r = (a == 0) ? img : rotate(img, a);
            LuminanceSource src = new BufferedImageLuminanceSource(r);
            String s = tryDecode(new BinaryBitmap(new HybridBinarizer(src)));
            if (s != null) return s;
            s = tryDecode(new BinaryBitmap(new GlobalHistogramBinarizer(src)));
            if (s != null) return s;
        }
        return null;
    }

    private static String tryDecode(BinaryBitmap bmp) {
        try { return new MultiFormatReader().decode(bmp, HINTS).getText(); }
        catch (NotFoundException e) { return null; }
    }

    private static BufferedImage rotate(BufferedImage img, int deg) {
        AffineTransform tx = AffineTransform.getRotateInstance(
                Math.toRadians(deg), img.getWidth()/2.0, img.getHeight()/2.0);
        return new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR).filter(img, null);
    }
}