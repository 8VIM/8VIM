package inc.flide.logging;

import android.util.Log;

public class Logger implements LoggingConstants
{
    private static final String tag = LoggingConstants.Project_Name;

    public static void d(Object caller, String message)
    { Debug(caller, message); }
    public static void Debug(Object caller, String message)
    {
        if(LoggingConstants.DEBUG == true)
            Log.d(tag, caller.getClass().getName() + " : " + message);
    }

    public static void d(Object caller, String message, Throwable tr)
    { Debug(caller, message, tr); }
    public static void Debug(Object caller, String message, Throwable tr)
    {
        if(LoggingConstants.DEBUG == true)
            Log.d(tag, caller.getClass().getName() + " : " + message, tr);
    }



    public static void e(Object caller, String message)
    { Error(caller, message); }
    public static void Error(Object caller, String message)
    {
        if(LoggingConstants.ERROR == true)
            Log.e(tag, caller.getClass().getName() + " : " + message);
    }

    public static void e(Object caller, String message, Throwable tr)
    { Error(caller, message, tr); }
    public static void Error(Object caller, String message, Throwable tr)
    {
        if(LoggingConstants.ERROR == true)
            Log.e(tag, caller.getClass().getName() + " : " + message, tr);
    }



    public static void i(Object caller, String message)
    { Info(caller, message); }
    public static void Info(Object caller, String message)
    {
        if(LoggingConstants.INFO == true)
            Log.i(tag, caller.getClass().getName() + " : " + message);
    }

    public static void i(Object caller, String message, Throwable tr)
    { Info(caller, message, tr); }
    public static void Info(Object caller, String message, Throwable tr)
    {
        if(LoggingConstants.INFO == true)
            Log.i(tag, caller.getClass().getName() + " : " + message, tr);
    }



    public static void v(Object caller, String message)
    { Verbose(caller, message); }
    public static void Verbose(Object caller, String message)
    {
        if(LoggingConstants.VERBOSE == true)
            Log.v(tag, caller.getClass().getName() + " : " + message);
    }

    public static void v(Object caller, String message, Throwable tr)
    { Verbose(caller, message, tr); }
    public static void Verbose(Object caller, String message, Throwable tr)
    {
        if(LoggingConstants.VERBOSE == true)
            Log.v(tag, caller.getClass().getName() + " : " + message, tr);
    }



    public static void w(Throwable tr)
    { Warn(tr); }
    public static void Warn(Throwable tr)
    {
        if(LoggingConstants.WARN == true)
            Log.w(tag, tr);
    }

    public static void w(Object caller, String message)
    { Warn(caller, message); }
    public static void Warn(Object caller, String message)
    {
        if(LoggingConstants.WARN == true)
            Log.w(tag, caller.getClass().getName() + " : " + message);
    }

    public static void w(Object caller, String message, Throwable tr)
    { Warn(caller, message, tr); }
    public static void Warn(Object caller, String message, Throwable tr)
    {
        if(LoggingConstants.WARN == true)
            Log.w(tag, caller.getClass().getName() + " : " + message, tr);
    }

}
