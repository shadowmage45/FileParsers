import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class WorkOrderParser 
{

private int freq;
private File serverFolder;
private File archiveFolder;
private File outputFolder;

private List<File> filesToCopy = new ArrayList<File>();
private List<File> filesToDelete = new ArrayList<File>();
private List<File> filesToParse = new ArrayList<File>();
private List<File> filesToPrint = new ArrayList<File>();

public static void main(String... aArgs)
  {
  int freq = 1;
  String path = "";
  if(aArgs==null || aArgs.length==0)
    {
    System.out.println("No arguments detected for print program.");
    System.out.println("Running in static mode (static set start params)");
    freq = 20;
    path = "//192.168.17.107/Print_WO/";      
    }
  else if(aArgs.length==2)
    {
    if(aArgs[1].startsWith("\"")){aArgs[1]=aArgs[1].substring(1, aArgs[1].length());}
    if(aArgs[1].endsWith("\"")){aArgs[1]=aArgs[1].substring(0, aArgs[1].length()-1);}
    try{freq = Integer.valueOf(aArgs[0]);}
    catch(NumberFormatException e){freq=2;}
    path = aArgs[1];
    }
  else
    {
    System.out.println("The program must be launched with 0 arguments (static setup), or two arguments (frequency, path_to_monitor)");
    return;
    }
  WorkOrderParser parser = new WorkOrderParser(freq, path);
  parser.startMonitoring();
  }

public WorkOrderParser(int frequency, String pathToMonitor)
  {
  this.freq = frequency;
  serverFolder = new File(pathToMonitor);
  if(!serverFolder.exists())
    {
    throw new RuntimeException("Folder to read from does not exist or cannot be reached: "+pathToMonitor);
    }
  archiveFolder = new File("archive");
  archiveFolder.mkdirs();
  outputFolder = new File("output");
  outputFolder.mkdirs();  
  System.out.println("Setting up Work Order parsing to monitor directory: "+serverFolder.getAbsolutePath());
  }

public void startMonitoring()
  {
  System.out.println("Beginning directory monitoring.");
  while(true)
    {
    filesToPrint.clear();
    filesToParse.clear();
    filesToCopy.clear();
    filesToDelete.clear();
    try
      {
      Thread.sleep(freq * 1000);
      }
    catch (InterruptedException e){}
    if(copyFiles())
      {
      deleteFiles();
      parseFiles();
      printFiles();
      deletePrintedFiles();
      System.out.println("Processing finished, resuming directory monitoring.");
      }
    }
  }

/** 
 * @return true if there were any files copied
 */
private boolean copyFiles()
  {
  File[] files = serverFolder.listFiles();
  File f1;
  for(File f : files)
    {
    if(!f.isDirectory() && f.isFile())
      {
      f1 = new File(archiveFolder, f.getName());
      System.out.println("Copying "+f.getAbsolutePath()+" to "+f1.getAbsolutePath());
      try
        {
        Files.copy(f.toPath(), f1.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      catch (IOException e)
        {        
        e.printStackTrace();
        continue;
        }
      System.out.println("Successfully copied file.");
      filesToDelete.add(f);
      filesToParse.add(f1);
      }
    }
  return !filesToDelete.isEmpty();
  }

private void deleteFiles()
  {
  while(!filesToDelete.isEmpty())
    {
    Iterator<File> it = filesToDelete.iterator();
    File f;
    while(it.hasNext() && (f=it.next())!=null)
      {
      System.out.println("Deleting "+f.getAbsolutePath()+" from server.");
      try
        {
        Files.delete(f.toPath());
        }
      catch (IOException e)
        {
        continue;
        }
      it.remove();
      System.out.println("Successfully deleted file");
      }
    }
  }

private void parseFiles()
  {
  Iterator<File> it = filesToParse.iterator();
  File f;
  File out;
  while(it.hasNext() && (f=it.next())!=null)
    {      
    out = new File(outputFolder, f.getName()+".rtf");
    System.out.println("Parsing file: "+f.getAbsolutePath()+" into: "+out.getAbsolutePath());
    parseFile(f, out);
    filesToPrint.add(out);
    it.remove();
    }
  }

private void printFiles()
  {
  while(!filesToPrint.isEmpty())
    {
    Iterator<File> it = filesToPrint.iterator();
    File f;
    while(it.hasNext() && (f=it.next())!=null)
      {      
      System.out.println("Printing file: "+f.getAbsolutePath());
      try
        {
        printFile(f);
//        Runtime.getRuntime().exec("cmd /c auto-print.bat "+f.getName());
        }
      catch (IOException e)
        {
        e.printStackTrace();
        continue;
        }
      try
        {
        Thread.sleep(10 * 1000);
        }
      catch (InterruptedException e){e.printStackTrace();}
      System.out.println("Successfully printed file.");
      filesToDelete.add(f);
      it.remove();
      }
    } 
  }

private void deletePrintedFiles()
  {
  while(!filesToDelete.isEmpty())
    {
    Iterator<File> it = filesToDelete.iterator();
    File f;
    while(it.hasNext() && (f=it.next())!=null)
      {
      System.out.println("Deleting "+f.getAbsolutePath()+" from to-print cache.");
      try
        {
        Files.copy(f.toPath(), new File(archiveFolder, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(f.toPath());
        }
      catch (IOException e)
        {
        continue;
        }
      it.remove();
      System.out.println("Successfully deleted file.");
      }
    }
  }

private void printFile(File f) throws IOException
  {
  ProcessBuilder pb = new ProcessBuilder("C:\\Program Files\\Windows NT\\Accessories\\wordpad.exe", "/p", outputFolder.getAbsolutePath()+"\\"+f.getName());  
  Process p = pb.start();
  try
    {
    p.waitFor();
    }
  catch (InterruptedException e)
    {
    e.printStackTrace();
    }
  }


///**
// * infinite directory monitor loop
// * @param freq
// * @param path
// */
//private void setupMonitor(int freq, String path)
//  {
//  this.freq = freq;
//  this.path = path;    
//  File dir = new File(path);
//  System.out.println("Seting up directory monitor for: "+dir.getAbsolutePath());
//  while(true)
//    {
//    try
//      {
//      Thread.sleep(freq * 1000);
//      //this.wait(freq*1000);
//      } 
//    catch (InterruptedException e)
//      {      
//      e.printStackTrace();
//      System.out.println("interrupted Sleep!!!");
//      //break;
//      }
//    File [] files = dir.listFiles();
//    if(files==null)
//      {
//      continue;
//      }
//    for(int i = 0; i < files.length ; i++)
//      {
//      File file = files[i];
//      if(file==null || file.isDirectory())
//        {
//        continue;
//        }
//      System.out.println("Attempting to process: "+file.getName());
////      if(!parseFile(file))
////        {        
////        continue;
////        }
//      
//      System.out.println("Processing successful.");      
//      
//      try
//        {
//        System.out.println("Attempting to print: "+file.getName());
//        Runtime.getRuntime().exec("cmd /c auto-print.bat "+file.getName());
//        System.out.println("Sucessfully sent to printer.");
//        System.out.println("Sleeping to wait for print job to clear.");
//        Thread.sleep(10*1000);
//        } 
//      catch (IOException e)
//        {
//        System.out.println("SEVERE ERROR PRINTING FILE VIA BATCH");
//        e.printStackTrace();
//        continue;
//        } 
//      catch (InterruptedException e)
//        {
//        System.out.println("INTERRUPTED WHILE SLEEPING AFTER PRINT-JOB");
//        e.printStackTrace();
//        continue;
//        }
//      
//      
//      System.out.println("Copying: "+file.getName()+" to /archive");
//      copyFileTo(file, "archive", getFileTS());
//      
//      /**
//       * delete the origin file, since we have already copied it into archive
//       */
//      try
//        {
//        System.out.println("Cleaning up:  Attempting to delete: "+file.getAbsolutePath());
//        Files.delete(file.toPath());
//        System.out.println("File deleted, resuming directory monitoring.");
//        } 
//      catch (IOException e)
//        {
//        e.printStackTrace();
//        }
//      }
//    }
//  }

//private String getFileTS()
//  {
//  Calendar cal = Calendar.getInstance();
//  return cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"-"+cal.get(Calendar.YEAR)+ "--"+cal.get(Calendar.HOUR_OF_DAY)+"-"+cal.get(Calendar.MINUTE)+"-"+cal.get(Calendar.SECOND);
//  }
//
//private void copyFileTo(File file, String path, String name)
//  {
//  File out = new File(path);
//  out.mkdirs();
//  File outFile = new File(path, name);  
//  
//  InputStream reader =null ;
//  OutputStream output=null;
//  try
//    {
//    reader = new FileInputStream(file);
//    output = new FileOutputStream(outFile);
//    byte[] readBuffer = new byte[1024];
//    int length;
//    while((length = reader.read(readBuffer))>0)
//      {
//      output.write(readBuffer,0,length);
//      }
//    output.close();
//    reader.close();
//    } 
//  catch (FileNotFoundException e)
//    {    
//    e.printStackTrace();
//    } 
//  catch (IOException e)
//    {
//    e.printStackTrace();
//    }
//  
//  }

private boolean parseFile(File toParse, File destination)
  {
  /**
   * A list to temporarily store lines in for parsing
   */
  LinkedList<String> lines = new LinkedList<String>(); 


  /**
   * load and open both input and output files
   */
  File outFile = destination;
  File inFile = toParse;
  
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
    writer = new FileWriter(outFile);
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
