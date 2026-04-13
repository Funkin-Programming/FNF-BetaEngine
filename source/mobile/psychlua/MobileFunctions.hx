package mobile.psychlua;

import lime.ui.Haptic;
import psychlua.FunkinLua;

class MobileFunctions
{
	public static function implement(funk:FunkinLua)
	{
		#if LUA_ALLOWED
		var lua:State = funk.lua;

		#if mobile
		Lua_helper.add_callback(lua, "haptic", function(duration:Int, ?period:Int)
		{
			return Haptic.vibrate(period, duration);
		});

		Lua_helper.add_callback(lua, "touchUtilJustPressed", TouchUtil.justPressed);
		Lua_helper.add_callback(lua, "touchUtilPressed", TouchUtil.pressed);
		Lua_helper.add_callback(lua, "touchUtilJustReleased", TouchUtil.justReleased);
		#end
		#end
	}
}