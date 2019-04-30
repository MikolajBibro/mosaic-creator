import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val thumbnailSize = 1

    val inputImage = ImageIO.read(File("test.jpg"))
    val newImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)
    val g2d = newImage.createGraphics()

    val thumbnails: List<BufferedImage> = getImagesFromDirectory(File("CAT_00"))
    val scaledThumbnails: List<BufferedImage> = thumbnails.map { scaleImage(it, thumbnailSize) }.toList()

    val imageToColorMap = createImageToColorMap(scaledThumbnails)

    for (i in 0 until inputImage.width / thumbnailSize) {
        for (j in 0 until inputImage.height / thumbnailSize) {
            val subImage = inputImage.getSubimage(i * thumbnailSize, j * thumbnailSize, thumbnailSize, thumbnailSize)
            val bestMatchingImage = findBestMatchingImage(subImage, imageToColorMap)
            g2d.drawImage(bestMatchingImage, i * thumbnailSize, j * thumbnailSize, null)
        }
    }

    val outputFile = File("outputFile.jpg")
    ImageIO.write(newImage, "jpg", outputFile)
}

fun findBestMatchingImage(image: BufferedImage, imagesAndColors: Map<BufferedImage, Color>): BufferedImage? {
    val color = getAverageColor(image)
    var bestMatching: BufferedImage? = null
    var min = Int.MAX_VALUE

    for (img in imagesAndColors) {
        val tempColor = img.value
        val r = color.red - tempColor.red
        val g = color.green - tempColor.green
        val b = color.blue - tempColor.blue
        val value = r * r + g * g + b * b

        if (value < min) {
            min = value
            bestMatching = img.key
        }
    }
    return bestMatching
}

fun BufferedImage.averageColor() = getAverageColor(this)

fun createImageToColorMap(images: List<BufferedImage>) = images.map { it to it.averageColor() }.toMap()

fun getAverageColor(image: BufferedImage): Color {
    var r = 0
    var g = 0
    var b = 0
    val num = image.width * image.height

    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            val color = Color(image.getRGB(i, j))
            r += color.red
            g += color.green
            b += color.blue
        }
    }
    return Color(r / num, g / num, b / num)
}

fun scaleImage(image: BufferedImage, size: Int): BufferedImage {
    val tmp: Image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH)
    val scaledImage = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
    val graphic: Graphics2D = scaledImage.createGraphics()
    graphic.drawImage(tmp, 0, 0, null)
    graphic.dispose()
    return scaledImage
}

fun getImagesFromDirectory(directory: File) = directory.listFiles().filter { it.name.endsWith("jpg") }.map { ImageIO.read(it) }
