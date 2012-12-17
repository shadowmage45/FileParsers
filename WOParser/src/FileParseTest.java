import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;


public class FileParseTest 
{

public void doParseTest(String name)
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
      return;      //if file was not initialized, exit early.
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
    }
  finally
    {
    /**
     * if writer was null...early exit
     */
    if(writer==null)//
      {
      return;
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
  }


}
