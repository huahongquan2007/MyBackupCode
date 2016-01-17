//
//  ImageProcessor.swift
//  
//
//  Created by Quan Hua on 1/11/16.
//
//

import UIKit

public class ImageProcessor{
    
    var listFilter: [Filter] = []
    
    public init?(){
    }
    
    public func clearListFilter(){
        listFilter.removeAll()
    }
    
    public func insertFilter(filter: Filter){
        listFilter.append(filter)
    }
    public func insertFilter(filter: Filter, atIndex: Int){
        listFilter.insert(filter, atIndex: atIndex)
    }
    public func applyListFilter(image: UIImage) -> UIImage {
        
        var rgbaImage = RGBAImage(image: image)!
        var filteredImage = nil as UIImage?
        
        
        for filter in self.listFilter.enumerate(){
            rgbaImage = filter.element.process(rgbaImage)
        }
        
        filteredImage = rgbaImage.toUIImage()
        if (filteredImage == nil){
            return image
        }
        
        return filteredImage!
    }
    
    public func filter(image: UIImage, filter: Filter) -> UIImage{
        let rgbaImage = RGBAImage(image: image)!
        return filter.process(rgbaImage).toUIImage()!
    }
    public func filter(image: UIImage, filterName: String) -> UIImage{
        
        let rgbaImage = RGBAImage(image: image)!
        var filteredImage = nil as UIImage?
        switch filterName {
        case Filter.BLUR:
            filteredImage = BlurFilter()!.process(rgbaImage).toUIImage()
        case Filter.BRIGHTNESS:
            filteredImage = BrightnessFilter()!.process(rgbaImage, value: 50).toUIImage()
        case Filter.CONTRAST:
            filteredImage = ContrastFilter()!.process(rgbaImage, value: 1.5).toUIImage()
        case Filter.GRAY:
            filteredImage = GrayFilter()!.process(rgbaImage).toUIImage()
        case Filter.MOTION:
            filteredImage = MotionFilter()!.process(rgbaImage).toUIImage()
        default:
            filteredImage = nil
        }
        
        if(filteredImage == nil){
            return image
        }
        
        return filteredImage!
    }
}