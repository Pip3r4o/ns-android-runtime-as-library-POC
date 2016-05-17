(function() {
    // bootstrap modules
    require("application");

    try {
     console.log("from console.log()");
    } catch (e) {
        __log("crashed during console.log");
    }

     var result = require("simplemodule").myLog("12");

     console.log("result from myLog -> " + result);

     var five = new java.lang.Integer(5);

     return five;
})();