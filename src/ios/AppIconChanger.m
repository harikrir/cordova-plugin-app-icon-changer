#import "Cordova/CDV.h"
#import "AppIconChanger.h"
@implementation AppIconChanger
- (void)isSupported:(CDVInvokedUrlCommand*)command
{
   BOOL supported = NO;
   if (@available(iOS 10.3, *)) {
       supported = [[UIApplication sharedApplication] supportsAlternateIcons];
   }
   CDVPluginResult* result =
   [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                        messageAsBool:supported];
   [self.commandDelegate sendPluginResult:result
                               callbackId:command.callbackId];
}
- (void)changeIcon:(CDVInvokedUrlCommand*)command
{
   if (@available(iOS 10.3, *)) {
       if (![[UIApplication sharedApplication] supportsAlternateIcons]) {
           CDVPluginResult* result =
           [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                             messageAsString:@"Alternate icons not supported"];
           [self.commandDelegate sendPluginResult:result
                                       callbackId:command.callbackId];
           return;
       }
       NSDictionary* options = [command.arguments objectAtIndex:0];
       NSString* iconName = options[@"iconName"];
       dispatch_async(dispatch_get_main_queue(), ^{
           [[UIApplication sharedApplication]
            setAlternateIconName:iconName
            completionHandler:^(NSError * _Nullable error) {
               if (error) {
                   NSLog(@"ICON ERROR: %@", error);
                   CDVPluginResult* result =
                   [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                     messageAsString:error.localizedDescription];
                   [self.commandDelegate sendPluginResult:result
                                               callbackId:command.callbackId];
               } else {
                   NSLog(@"ICON CHANGED");
                   CDVPluginResult* result =
                   [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                   [self.commandDelegate sendPluginResult:result
                                               callbackId:command.callbackId];
               }
           }];
       });
   } else {
       CDVPluginResult* result =
       [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                         messageAsString:@"iOS version not supported"];
       [self.commandDelegate sendPluginResult:result
                                   callbackId:command.callbackId];
   }
}
@end
