import Foundation

public class MotionFilter : Filter{
    public override func process(rgbaImage: RGBAImage) -> RGBAImage {
        
        let filter = [1.0, 0.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 0.0, 1.0]
        let filterSize = 9 / 2
        
        return applyFilter(rgbaImage, filter: filter, filterSize: filterSize)
    }
}