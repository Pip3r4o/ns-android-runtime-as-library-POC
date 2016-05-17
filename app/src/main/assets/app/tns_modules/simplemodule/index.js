var summer = require('../simplesubmodule')

exports.myLog = function(s)
{
    console.log("Inside tns_modules/simplemodule");

	return s + summer.add(5, 42);
}