import Foundation

public class BrightnessFilter : Filter {
    public func process(rgbaImage: RGBAImage, value : Int) -> RGBAImage {
        
        
        for y in 0..<rgbaImage.height  {
            for x in 0..<rgbaImage.width{
                
                let index = y * rgbaImage.width + x
                var pixel = rgbaImage.pixels[index]
                
                pixel.red = UInt8(max(min(255, Int(pixel.red) + value) , 0))
                pixel.green = UInt8(max(min(255, Int(pixel.green) + value) , 0))
                pixel.blue = UInt8(max(min(255, Int(pixel.blue) + value) , 0))
                rgbaImage.pixels[index] = pixel;
            }
        }
        
        
        return rgbaImage
    }
}