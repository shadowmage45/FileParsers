import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * @author Shadowmage
 *
 */
public class MaterialsParser 
{

public class Page
{
String itemID = "";
public LinkedList<String> pageLines = new LinkedList<String>();
}

public class Document
{
public LinkedList<Page> pages = new LinkedList<Page>();
public LinkedList<ItemEntry> items = new LinkedList<ItemEntry>();
}

public class ItemEntry
{
public String itemID = "";
public LinkedList<Page> pages = new LinkedList<Page>();
}

public class ItemsFilter
{
public LinkedList<String> validItems = new LinkedList<String>();
}

private static MaterialsParser INSTANCE;
private MaterialsParser(){}
public static MaterialsParser instance()
  {
  if(INSTANCE==null)
    {
    INSTANCE = new MaterialsParser();
    }
  return INSTANCE;
  }

public static void main(String... aArgs)
  {
  if(aArgs.length>0)
    {
    instance().parseFile(aArgs[0]);
    }
  else
    {
    instance().parseFile("in.txt");
    }
  }

public void parseFile(String fileName)
  {  
  /**
   * A filter list...
   */
  ItemsFilter filter = new ItemsFilter();
  
  /**
   * A list to temporarily store lines in for parsing
   */
  LinkedList<String> lines = new LinkedList<String>(); 
      
  /**
   * load and open both input and output files
   */
  File file = new File("output.txt");
  File inFile = new File(fileName);
  File filterFile = new File("filter.txt");
  
  /**
   * scanner instance, does the reading of fileStreams line by line
   */
  Scanner scan = null;
  Scanner filterScan = null;
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
   * if filter file is not present, set hasFilter flag to false
   */
  boolean hasFilter = true;
  try
    {
    filterScan = new Scanner(filterFile);
    while(filterScan.hasNextLine())
      {
      filter.validItems.add(String.valueOf(filterScan.nextLine()));
      }
    }
  catch(FileNotFoundException e)
    {
    hasFilter = false;
    }
 
  
  Document doc = new Document();  
  String line = "";
  String prevItemName = "";  
  
  while(scan.hasNextLine())
    {
    /**
     * scan initial line
     */
    line = scan.nextLine();
    /**
     * if line contains page header
     */
    if(line.length()>=10 && line.substring(0, 10).trim().equals("CROAKIES"))//start new page
      {
      doc.pages.add(new Page());
      }
    else if(line.length()>=13 && line.substring(0, 13).equals("Item Number: "))
      {      
      if(line.length()>=44)
        {
        String itemName = line.substring(13,44);
        itemName.trim();
        doc.pages.getLast().itemID = itemName;
        if(!prevItemName.equals(itemName))
          {
          doc.items.add(new ItemEntry());
          doc.items.getLast().itemID = itemName;          
          }
        doc.items.getLast().pages.add(doc.pages.getLast());       
        prevItemName = itemName;
        }
      }
    doc.pages.getLast().pageLines.add(String.valueOf(line));    
    }
    
  System.out.println("first entry Item: "+doc.items.getFirst().itemID);
  System.out.println("page count: "+doc.pages.size());
  System.out.println("total entry count: "+doc.items.size());
 

  }








}
