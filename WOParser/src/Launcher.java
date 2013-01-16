
public class Launcher
{

public static FileParseTest test;
public static boolean debug = true;

public static void main(String... aArgs)
  {
  if(test==null)
    {
    test= new FileParseTest();
    if(debug)
      {
      System.out.println("Running in debug mode (static set start params)");
      test.setupMonitor(2, "//192.168.17.107/Print_WO/");
      return;
      }
    if(aArgs.length>0)
      {
      test.doParseTest(aArgs[0]);
      }
    else
      {
      test.doParseTest("");
      }
    if(aArgs.length==2)
      {
      test.setupMonitor(Integer.valueOf(aArgs[0]), aArgs[1]);
      }
    }
  
  
  }



}
