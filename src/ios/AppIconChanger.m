#import "AppIconChanger.h"
#import <UIKit/UIKit.h>

@implementation AppIconChanger

#pragma mark - Public API

- (void)isSupported:(CDVInvokedUrlCommand *)command
{
    BOOL supported = [[UIApplication sharedApplication] supportsAlternateIcons];
    CDVPluginResult *result =
        [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                           messageAsBool:supported];

    [self.commandDelegate sendPluginResult:result
                                callbackId:command.callbackId];
}

- (void)changeIcon:(CDVInvokedUrlCommand *)command
{
    if (![[UIApplication sharedApplication] supportsAlternateIcons]) {
        [self sendError:@"Alternate icons not supported on this iOS version"
               callback:command.callbackId];
        return;
    }

    NSDictionary *options =
        command.arguments.count > 0 ? command.arguments[0] : nil;

    NSString *iconName = options[@"iconName"];

    BOOL suppressUserNotification =
        options[@"suppressUserNotification"] == nil ||
        [options[@"suppressUserNotification"] boolValue];

    if (iconName == nil || iconName.length == 0) {
        [self sendError:@"'iconName' is mandatory"
               callback:command.callbackId];
        return;
    }

    __weak typeof(self) weakSelf = self;

    [[UIApplication sharedApplication]
        setAlternateIconName:iconName
           completionHandler:^(NSError *error) {

        if (suppressUserNotification) {
            [weakSelf suppressIconAlertBestEffort];
        }

        if (error) {
            NSString *msg =
                error.localizedDescription != nil
                ? error.localizedDescription
                : [NSString stringWithFormat:@"iOS error code %ld",
                   (long)error.code];

            [weakSelf sendError:msg callback:command.callbackId];
        } else {
            CDVPluginResult *ok =
                [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [weakSelf.commandDelegate sendPluginResult:ok
                                            callbackId:command.callbackId];
        }
    }];
}

#pragma mark - Helpers

- (void)sendError:(NSString *)message callback:(NSString *)callbackId
{
    CDVPluginResult *error =
        [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                          messageAsString:message ?: @"Unknown error"];
    [self.commandDelegate sendPluginResult:error callbackId:callbackId];
}

/**
 Best‑effort only. iOS may still show the alert.
 */
- (void)suppressIconAlertBestEffort
{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc = [UIViewController new];
        vc.view.alpha = 0.0;

        [self.viewController presentViewController:vc
                                          animated:NO
                                        completion:^{
            [vc dismissViewControllerAnimated:NO completion:nil];
        }];
    });
}

@end
