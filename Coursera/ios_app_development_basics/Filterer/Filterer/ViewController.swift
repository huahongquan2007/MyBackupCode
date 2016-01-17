//
//  ViewController.swift
//  Filterer
//
//  Created by Jack on 2015-09-22.
//  Copyright © 2015 UofT. All rights reserved.
//

import UIKit

class ViewController: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate {

    var originalImage: UIImage?
    var filteredImage: UIImage?
    var isOriginalImage: Bool = true
    var imageProcessor: ImageProcessor? = nil

    
    
    @IBOutlet var imageView: UIImageView!
    
    @IBOutlet weak var imageViewTop: UIImageView!
    @IBOutlet var secondaryMenu: UIView!
    @IBOutlet var bottomMenu: UIView!
    
    @IBOutlet var filterButton: UIButton!
    @IBOutlet weak var compareButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        secondaryMenu.backgroundColor = UIColor.whiteColor().colorWithAlphaComponent(0.5)
        secondaryMenu.translatesAutoresizingMaskIntoConstraints = false
        
        imageProcessor = ImageProcessor()
        isOriginalImage = true
        originalImage = self.imageView.image
        compareButton.enabled = false
        imageViewTop.alpha = 0
    }

    // MARK: Share
    @IBAction func onShare(sender: AnyObject) {
        let activityController = UIActivityViewController(activityItems: ["Check out our really cool app", imageView.image!], applicationActivities: nil)
        presentViewController(activityController, animated: true, completion: nil)
    }
    
    // MARK: New Photo
    @IBAction func onNewPhoto(sender: AnyObject) {
        let actionSheet = UIAlertController(title: "New Photo", message: nil, preferredStyle: .ActionSheet)
        
        actionSheet.addAction(UIAlertAction(title: "Camera", style: .Default, handler: { action in
            self.showCamera()
        }))
        
        actionSheet.addAction(UIAlertAction(title: "Album", style: .Default, handler: { action in
            self.showAlbum()
        }))
        
        actionSheet.addAction(UIAlertAction(title: "Cancel", style: .Cancel, handler: nil))
        
        self.presentViewController(actionSheet, animated: true, completion: nil)
    }
    
    func showCamera() {
        let cameraPicker = UIImagePickerController()
        cameraPicker.delegate = self
        cameraPicker.sourceType = .Camera
        
        presentViewController(cameraPicker, animated: true, completion: nil)
    }
    
    func showAlbum() {
        let cameraPicker = UIImagePickerController()
        cameraPicker.delegate = self
        cameraPicker.sourceType = .PhotoLibrary
        
        presentViewController(cameraPicker, animated: true, completion: nil)
    }
    
    // MARK: UIImagePickerControllerDelegate
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        dismissViewControllerAnimated(true, completion: nil)
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            imageView.image = image
            
            originalImage = image
            isOriginalImage = true
            compareButton.enabled = false
            imageViewTop.alpha = 0
        }
    }
    
    func imagePickerControllerDidCancel(picker: UIImagePickerController) {
        dismissViewControllerAnimated(true, completion: nil)
    }
    
    // MARK: Filter Menu
    @IBAction func onFilter(sender: UIButton) {
        if (sender.selected) {
            hideSecondaryMenu()
            sender.selected = false
        } else {
            showSecondaryMenu()
            sender.selected = true
        }
    }
    
    func showSecondaryMenu() {
        view.addSubview(secondaryMenu)
        
        let bottomConstraint = secondaryMenu.bottomAnchor.constraintEqualToAnchor(bottomMenu.topAnchor)
        let leftConstraint = secondaryMenu.leftAnchor.constraintEqualToAnchor(view.leftAnchor)
        let rightConstraint = secondaryMenu.rightAnchor.constraintEqualToAnchor(view.rightAnchor)
        
        let heightConstraint = secondaryMenu.heightAnchor.constraintEqualToConstant(44)
        
        NSLayoutConstraint.activateConstraints([bottomConstraint, leftConstraint, rightConstraint, heightConstraint])
        
        view.layoutIfNeeded()
        
        self.secondaryMenu.alpha = 0
        UIView.animateWithDuration(0.4) {
            self.secondaryMenu.alpha = 1.0
        }
    }

    func hideSecondaryMenu() {
        UIView.animateWithDuration(0.4, animations: {
            self.secondaryMenu.alpha = 0
            }) { completed in
                if completed == true {
                    self.secondaryMenu.removeFromSuperview()
                }
        }
    }
    
    // MARK: Filter
    
    @IBAction func FilterGray(sender: AnyObject) {
        if let imageProcessor = imageProcessor {
            filteredImage = imageProcessor.filter(originalImage!, filterName: Filter.GRAY)
        }
        compareButton.enabled = true
        setImageViewTop();
        toggleImageView();
    }
    @IBAction func FilterBlur(sender: AnyObject) {
        if let imageProcessor = imageProcessor {
            filteredImage = imageProcessor.filter(originalImage!, filterName: Filter.BLUR)
        }
        compareButton.enabled = true
        setImageViewTop();
        toggleImageView();
    }
    @IBAction func FilterMotion(sender: AnyObject) {
        if let imageProcessor = imageProcessor {
            filteredImage = imageProcessor.filter(originalImage!, filterName: Filter.MOTION)
        }
        compareButton.enabled = true
        setImageViewTop();
        toggleImageView();
    }
    @IBAction func FilterBright(sender: AnyObject) {
        if let imageProcessor = imageProcessor {
            filteredImage = imageProcessor.filter(originalImage!, filterName: Filter.BRIGHTNESS)
        }
        compareButton.enabled = true
        setImageViewTop();
        toggleImageView();
    }
    @IBAction func FilterContrast(sender: AnyObject) {
        if let imageProcessor = imageProcessor {
            filteredImage = imageProcessor.filter(originalImage!, filterName: Filter.CONTRAST)
        }
        compareButton.enabled = true
        setImageViewTop();
        toggleImageView();
    }
    
    // MARK: Compare
    
    @IBAction func Compare(sender: AnyObject) {
        toggleImageView();
    }
    
    func setImageViewTop(){
        self.imageViewTop.image = filteredImage
    }
    func toggleImageView(){
        isOriginalImage = !isOriginalImage
        
        if (isOriginalImage){
            UIView.animateWithDuration(0.4) {
                self.imageViewTop.alpha = 0.0
            }
        } else {
            UIView.animateWithDuration(0.4) {
                self.imageViewTop.alpha = 1.0
            }
        }
    }
    
}

