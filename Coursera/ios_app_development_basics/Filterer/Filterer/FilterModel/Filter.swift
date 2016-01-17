import UIKit

public class Filter{
    
    public static let BLUR = "blur"
    public static let BRIGHTNESS = "brightness"
    public static let GRAY = "gray"
    public static let CONTRAST = "constrast"
    public static let MOTION = "motion"
    
    public init?(){
        
    }
    
    public func process(image: RGBAImage) -> RGBAImage {
        return image
    }
    
    public func applyFilter(rgbaImage: RGBAImage, filter: [Double], filterSize: Int) -> RGBAImage{
        for y in filterSize..<rgbaImage.height - filterSize  {
            for x in filterSize..<rgbaImage.width - filterSize {
                
                let index = y * rgbaImage.width + x
                var pixel = rgbaImage.pixels[index]
                var idx = 0
                var total = 0.0
                var redValue = 0.0
                var blueValue = 0.0
                var greenValue = 0.0
                for i in -1...1{
                    for j in -1...1{
                        let curIndex = (y + i) * rgbaImage.width + (x + j)
                        var curPixel = rgbaImage.pixels[curIndex]
                        
                        redValue = redValue + filter[idx] * Double(Int(curPixel.red))
                        greenValue = greenValue + filter[idx] * Double(Int(curPixel.green))
                        blueValue = blueValue + filter[idx] * Double(Int(curPixel.blue))
                        
                        total = total + filter[idx]
                        
                        idx = idx + 1
                    }
                }

                pixel.red = UInt8(max(min(255, Int(redValue / total)) , 0))
                pixel.blue = UInt8(max(min(255, Int(blueValue / total)) , 0))
                pixel.green = UInt8(max(min(255, Int(greenValue / total)) , 0))
                rgbaImage.pixels[index] = pixel
            }
        }
        
        return rgbaImage
    }
}