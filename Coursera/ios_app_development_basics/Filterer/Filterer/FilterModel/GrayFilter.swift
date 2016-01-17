import Foundation
import UIKit

public class GrayFilter : Filter {
    public override func process(image: RGBAImage) -> RGBAImage {
        for y in 0..<image.height {
            for x in 0..<image.width {
                let index = y * image.width + x
                var pixel = image.pixels[index]
                
                let gray = (Int(pixel.green) + Int(pixel.red) + Int(pixel.blue))/3
                pixel.red = UInt8(gray)
                pixel.blue = UInt8(gray)
                pixel.green = UInt8(gray)
                image.pixels[index] = pixel
            }
        }
        return image
    }
}