import Foundation

public class BlurFilter : Filter {
    public override init?() {
        blurSize = 3
        super.init()
    }
    private var blurSize : Int
    public func setBlurSize(size: Int){
        blurSize = size
    }
    
    public override func process(rgbaImage: RGBAImage) -> RGBAImage {
        
        var filter:[Double] = []
        
        let squaredSize = blurSize * blurSize
        for _ in 0..<squaredSize{
            filter.append(1.0)
        }

        let filterSize = blurSize / 2
            
        return applyFilter(rgbaImage, filter: filter, filterSize: filterSize)
    }
}