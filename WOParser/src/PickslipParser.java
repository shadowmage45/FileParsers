import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class PickslipParser
{

private static PickslipParser INSTANCE;
private PickslipParser(){}
public static PickslipParser instance()
  {
  if(INSTANCE==null)
    {
    INSTANCE = new PickslipParser();
    }
  return INSTANCE;
  }

public static void main(String... aArgs)
  {
  String fileName;
  if(aArgs.length>0)
    {
    fileName = aArgs[0];
    }
  else
    {
    fileName = "in.txt";
    }
  File inFile = new File(fileName);
  if(inFile!=null && inFile.exists())
    {
    instance().parseFile(inFile);
    }
  else
    {
    System.out.println("Error encountered while loading file, no such file found: "+fileName);
    }
  }

public void parseFile(File file)
  {  
//  FileWriter fw;
//  try
//    {
//    fw = new FileWriter("LPT1"); 
//    BufferedWriter bw = new BufferedWriter(fw);
//    bw.write("~ph");
//    bw.close();
//    System.out.println("testing direct talk..");
//    } 
//  catch (IOException e1)
//    {
//    // TODO Auto-generated catch block
//    e1.printStackTrace();
//    }
 
  Scanner scan = null;
  try
    {
    scan = new Scanner(file);
    } 
  catch (FileNotFoundException e)
    {
    e.printStackTrace();
    return;
    }  
  List<String> pageLines = new ArrayList<String>();
  String line = null;  
  docLines.add("{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fmodern\\fprq1\\fs16\\fcharset0 @MS Mincho;}{\\f1\\fnil\\fcharset0 fixedsys;}{\\f2\\fnil\\fprq2\\fcharset0 Free 3 of 9 Extended;}}");
  docLines.add("\\viewkind4\\uc1\\pard\\lang1033\\f0\\fs18");
  while(scan.hasNextLine())
    {
    line = scan.nextLine();
//    System.out.println(line);
    if(line.length()>1)
      {
      if(line.charAt(0) == '')// page break char found
        {
        line = line.substring(1);
        pageLines.add("\\pard \\insrsid \\page \\par");
//        pageLines.add("\\pard");
        System.out.println("found break char");
        this.parsePage(pageLines);
        pageLines.clear();
        }
      } 
//    System.out.println("adding line: "+line);
    pageLines.add(line);
    }
  this.parsePage(pageLines);
  docLines.add("}");

  scan.close();
  writeOut();
  }

protected void writeOut()
  {
  try
    {
    FileWriter writer = new FileWriter(new File("output.rtf"));
    
    for(String line : docLines)
      {
      writer.write(line + "\r\n");
      }
    writer.close();
    } 
  catch (IOException e)
    {
    e.printStackTrace();
    }
  }

List<String> docLines = new ArrayList<String>();

protected void parsePage(List<String> lines)
  {
  System.out.println("parsing page...");
  Iterator<String> it = lines.iterator();
  String line;
  String toAdd;
  while(it.hasNext())
    {
    line = it.next();    
    if(line.length()>1 && (line.charAt(0)=='^' || line.charAt(1)=='^'))
      {
      int start = line.charAt(0)=='^' ? 21 : 22;
      int len = line.length();
      String newLine = "*";
      for(int i = start; i < len-7; i++)
        {
        newLine = newLine + line.charAt(i);
        }
      newLine = newLine.toUpperCase();
      newLine = newLine +"*";
      newLine = "\\f2\\fs72 " + newLine + "\\f0\\fs18\\par\r\n";        
      //lines.pollLast();
      System.out.println("adding checked barcode line to file : "+newLine);
      toAdd = newLine;
      }
    else
      {
      if(!it.hasNext())
        {
        line = line + "\\pard";
        }
      else
        {
        line = line + "\\par";
        }
      toAdd = line;      
      }    
    docLines.add(toAdd);
    }
  }

}
