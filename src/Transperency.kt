package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main() {
    println("Input the image filename:")
    val inputImage = readLine()!!
    val inputImageFile = File(inputImage)

    if (!inputImageFile.exists()) {
        println("The file $inputImage doesn't exist.")
        return
    }

    val bufferedInputImage = ImageIO.read(inputImageFile)
    if (bufferedInputImage.colorModel.numColorComponents != 3) {
        println("The number of image color components isn't 3.")
        return
    }

    if (!(bufferedInputImage.colorModel.pixelSize == 24 || bufferedInputImage.colorModel.pixelSize == 32)) {
        println("The image isn't 24 or 32-bit.")
        return
    }

    //* watermark

    println("Input the watermark image filename:")
    val watermark = readLine()!!
    val watermarkFile = File(watermark)

    if (!watermarkFile.exists()) {
        println("The file $watermark doesn't exist.")
        return
    }

    val bufferedWatermarkImage = ImageIO.read(watermarkFile)
    if (bufferedWatermarkImage.colorModel.numColorComponents != 3) {
        println("The number of watermark color components isn't 3.")
        return
    }

    if (!(bufferedWatermarkImage.colorModel.pixelSize == 24 || bufferedWatermarkImage.colorModel.pixelSize == 32)) {
        println("The watermark isn't 24 or 32-bit.")
        return
    }

    if (bufferedInputImage.width < bufferedWatermarkImage.width
        || bufferedInputImage.height < bufferedWatermarkImage.height
    ) {
        println("The watermark's dimensions are larger.")
        return
    }

    val transparency = when (bufferedWatermarkImage.transparency) {
        1, 2 -> "NON-TRANSLUCENT"
        else -> "TRANSLUCENT"
    }


    var useAlphaChannel = "no"
    var useTransparentColor = "no"


    if (transparency == "TRANSLUCENT") {
        println("Do you want to use the watermark's Alpha channel?")
        useAlphaChannel = readLine()!!
    } else {
        println("Do you want to set a transparency color?")
        useTransparentColor = readLine()!!
    }


    //TODO when everything is -1
    var r = 0
    var g = 0
    var b = 0

    if (useTransparentColor == "yes") {
        println("Input a transparency color ([Red] [Green] [Blue]):")

        val colorList = readLine()!!.split(" ").map {
            try {
                it.toInt()
            } catch (e: Exception) {
                println("The transparency color input is invalid.")
                return
            }
        }

        for (it in colorList) {
            if (it !in 0..255) {
                println("The transparency color input is invalid.")
                return
            }
        }


        if (colorList.size == 3) {
            r = colorList[0]
            g = colorList[1]
            b = colorList[2]

        } else {
            println("The transparency color input is invalid.")
            return
        }

    }
    val transparencyColor = Color(r, g, b)

    println("Input the watermark transparency percentage (Integer 0-100):")
    val blendValue = readLine()!!
    if (!isNumber(blendValue)) {
        println("The transparency percentage isn't an integer number.")
        return
    }
    val weight = blendValue.toInt()
    if (weight > 100 || weight < 0) {
        println("The transparency percentage is out of range.")
        return
    }

    println("Choose the position method (single, grid):")

    var xStart = -1
    var yStart = -1
    val position = readLine()!!

    when (position) {
        "single" -> {
            val xRange = bufferedInputImage.width - bufferedWatermarkImage.width
            val yRange = bufferedInputImage.height - bufferedWatermarkImage.height
            println("Input the watermark position ([x 0-$xRange], [y 0-$yRange]):")
            val range = readLine()!!.split(" ").map {
                try {
                    it.toInt()
                } catch (e: Exception) {
                    println("The position input is invalid.")
                    return
                }
            }

            if (range[0] !in 0..xRange) {
                println("The position input is out of range.")
                return
            }
            if (range[1] !in 0..yRange) {
                println("The position input is out of range.")
                return
            }

            if (range.size == 2) {
                xStart = range[0]
                yStart = range[1]

            } else {
                println("The position input is invalid.")
                return
            }

        }
        "grid" -> {
            //Do nothing
        }
        else -> {
            println("The position method input is invalid.")
            return
        }
    }


    println("Input the output image filename (jpg or png extension):")
    val fileName = readLine()!!

    val extension = fileName.substring(fileName.lastIndexOf('.') + 1)

    if (!((extension == "jpg") || (extension == "png"))) {
        println("The output file extension isn't \"jpg\" or \"png\".")
        return
    }


    when {
        useAlphaChannel == "yes" -> {
            constructImage(
                bufferedInputImage,
                bufferedWatermarkImage,
                weight,
                position,
                xStart,
                yStart,
                true,
                false,
                transparencyColor
            )
        }
        useTransparentColor == "yes" -> {
            constructImage(
                bufferedInputImage,
                bufferedWatermarkImage,
                weight,
                position,
                xStart,
                yStart,
                false,
                true,
                transparencyColor

            )
        }
        else -> {
            constructImage(
                bufferedInputImage,
                bufferedWatermarkImage,
                weight,
                position,
                xStart,
                yStart,
                false,
                false,
                transparencyColor
            )
        }
    }


    val outputFile = File(fileName) // Output the file
    ImageIO.write(bufferedInputImage, "$extension", outputFile)

    println("The watermarked image $fileName has been created.")

}

private fun constructImage(
    bufferedInputImage: BufferedImage,
    bufferedWatermarkImage: BufferedImage,
    weight: Int,
    position: String,
    xStart: Int,
    yStart: Int,
    useAlpha: Boolean,
    useTransparency: Boolean,
    transparencyColor: Color
) {


    when (position) {
        "single" -> {
            for (x in 0 until bufferedWatermarkImage.width) {               // For every column.
                for (y in 0 until bufferedWatermarkImage.height) {
                    // For every row

                    when {
                        useAlpha -> {
                            val w =
                                Color(bufferedWatermarkImage.getRGB(x, y), true)  // Read color from the (x, y) position
                            val i = Color(bufferedInputImage.getRGB(x + xStart, y + yStart))
                            if (w.alpha != 0) {
                                val color = Color(
                                    (weight * w.red + (100 - weight) * i.red) / 100,
                                    (weight * w.green + (100 - weight) * i.green) / 100,
                                    (weight * w.blue + (100 - weight) * i.blue) / 100
                                )
                                bufferedInputImage.setRGB(x + xStart, y + yStart, color.rgb)
                            }
                        }
                        useTransparency -> {
                            val w = Color(bufferedWatermarkImage.getRGB(x, y))  // Read color from the (x, y) position
                            if (!(w.red == transparencyColor.red && w.blue == transparencyColor.blue && w.green == transparencyColor.green)) {
                                val i = Color(bufferedInputImage.getRGB(x + xStart, y + yStart))
                                val color = Color(
                                    (weight * w.red + (100 - weight) * i.red) / 100,
                                    (weight * w.green + (100 - weight) * i.green) / 100,
                                    (weight * w.blue + (100 - weight) * i.blue) / 100
                                )
                                bufferedInputImage.setRGB(x + xStart, y + yStart, color.rgb)
                            }
                        }
                        else -> {
                            val w = Color(bufferedWatermarkImage.getRGB(x, y))  // Read color from the (x, y) position
                            val i = Color(bufferedInputImage.getRGB(x + xStart, y + yStart))
                            val color = Color(
                                (weight * w.red + (100 - weight) * i.red) / 100,
                                (weight * w.green + (100 - weight) * i.green) / 100,
                                (weight * w.blue + (100 - weight) * i.blue) / 100
                            )
                            bufferedInputImage.setRGB(x + xStart, y + yStart, color.rgb)

                        }
                    }

                }
            }
        }

        "grid" -> {

            for (x in 0 until bufferedInputImage.width) {               // For every column.
                for (y in 0 until bufferedInputImage.height) {
                    // For every row
                    val w = Color(
                        bufferedWatermarkImage.getRGB(
                            x % bufferedWatermarkImage.width,
                            y % bufferedWatermarkImage.height
                        ), useAlpha
                    )

                    // Read color from the (x, y) position

                    when {
                        useAlpha -> {
                            val i = Color(bufferedInputImage.getRGB(x, y))
                            if (w.alpha != 0) {
                                val color = Color(
                                    (weight * w.red + (100 - weight) * i.red) / 100,
                                    (weight * w.green + (100 - weight) * i.green) / 100,
                                    (weight * w.blue + (100 - weight) * i.blue) / 100
                                )
                                bufferedInputImage.setRGB(x, y, color.rgb)
                            }
                        }
                        useTransparency -> {
                            if (!(w.red == transparencyColor.red && w.blue == transparencyColor.blue && w.green == transparencyColor.green)) {
                                val i = Color(bufferedInputImage.getRGB(x, y))
                                val color = Color(
                                    (weight * w.red + (100 - weight) * i.red) / 100,
                                    (weight * w.green + (100 - weight) * i.green) / 100,
                                    (weight * w.blue + (100 - weight) * i.blue) / 100
                                )
                                bufferedInputImage.setRGB(x, y, color.rgb)
                            }
                        }
                        else -> {
                            val i = Color(bufferedInputImage.getRGB(x, y))
                            val color = Color(
                                (weight * w.red + (100 - weight) * i.red) / 100,
                                (weight * w.green + (100 - weight) * i.green) / 100,
                                (weight * w.blue + (100 - weight) * i.blue) / 100
                            )
                            bufferedInputImage.setRGB(x, y, color.rgb)
                        }
                    }
                }
            }
        }
    }
}


fun isNumber(s: String): Boolean {
    return s.matches(Regex("\\d+"))
}



