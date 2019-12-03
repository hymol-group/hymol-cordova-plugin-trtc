#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>
#import <TXLiteAVSDK_TRTC/TRTCCloudDef.h>
#import "TRTCMainViewController.h"
#import "GenerateTestUserSig.h"

@interface HymolTrtc : CDVPlugin {
  // 视频结束时的回调
  NSString *onFinishedCallbackId;
  TRTCMainViewController *controller;
}

- (void)enterRoom:(CDVInvokedUrlCommand*)command;
- (void)hangUp:(CDVInvokedUrlCommand*)command;

@end

@implementation HymolTrtc

// 开始视频
- (void)enterRoom:(CDVInvokedUrlCommand*)command 
{
    NSDictionary *data = [command.arguments objectAtIndex:0];
    if (data == nil) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"数据有误"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    onFinishedCallbackId = command.callbackId;

    // TRTC相关参数设置
    TRTCParams *param = [[TRTCParams alloc] init];
    param.sdkAppId = (UInt32)[(NSString*)[data objectForKey: @"sdkappid"] intValue];
    param.userId = (NSString*)[data objectForKey: @"userId"];
    param.roomId = (UInt32)[(NSString*)[data objectForKey: @"roomId"] intValue];
    param.userSig = (NSString*)[data objectForKey: @"userSig"];
    param.privateMapKey = @"";
    param.role = TRTCRoleAnchor;
    
    // 控制器参数
    TRTCMainViewController *vc = [[TRTCMainViewController alloc] init];

    NSString *b64 = [data valueForKey:@"base64Image"];
    if (b64 == nil) {
        // 医师版
        vc.role = 0;
    } else {
        // 患者版, 传入医师信息
        vc.role = 1;
        vc.physicianImage = [self imageFromBase64:b64];
        vc.physicianName = [data valueForKey:@"physicianName"];
        vc.physicianRole = [data valueForKey:@"physicianRole"];
        vc.physicianDesc = [data valueForKey:@"physicianDesc"];
    }
    __weak HymolTrtc *w_self = self;
    vc.onHangUp = ^{
        [w_self onHangUp];
    };
    vc.param = param;
    // 视频通话场景
    vc.appScene = TRTCAppSceneVideoCall;

    controller = vc;
    [self.viewController presentViewController:vc animated:YES completion:nil];
}

-(void) onHangUp {
    if (self->onFinishedCallbackId) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"视频挂断"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:onFinishedCallbackId];
    }
}

// base64转UIImage
- (UIImage*) imageFromBase64: (NSString*)base64 {
    NSData *b64Data = [[NSData alloc]initWithBase64EncodedString:base64 options:0];
    UIImage *img = [UIImage imageWithData:b64Data];
    return img;
}

// 挂断
- (void)hangUp:(CDVInvokedUrlCommand*)command {
    [controller hangUp];
}

@end

