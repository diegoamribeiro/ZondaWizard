//
//  SMSHandler.swift
//  iosApp
//
//  Created by DIEGO RIBEIRO on 09/03/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import Foundation
import UIKit

@objc public class SMSHandler : NSObject {
    @objc public static func sendSms(number: String, message: String){
        let sms = "sms:\(number)&body=\(message)"
        if let strURL = sms.addingPercentEncoding(withAllowedCharacters:.urlQueryAllowed),
           let url = URL(string: strURL){
            UIApplication.shared.open(url, options: [:],
                                      completionHandler: nil)
        }else {
            print("Invalid SMS URL")
        }
    }
}
