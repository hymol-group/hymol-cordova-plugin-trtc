var exec = require('cordova/exec');

exports.enterRoom = function (arg0, success, error) {
    exec(success, error, 'HymolTrtc', 'enterRoom', [arg0]);
};

exports.hangUp = function (arg0, success, error) {
    exec(success, error, 'HymolTrtc', 'hangUp', [arg0]);
};