var exec = require('cordova/exec');

exports.initializeSystem = function (arg0, success, error) {
    exec(success, error, 'AisinoPrinterPlugin', 'initializeSystem', [arg0]);
};

exports.printReceipt = function (arg0, success, error) {
    exec(success, error, 'AisinoPrinterPlugin', 'printReceipt', [arg0]);
};

exports.printSummary = function (arg0, success, error) {
    exec(success, error, 'AisinoPrinterPlugin', 'printSummary', arg0);
};
