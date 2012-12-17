
public class Launcher
{

public static FileParseTest test;

public static void main(String... aArgs)
  {
  if(test==null)
    {
    test= new FileParseTest();
    if(aArgs.length>0)
      {
      test.doParseTest(aArgs[0]);
      }
    else
      {
      test.doParseTest("");
      }    
    }
  }



}
