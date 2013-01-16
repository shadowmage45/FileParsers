import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;


public class FileParseTest 
{

int freq;
String path;


/**
 * infinite directory monitor loop
 * @param freq
 * @param path
 */
public void setupMonitor(int freq, String path)
  {
  System.out.println("seting up directory monitor");
  while(true)
    {
    try
      {
      Thread.sleep(freq*1000);
      //this.wait(freq*1000);
      } 
    catch (InterruptedException e)
      {      
      e.printStackTrace();
      System.out.println("interrupted Sleep!!!");
      //break;
      }
    File dir = new File(path);
    File [] files = dir.listFiles();
    if(files==null)
      {
      continue;
      }
    for(int i = 0; i < files.length ; i++)
      {
      File file = files[i];
      if(file==null || file.isDirectory())
        {
        continue;
        }
      System.out.println("Attempting to process: "+file.getName());
      if(!doParseTest(file.getAbsolutePath()))
        {        
        continue;
        }
      
      System.out.println("Processing successful.");
      
      
      try
        {
        System.out.println("Attempting to print: "+file.getName());
        //Runtime.getRuntime().exec("cmd /c wordpad.exe /p output.rtf");
        Runtime.getRuntime().exec("cmd /c auto-print.bat "+file.getName());
        System.out.println("Sucessfully sent to printer.");
        System.out.println("Sleeping to wait for print job to clear.");
        Thread.sleep(10*1000);
        } 
      catch (IOException e)
        {
        System.out.println("SEVERE ERROR PRINTING FILE VIA BATCH");
        e.printStackTrace();
        continue;
        } 
      catch (InterruptedException e)
        {
        System.out.println("INTERRUPTED WHILE SLEEPING AFTER PRINT-JOB");
        e.printStackTrace();
        continue;
        }
      
      
      System.out.println("Copying: "+file.getName()+" to /archive");
      copyFileTo(file, file.getParent()+"/archive/"+getFileTS());
      
      /**
       * delete the origin file, since we have already copied it into archive
       */
      try
        {
        System.out.println("Cleaning up:  Attempting to delete: "+file.getPath());
        Files.delete(file.toPath());
        System.out.println("File deleted, resuming directory monitoring.");
        } 
      catch (IOException e)
        {
        e.printStackTrace();
        }
      }
    }
  }

public String getFileTS()
  {
  Calendar cal = Calendar.getInstance();
  return cal.get(cal.MONTH)+"-"+cal.get(cal.DAY_OF_MONTH)+"-"+cal.get(cal.YEAR)+ "--"+cal.get(cal.HOUR_OF_DAY)+"-"+cal.get(cal.MINUTE)+"-"+cal.get(cal.SECOND);
  }

public void copyFileTo(File file, String destination)
  {
  InputStream reader =null ;
  OutputStream output=null;
  try
    {
    reader = new FileInputStream(file);
    output = new FileOutputStream(new File(destination));
    byte[] readBuffer = new byte[1024];
    int length;
    while((length = reader.read(readBuffer))>0)
      {
      output.write(readBuffer,0,length);
      }
    output.close();
    reader.close();
    } 
  catch (FileNotFoundException e)
    {    
    e.printStackTrace();
    } 
  catch (IOException e)
    {
    e.printStackTrace();
    }
  
  }

public boolean doParseTest(String name)
  {
  /**
   * A list to temporarily store lines in for parsing
   */
  LinkedList<String> lines = new LinkedList<String>(); 
  /**
   * if file name was not passed in through args, default to in.txt
   */
  String fileName = "";
  if(name.equals(""))
    {
    fileName = "in.txt";
    }
  else
    {
    fileName = name;
    }
  /**
   * load and open both input and output files
   */
  File file = new File("output.rtf");
  File inFile = new File(fileName);
  
  /**
   * scanner instance, does the reading of fileStreams line by line
   */
  Scanner scan = null;
  /**
   * FileWriter instance, handles actually writing the file after the parsing
   */
  FileWriter writer = null;  
  
  /**
   * Exception handling...because file I/O relies on humans
   */  
  try
    {
    scan = new Scanner(inFile);//attempt to initialize scanner with input file
    } 
  catch (FileNotFoundException e)
    {  
    e.printStackTrace();
    }
  finally
    {
    if(scan==null)
      {
      return false;      //if file was not initialized, exit early.
      }    
    }  
  
  
  /**
   * more exception handling, to handle errors that may pop-up during stream reading/writing
   */
  try
    {
    /**
     * initialize fileWriter, and write header to the beggining of the document
     */
    writer = new FileWriter(file);
    writer.write("{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fmodern\\fprq1\\fcharset0 @MS Mincho;}{\\f1\\fnil\\fcharset0 fixedsys;}{\\f2\\fnil\\fprq2\\fcharset0 Free 3 of 9 Extended;}}");
    writer.write("\\viewkind4\\uc1\\pard\\lang1033\\f0\\fs20");
    
    /**
     * do actual parsing here
     */
    String line = "";
    boolean pageEnd = false;//keeps track of when a page break is found
    boolean doPageEnd = false;//if a page break was found last run, this appends page end character to the line
    
    /**
     * while the scanner has more lines to parse, keep parsing...
     */
    while(scan.hasNextLine())
      {
      /**
       * pull the next line out of the scanner into a local string for manipulation
       */
      line = scan.nextLine();      
      /**
       * check the line for presence of special characters for barcodes--currently a ^
       * if character is present, remove extra pre/post characters, and append * before and after the barcode
       * data. pre-append font-tag to line 
       */
      if(line.length()>0 && line.charAt(0)=='^')
        {
        int len = line.length();
        String newLine = "*";
        for(int i = 21; i < len-7; i++)
          {
          newLine = newLine + line.charAt(i);
          }
        newLine = newLine.toUpperCase();
        newLine = newLine +"*";
        newLine = "\\f2\\fs72 " + newLine + "\\f0\\fs20\\par\r\n";        
        //lines.pollLast();
        lines.add(newLine);
        }
      else if(line.startsWith("Page :"))///else if it is the end of page-line, set end of page found to true
        {
        line = line+("\\par\r\n");
        lines.add(line);  
        pageEnd = true;
        }
      else//else it is a normal line--not a page end or barcode, append linebreak to the line, and add it to the document
        {
        if(!doPageEnd)
          {
          line = line+("\\par\r\n");
          }        
        lines.add(line);                
        }
      if(doPageEnd)//if a page end was found, reset flags and append page-break to line
        {
        lines.add("\\pard\r\n");
        doPageEnd = false;
        }
      if(pageEnd)//because the page-break line is actually one line above the page-break, we use this variable to delay processing        
        {
        doPageEnd = true;
        pageEnd = false;
        }
      } 
    
    /**
     * after the entire document has been parsed....
     */
    Iterator<String> it = lines.iterator();
    /**
     * keep pulling lines from the parsed document
     */
    while(it.hasNext())
      {
      String tempLine = it.next();
      /**
       * and write each of those lines to file.
       */
      writer.write(tempLine);
      }
    /**
     * finally, append an end of document tag to the end of the document.
     */
    writer.write("}");
    } 
  catch (IOException e)//these try/catch sections will only run if there are errors regarding file loading, reading, or writing
    {
    e.printStackTrace();
    return false;
    }
  finally
    {
    /**
     * if writer was null...early exit
     */
    if(writer==null)//
      {
      return false;
      }
    }  
  
  /**
   * attempt to close files, clean up resources
   */
  try
    {
    writer.close();   
    } 
  catch (IOException e)
    {  
    e.printStackTrace();
    }
  finally
    {
    scan.close();
    }  
  return true;
  }


}
