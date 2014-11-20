import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class WorkOrderParser 
{

private int freq;
private File serverFolder;
private File archiveFolder;
private File outputFolder;

private Logger log;

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
  try
    {
    parser.startMonitoring();
    }
  catch(Exception e)
    {
    
    }  
  }

public WorkOrderParser(int frequency, String pathToMonitor)
  {
  this.freq = frequency;
  serverFolder = new File(pathToMonitor);   
  archiveFolder = new File("archive");
  archiveFolder.mkdirs();
  outputFolder = new File("output");
  outputFolder.mkdirs();  
  }

public void startMonitoring()
  {
  log = Logger.getLogger("com.croakies.woparser");
  FileHandler h = null;
  try
    {
    String date = String.valueOf(System.currentTimeMillis());
    h = new FileHandler("log--"+date+".txt");
    }
  catch (SecurityException e1)
    {
    e1.printStackTrace();
    }
  catch (IOException e1)
    {
    e1.printStackTrace();
    }
  if(h!=null)
    {
    log.addHandler(h);
    h.setFormatter(new SimpleFormatter());
    }
  
  log("Setting up Work Order parsing to monitor directory: "+serverFolder.getAbsolutePath());
  log("Beginning directory monitoring.");  
  log("Redirecting output to logger, please check log.txt for output.");  
  
  try
    {
    if(!serverFolder.exists())
      {
      throw new RuntimeException("Folder to read from does not exist or cannot be reached: "+serverFolder.getAbsolutePath());
      }
    
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
        log("Processing finished, resuming directory monitoring.");
        }
      }
    }
  catch(Exception e)
    {
    log.log(Level.SEVERE, "Caught exception from parsing thread", e);
    }
  }

private void log(String message)
  {
  log.log(Level.SEVERE, message);
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
      log("Copying "+f.getAbsolutePath()+" to "+f1.getAbsolutePath());
      try
        {
        Files.copy(f.toPath(), f1.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      catch (IOException e)
        {        
        log.log(Level.SEVERE, "Caught exception copying files...", e);
        e.printStackTrace();
        continue;
        }
      log("Successfully copied file.");
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
      log("Deleting "+f.getAbsolutePath()+" from server.");
      try
        {
        Files.delete(f.toPath());
        }
      catch (IOException e)
        {
        log.log(Level.SEVERE, "Caught exception deleting files...", e);
        continue;
        }
      it.remove();
      log("Successfully deleted file");
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
    log("Parsing file: "+f.getAbsolutePath()+" into: "+out.getAbsolutePath());
    parseFile2(f, out);
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
      log("Printing file: "+f.getAbsolutePath());
      try
        {
        printFile(f);
        }
      catch (IOException e)
        {
        e.printStackTrace();
        log.log(Level.SEVERE, "Caught exception while printing files", e);
        continue;
        }
      try
        {
        Thread.sleep(10 * 1000);
        }
      catch (InterruptedException e){e.printStackTrace();}
      log("Successfully printed file.");
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
      log("Deleting "+f.getAbsolutePath()+" from to-print cache.");
      try
        {
        Files.copy(f.toPath(), new File(archiveFolder, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(f.toPath());
        }
      catch (IOException e)
        {
        log.log(Level.SEVERE, "Caught exception deleting printed files...", e);
        continue;
        }
      it.remove();
      log("Successfully deleted file.");
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

private List<String> getFileLines(File toParse)
  {
  ArrayList<String> lines = new ArrayList<String>();
  try
    {
    Scanner scan = new Scanner(toParse);
    while(scan.hasNextLine())
      {
      lines.add(scan.nextLine());
      }
    scan.close();
    }
  catch (FileNotFoundException e)
    {
    e.printStackTrace();
    }
  return lines;
  }

private List<String> parseFileLines(List<String> lines)
  {
  List<String> outLines = new ArrayList<String>();
  outLines.add("{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fmodern\\fprq1\\fcharset0 @MS Mincho;}{\\f1\\fnil\\fcharset0 fixedsys;}{\\f2\\fnil\\fprq2\\fcharset0 Free 3 of 9 Extended;}}");
  outLines.add("\\viewkind4\\uc1\\pard\\lang1033\\f0\\fs20");
  
  boolean pageEnd = false;//keeps track of when a page break is found
  boolean doPageEnd = false;//if a page break was found last run, this appends page end character to the line
  String line;
  Iterator<String> it = lines.iterator();
  while(it.hasNext() && (line=it.next())!=null)
    {
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
      outLines.add(newLine);
      }
    else if(line.startsWith("Page :"))///else if it is the end of page-line, set end of page found to true
      {
      line = line+("\\par\r\n");
      outLines.add(line);  
      pageEnd = true;
      }
    else//else it is a normal line--not a page end or barcode, append linebreak to the line, and add it to the document
      {
      if(!doPageEnd)
        {
        line = line+("\\par\r\n");
        }        
      outLines.add(line);                
      }
    if(doPageEnd)//if a page end was found, reset flags and append page-break to line
      {
      outLines.add("\\pard\r\n");
      doPageEnd = false;
      }
    if(pageEnd)//because the page-break line is actually one line above the page-break, we use this variable to delay processing        
      {
      doPageEnd = true;
      pageEnd = false;
      }    
    } 
  outLines.add("}");
  return outLines;
  }

private void writeFile(List<String> lines, File destination)
  {
  /**
   * FileWriter instance, handles actually writing the file after the parsing
   */
  try
    {
    FileWriter writer = new FileWriter(destination);
    for(String line : lines)
      {
      writer.write(line+"\n");      
      }
    writer.close();
    }
  catch (IOException e)
    {
    log.log(Level.SEVERE, "Caught exception writing files...", e);
    e.printStackTrace();
    }
  }

private void parseFile2(File toParse, File destination)
  {
  List<String> lines = getFileLines(toParse);
  List<String> outLines = parseFileLines(lines);
  writeFile(outLines, destination);
  }


}
