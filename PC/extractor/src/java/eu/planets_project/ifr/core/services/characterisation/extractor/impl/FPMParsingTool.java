package eu.planets_project.ifr.core.services.characterisation.extractor.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import eu.planets_project.services.datatypes.FileFormatProperties;
import eu.planets_project.services.datatypes.FileFormatProperty;
import eu.planets_project.services.datatypes.Metric;
import eu.planets_project.services.datatypes.Metrics;
import eu.planets_project.services.utils.ByteArrayHelper;
import eu.planets_project.services.utils.ProcessRunner;

/** Small tool that reads .fpm files generated by the FPMTool and skips "unrelevant" information.
 *  Produces xml files containing format properties for a given file format... 
 * @author melmsp
 *
 */

public class FPMParsingTool {
	
	public static String FPMTOOL_HOME = System.getenv("FPMTOOL_HOME") + File.separator;
	public static String FPM_TOOL = FPMTOOL_HOME + "fpmTool";
	public static BufferedWriter out;
	
	
	public static boolean generatePropertiesFile (String pronomID) {
		try {
			out = new BufferedWriter(new FileWriter(new File("PC/extractor/src/resources/fpm_files/" + "parsingOutputLog.txt"), true));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean success = false;
		ProcessRunner shell = new ProcessRunner();
		
		shell.setStartingDir(new File(FPMTOOL_HOME));
		
		List<String> shellCommands = new ArrayList <String>();
		shellCommands.add(FPM_TOOL);
		shellCommands.add(pronomID);
		
		shell.setCommand(shellCommands);
		shell.run();
		
		String processOutput = shell.getProcessOutputAsString();
		String processError = shell.getProcessErrorAsString();
		
		System.out.println("Process output: " + processOutput);
		try {
			out.append("Process output: " + processOutput);
			out.newLine();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.err.println("Process error: " + processError);
		
		File resultFPM = new File(FPMTOOL_HOME + "fpm.fpm");
		
		byte[] resultFPMArray = ByteArrayHelper.read(resultFPM);
		
		String fileNamePronomID = pronomID.substring(0, pronomID.indexOf(":")).replace("/", "_"); 
		
		String newOutputFilePath = "PC/extractor/src/resources/fpm_files/" + fileNamePronomID + ".temp";  
		
		File fpmTempFile = ByteArrayHelper.writeToDestFile(resultFPMArray, newOutputFilePath);
		File formattedFPM = new File("PC/extractor/src/resources/fpm_files/" + fileNamePronomID + ".fpm");
		
		SAXBuilder saxBuilder = new SAXBuilder();
		XMLOutputter xmlOut = new XMLOutputter();
		BufferedWriter xmlWriter;
		String deletedFile;
		
		Document orgDoc;
		try {
//			List <FileFormatProperty> fileFormatProperties = new ArrayList<FileFormatProperty>();
			FileFormatProperties fileFormatProperties = new FileFormatProperties();
			
			orgDoc = saxBuilder.build(fpmTempFile);
		
			Element orgRoot = orgDoc.getRootElement();
			
			List <Element> content = orgRoot.getChildren();
			
			FileFormatProperty formatProperty = null;
//			List <Metric> metrics = null;
			Metrics metrics = null;
			
			Element format = orgRoot.getChild("format");
			
			String puid = format.getAttributeValue("puid");
			System.out.println("Format: " + puid);
			System.out.println("***********************************************************");
			out.append("Format: " + puid);
			out.newLine();
			out.append("***********************************************************");
			out.newLine();
			
			
			
			for (Element element : content) {
				List <Element> level1Elements = element.getChildren();
				
				for (Element propertyTopLevel : level1Elements) {
					List <Element> level2Elements = propertyTopLevel.getChildren();
					formatProperty = new FileFormatProperty();
					
					for (Element property : level2Elements) {
						
						if(property.getName().equalsIgnoreCase("id")) {
							formatProperty.setId(property.getTextTrim());
							continue;
						}
						
						if(property.getName().equalsIgnoreCase("name")) {
							formatProperty.setName(property.getTextTrim());
							continue;
						}
						
						if(property.getName().equalsIgnoreCase("description")) {
							formatProperty.setDescription(property.getTextTrim());
							continue;
						}
						
						if(property.getName().equalsIgnoreCase("unit")) {
							formatProperty.setUnit(property.getTextTrim());
							continue;
						}
						
						if(property.getName().equalsIgnoreCase("type")) {
							formatProperty.setType(property.getTextTrim());
							continue;
						}
						
						if(property.getName().equalsIgnoreCase("metrics")) {
							metrics = new Metrics();
							List <Element> level3Elements = property.getChildren();
							
							for (Element m : level3Elements) {
								List <Element> level4Elements = m.getChildren();
								Metric pMetric = new Metric();
								
								for (Element metric : level4Elements) {
									if(metric.getName().equalsIgnoreCase("mId")) {
										pMetric.setId(metric.getTextTrim());
										continue;
									}
									
									if(metric.getName().equalsIgnoreCase("mName")) {
										pMetric.setName(metric.getTextTrim());
										continue;
									}
									
									if(metric.getName().equalsIgnoreCase("mDescription")) {
										pMetric.setDescription(metric.getTextTrim());
										continue;
									}
								}
								metrics.add(pMetric);
							}
						}
					}
					
					formatProperty.setMetrics(metrics);
					fileFormatProperties.add(formatProperty);
					metrics = null;
				}
			}
			Element xclProperties = new Element("XCLProperties");
			xclProperties.setAttribute("formatPUID", puid);
			
			Document newDoc = new Document(xclProperties);
			
			Element newRoot = newDoc.getRootElement();
			
			for (FileFormatProperty ffproperty : fileFormatProperties.getProperties()) {
				Element new_property_top = new Element("property");
				
				Element name = new Element("name").setText(ffproperty.getName());
				Element id = new Element("id").setText(ffproperty.getId());
				Element description = new Element("description").setText(ffproperty.getDescription());
				Element unit = new Element("unit").setText(ffproperty.getUnit());
				Element type = new Element("type").setText(ffproperty.getType());
				Element new_metrics_top = new Element("metrics");
				
				for (Metric metric : ffproperty.getMetrics().getList()) {
					Element mName = new Element("mName").setText(metric.getName());
					new_metrics_top.addContent(mName);
					Element mId = new Element("mId").setText(metric.getId());
					new_metrics_top.addContent(mId);
					Element mDescription = new Element("mDescription").setText(metric.getDescription());
					new_metrics_top.addContent(mDescription);
				}
				
				new_property_top.addContent(name);
				new_property_top.addContent(id);
				new_property_top.addContent(description);
				new_property_top.addContent(unit);
				new_property_top.addContent(type);
				new_property_top.addContent(new_metrics_top);
				
				newRoot.addContent(new_property_top);
				
				xmlWriter = new BufferedWriter(new FileWriter(formattedFPM));
				xmlOut.output(newDoc, xmlWriter);
				
				for (Iterator iterator = fileFormatProperties.getProperties().iterator(); iterator.hasNext();) {
					FileFormatProperty testOutProp = (FileFormatProperty) iterator.next();
					System.out.println(testOutProp.toString());
					out.append(testOutProp.toString());
					out.newLine();
				}
			}
			
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean deleted = fpmTempFile.delete();
		success = true;
		System.out.println("***********************************************************");
		try {
			out.append("***********************************************************");
			out.newLine();
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}
}