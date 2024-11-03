package com.rintisa.util;


import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class SvgConverter {
    private static final Logger logger = LoggerFactory.getLogger(SvgConverter.class);
    private static final Path CACHE_DIR = Paths.get("cache", "icons");

    static {
        try {
            Files.createDirectories(CACHE_DIR);
        } catch (IOException e) {
            logger.error("No se pudo crear el directorio de caché", e);
        }
    }

    public static BufferedImage convertSvgToPng(String svgName, int width, int height) {
        try {
            // Verificar caché
            Path pngPath = CACHE_DIR.resolve(svgName + "_" + width + "x" + height + ".png");
            if (Files.exists(pngPath)) {
                return ImageIO.read(pngPath.toFile());
            }

            // Cargar SVG
            String svgPath = "/icons/" + svgName + ".svg";
            try (InputStream svgStream = SvgConverter.class.getResourceAsStream(svgPath)) {
                if (svgStream == null) {
                    throw new IOException("No se encontró el recurso: " + svgPath);
                }

                // Configurar el transcodificador
                PNGTranscoder transcoder = new PNGTranscoder();
                transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float)width);
                transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float)height);

                // Convertir SVG a PNG
                TranscoderInput input = new TranscoderInput(svgStream);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput(outputStream);
                transcoder.transcode(input, output);

                // Crear imagen desde bytes
                byte[] imageData = outputStream.toByteArray();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));

                // Guardar en caché
                ImageIO.write(image, "PNG", pngPath.toFile());

                return image;
            }
        } catch (Exception e) {
            logger.error("Error al convertir SVG a PNG: " + svgName, e);
            return createFallbackImage(width, height);
        }
    }

    private static BufferedImage createFallbackImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.createGraphics().dispose();
        return image;
    }
}
