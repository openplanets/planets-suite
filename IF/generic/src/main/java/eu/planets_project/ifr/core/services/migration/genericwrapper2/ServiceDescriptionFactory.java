package eu.planets_project.ifr.core.services.migration.genericwrapper2;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.planets_project.ifr.core.services.migration.genericwrapper2.exceptions.ConfigurationException;
import eu.planets_project.ifr.core.services.migration.genericwrapper2.exceptions.MigrationPathConfigException;
import eu.planets_project.services.datatypes.Parameter;
import eu.planets_project.services.datatypes.Property;
import eu.planets_project.services.datatypes.ServiceDescription;
import eu.planets_project.services.datatypes.Tool;
import eu.planets_project.services.datatypes.Property.Builder;

/**
 * @author Pelle Kofod &lt;pko@statsbiblioteket.dk&gt;
 * @author Thomas Skou Hansen &lt;tsh@statsbiblioteket.dk&gt;
 */
class ServiceDescriptionFactory {
    private Logger log = Logger.getLogger(ServiceDescriptionFactory.class
	    .getName());

    private final XPathFactory xPathFactory;

    private final Document configuration;
    private final String canonicalServiceName;

    ServiceDescriptionFactory(String canonicalServiceName,
	    Document wrapperConfiguration) {
	xPathFactory = XPathFactory.newInstance();
	configuration = wrapperConfiguration;
	this.canonicalServiceName = canonicalServiceName;
    }

    ServiceDescription getServiceDescription() throws ConfigurationException,
	    MigrationPathConfigException {

	try {

	    final XPath pathsXPath = xPathFactory.newXPath();
	    final Node serviceDescriptionNode = (Node) pathsXPath.evaluate(
		    "//serviceWrapping/serviceDescription", configuration,
		    XPathConstants.NODE);

	    NodeList serviceDescriptionElements = configuration
		    .getElementsByTagName(Constants.SERVICE_DESCRIPTION)
		    .item(0).getChildNodes();

	    String title = null, description = null, version = null, creator = null, publisher = null, identifier = null, instructions = null, furtherinfo = null, logo = null;

	    Tool toolDescription = null;

	    for (int nodeIndex = 0; nodeIndex < serviceDescriptionElements
		    .getLength(); nodeIndex++) {
		final Node currentNode = serviceDescriptionElements
			.item(nodeIndex);
		if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
		    if (currentNode.getNodeName().equals(Constants.TITLE)) {
			title = currentNode.getTextContent().trim();
		    }/*
		      * FIXME! KILL else if (currentNode.getNodeName().equals(
		      * Constants.DESCRIPTION)) { description =
		      * currentNode.getTextContent().trim(); }
		      */else if (currentNode.getNodeName().equals(
			    Constants.TOOL)) {
			toolDescription = parseTool(currentNode);
		    } else if (currentNode.getNodeName().equals(
			    Constants.VERSION)) {
			version = currentNode.getTextContent().trim();
		    } else if (currentNode.getNodeName().equals(
			    Constants.CREATOR)) {
			creator = currentNode.getTextContent().trim();
		    } else if (currentNode.getNodeName().equals(
			    Constants.IDENTIFIER)) {
			identifier = currentNode.getTextContent().trim();
		    } else if (currentNode.getNodeName().equals(
			    Constants.INSTRUCTIONS)) {
			instructions = currentNode.getTextContent().trim();
		    } else if (currentNode.getNodeName().equals(
			    Constants.FURTHERINFO)) {
			furtherinfo = currentNode.getTextContent().trim();
		    } else if (currentNode.getNodeName().equals(Constants.LOGO)) {
			logo = currentNode.getTextContent().trim();
		    }
		}
	    }

	    description = getDescription(serviceDescriptionNode);

	    Property[] serviceProperties = getServiceProperties(serviceDescriptionNode);
	    // TODO: Check that all mandatory fields have been properly filled
	    // out.
	    if (title == null) {
		throw new ConfigurationException("title not set in configfile");
	    }
	    if (creator == null) {
		throw new ConfigurationException(
			"creator not set in configfile");
	    }

	    // FIXME! Is that the correct type passed on to the builder?
	    // Shouldn't
	    // it be the type of the concrete service?
	    ServiceDescription.Builder builder = new ServiceDescription.Builder(
		    title, "eu.planets_project.ifr.services.migrate.Migrate");

	    builder.author(creator);
	    builder.classname(canonicalServiceName);
	    // FIXME! I geuss that the end-point URL should be obtained
	    // dynamically to ensure that it is in sync. with the actual
	    // end-point URL.
	    builder
		    .endpoint(new URL("http://FIXME! put the correct URL here!"));
	    builder.description(description);
	    builder.identifier(identifier);
	    builder.instructions(instructions);
	    builder.version(version);
	    builder.tool(toolDescription);

	    // TODO: Publisher is dynamic and depends on the installation site.
	    // builder.serviceProvider(publisher);
	    eu.planets_project.services.datatypes.MigrationPath planetsPaths[] = getPlanetsMigrationPaths(configuration);
	    builder.paths(planetsPaths);
	    builder.inputFormats(getInputFormats(planetsPaths));

	    final List<Parameter> parameters = getUniqueParameters(planetsPaths);
	    builder.parameters(parameters);

	    builder.properties(serviceProperties);
	    if (furtherinfo != null) {
		try {
		    builder.furtherInfo(new URI(furtherinfo));
		} catch (URISyntaxException e) {
		    throw new ConfigurationException(
			    "furtherInfo not set to valid value", e);
		}
	    }
	    if (logo != null) {
		try {
		    builder.logo(new URI(logo));
		} catch (URISyntaxException e) {
		    throw new ConfigurationException(
			    "logo not set to valid value", e);
		}
	    }

	    return builder.build();
	} catch (Exception exception) {
	    throw new ConfigurationException(
		    "Failed parsing the service description element of the "
			    + "generic wrapper configuration.", exception);
	}

    }

    private Property[] getServiceProperties(Node serviceDescriptionNode)
	    throws ConfigurationException {

	Node propertyNode = null;
	try {
	    final XPath pathsXPath = xPathFactory.newXPath();
	    final NodeList propertyNodes = (NodeList) pathsXPath.evaluate(
		    "properties/property", serviceDescriptionNode,
		    XPathConstants.NODESET);

	    ArrayList<Property> properties = new ArrayList<Property>();
	    for (int propertyIdx = 0; propertyIdx < propertyNodes.getLength(); propertyIdx++) {
		propertyNode = propertyNodes.item(propertyIdx);

		final NamedNodeMap propertyAttributes = propertyNode
			.getAttributes();

		// Get the values for the mandatory attributes and sub-nodes.
		final URI propertyID = new URI(propertyAttributes.getNamedItem(
			"id").getNodeValue());
		Property.Builder propertyBuilder = new Property.Builder(
			propertyID);
		propertyBuilder.name(propertyAttributes.getNamedItem("name")
			.getNodeValue());

		propertyBuilder = addValue(propertyBuilder, propertyNode);

		// Add values from optional attributes and sub-nodes.
		try {
		    propertyBuilder.type(propertyAttributes
			    .getNamedItem("type").getNodeValue());

		    propertyBuilder.description(getDescription(propertyNode));
		} catch (Exception exception) {
		    // Ignore exceptions thrown due to missing optional
		    // attributes and sub-nodes.
		}

		properties.add(propertyBuilder.build());
	    }
	    return properties.toArray(new Property[properties.size()]);
	} catch (Exception exception) {
	    throw new ConfigurationException(String.format(
		    "Failed parsing the '%s' element of the configuration"
			    + " document.", serviceDescriptionNode
			    .getNodeName()), exception);
	}
    }

    /**
     * Add the text content of <code>&lt;value&gt;</code> in
     * <code>nodeWithValueElement</code> and the value of its optional
     * <code>&quot;unit&quot;</code> attribute to the
     * <code>propertyBuilder</code>.
     * 
     * @param propertyBuilder
     *            <code>Property.Builder</code> instance to store the retrieved
     *            data in.
     * @param nodeWithValueElement
     *            <code>Node</code> instance having a <code>&lt;value&gt;</code>
     *            sub-node.
     * @return the <code>Property.Builder</code> specified by
     *         <code>propertyBuilder</code> with the added data retrieved from
     *         <code>nodeWithValueElement</code>.
     * @throws XPathExpressionException
     *             if no <code>&lt;value&gt;</code> element is found in
     *             <code>nodeWithValueElement</code>.
     */
    private Builder addValue(Property.Builder propertyBuilder,
	    Node nodeWithValueElement) throws XPathExpressionException {
	final XPath pathsXPath = xPathFactory.newXPath();
	final Node valueNode = (Node) pathsXPath.evaluate("value",
		nodeWithValueElement, XPathConstants.NODE);

	propertyBuilder.value(valueNode.getTextContent().trim());

	NamedNodeMap valueAttributes = valueNode.getAttributes();
	propertyBuilder.unit(valueAttributes.getNamedItem("unit")
		.getNodeValue());

	return propertyBuilder;
    }

    /**
     * Return the text content of the <code>&lt;description&gt;</code> element
     * of <code>nodeWithDescriptionElement</code>.
     * 
     * @param nodeWithDescriptionElement
     *            <code>Node</code> containing a
     *            <code>&lt;description&gt;</code> (child) element.
     * @return a <code>String</code> instance containing the text content of the
     *         description element.
     * @throws XPathExpressionException
     *             if no <code>&lt;description&gt;</code> element is found in
     *             <code>nodeWithDescriptionElement</code>.
     */
    private String getDescription(Node nodeWithDescriptionElement)
	    throws XPathExpressionException {
	final XPath pathsXPath = xPathFactory.newXPath();
	final Node descriptionNode = (Node) pathsXPath.evaluate("description",
		nodeWithDescriptionElement, XPathConstants.NODE);

	return descriptionNode.getTextContent().trim();
    }

    /**
     * Collect all the unique parameters (unique by name) defined for the
     * migration paths specified by <code>planetsPaths</code> and return them in
     * a list.
     * <p/>
     * <p/>
     * The same parameter is likely to be defined for multiple paths, however,
     * if the configuration file for the generic wrapper has been properly
     * written, then all occurrences should have the same meaning, thus, only
     * the first occurrence of a parameter will be collected. For the same
     * reason, the returned parameters will not have any value to avoid
     * confusion, as the default value of a parameter may differ among the
     * various migration paths.
     * 
     * @param planetsPaths
     *            An array of <code>MigrationPath</code> instances to collect
     *            parameters from.
     * @return <code>List</code> of unique parameters declared for the paths
     *         specified by <code>planetsPaths</code>. The value of all the
     *         parameters will be <code>null</code>.
     */
    private List<Parameter> getUniqueParameters(
	    eu.planets_project.services.datatypes.MigrationPath[] planetsPaths) {

	final HashMap<String, Parameter> parameterMap = new HashMap<String, Parameter>();
	for (eu.planets_project.services.datatypes.MigrationPath migrationPath : planetsPaths) {
	    for (Parameter parameter : migrationPath.getParameters()) {

		// Collect the parameter and ignore multiple occurrences.
		// Parameters having a value will have the value stripped.
		final String parameterName = parameter.getName();
		if (!parameterMap.containsKey(parameterName)) {

		    Parameter.Builder parameterBuilder = new Parameter.Builder(
			    parameterName, null);
		    parameterMap.put(parameterName, parameterBuilder.build());
		}
	    }
	}

	return new ArrayList<Parameter>(parameterMap.values());
    }

    /**
     * Extract the unique input format <code>URI</code>s from the migration
     * paths specified by <code>planetsPaths</code>.
     * 
     * @param planetsPaths
     *            A list of planets <code>MigrationPath</code> instances to
     *            extract input format <code>URI</code>s from.
     * @return An array containing the unique input format <code>URI</code>s
     *         from the migration paths.
     */
    private URI[] getInputFormats(
	    eu.planets_project.services.datatypes.MigrationPath[] planetsPaths) {
	final Set<URI> inputFormats = new HashSet<URI>();
	for (eu.planets_project.services.datatypes.MigrationPath migrationPath : planetsPaths) {
	    inputFormats.add(migrationPath.getInputFormat());
	}
	return inputFormats.toArray(new URI[inputFormats.size()]);
    }

    private eu.planets_project.services.datatypes.MigrationPath[] getPlanetsMigrationPaths(
	    Document wrapperConfiguration) throws MigrationPathConfigException {

	final DBMigrationPathFactory migrationPathFactory = new DBMigrationPathFactory(
		wrapperConfiguration);

	final MigrationPaths migrationPaths = migrationPathFactory
		.getAllMigrationPaths();

	final Collection<MigrationPath> pathCollection = migrationPaths
		.getAllMigrationPaths();

	return convertToPlanetsPaths(pathCollection)
		.toArray(
			new eu.planets_project.services.datatypes.MigrationPath[pathCollection
				.size()]);
    }

    private Tool parseTool(Node tool) throws ConfigurationException {
	NodeList topLevelNodes = tool.getChildNodes();
	String description = null, version = null, identifier = null, name = null, homepage = null;

	for (int nodeIndex = 0; nodeIndex < topLevelNodes.getLength(); nodeIndex++) {
	    final Node currentNode = topLevelNodes.item(nodeIndex);
	    if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
		if (currentNode.getNodeName().equals(Constants.DESCRIPTION)) {
		    description = currentNode.getTextContent().trim();
		} else if (currentNode.getNodeName().equals(Constants.VERSION)) {
		    version = currentNode.getTextContent().trim();
		} else if (currentNode.getNodeName().equals(
			Constants.IDENTIFIER)) {
		    identifier = currentNode.getTextContent().trim();
		} else if (currentNode.getNodeName().equals(Constants.NAME)) {
		    name = currentNode.getTextContent().trim();
		} else if (currentNode.getNodeName().equals(Constants.HOMEPAGE)) {
		    homepage = currentNode.getTextContent().trim();
		}

	    }

	}
	try {
	    description = getDescription(tool);
	} catch (XPathExpressionException xpee) {
	    // The description is optional, thus ignore exceptions.
	}

	URL homepageURL = null;
	URI identifierURI = null;
	if (homepage != null) {
	    try {
		homepageURL = new URL(homepage);
	    } catch (MalformedURLException e) {
		throw new ConfigurationException(
			"Homepage not set to valid value", e);
	    }
	}
	if (identifier != null) {
	    try {
		identifierURI = new URI(identifier);
	    } catch (URISyntaxException e) {
		throw new ConfigurationException(
			"identifier not set to valid value", e);
	    }
	}

	Tool t = new Tool(identifierURI, name, version, description,
		homepageURL);
	return t;
    }

    /**
     * TODO: This should go into a utility class.
     * 
     * Convert a collection of generic wrapper migration paths to a PLANETS
     * <code>MigrationPath</code> instances. During this conversion any presets
     * of the generic wrapper migration paths will be converted to a PLANETS
     * parameters and a list of valid values and their descriptions will be
     * appended to the description of the (preset) parameter.
     * 
     * @param genericWrapperMigrationPaths
     *            A collection of generic wrapper <code>MigrationPath</code>
     *            instances to convert.
     * @return a <code>List</code> of
     *         <code>eu.planets_project.services.datatypes.MigrationPath</code>
     *         created from the generic wrapper migration paths.
     */
    private List<eu.planets_project.services.datatypes.MigrationPath> convertToPlanetsPaths(
	    Collection<MigrationPath> genericWrapperMigrationPaths) {

	final ArrayList<eu.planets_project.services.datatypes.MigrationPath> planetsPaths = new ArrayList<eu.planets_project.services.datatypes.MigrationPath>();
	for (MigrationPath migrationPath : genericWrapperMigrationPaths) {

	    final List<Parameter> planetsParameters = new ArrayList<Parameter>();
	    planetsParameters.addAll(migrationPath.getToolParameters());

	    // Add a parameter for each preset (category)
	    final ToolPresets toolPresets = migrationPath.getToolPresets();
	    final Collection<Preset> presets = toolPresets.getAllToolPresets();
	    for (Preset preset : presets) {

		// Create a parameter for each preset which is not assigned any
		// value (i.e. null), however, the default preset parameter will
		// be
		// assigned the name of the default preset setting.
		Parameter.Builder parameterBuilder;
		if (preset.getName().equals(toolPresets.getDefaultPresetID())) {
		    parameterBuilder = new Parameter.Builder(preset.getName(),
			    preset.getDefaultSetting().getName());
		} else {
		    parameterBuilder = new Parameter.Builder(preset.getName(),
			    null);
		}

		// Append a description of the valid values for the preset
		// parameter.
		String usageDescription = "\n\nValid values : Description\n";

		for (PresetSetting presetSetting : preset.getAllSettings()) {

		    usageDescription += "\n" + presetSetting.getName() + " : "
			    + presetSetting.getDescription();
		}

		parameterBuilder.description(preset.getDescription()
			+ usageDescription);

		planetsParameters.add(parameterBuilder.build());
	    }
	    planetsPaths
		    .add(new eu.planets_project.services.datatypes.MigrationPath(
			    migrationPath.getSourceFormat(), migrationPath
				    .getDestinationFormat(), planetsParameters));
	}

	return planetsPaths;
    }

}