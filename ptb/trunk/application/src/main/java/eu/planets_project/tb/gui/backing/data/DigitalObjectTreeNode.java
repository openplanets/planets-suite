/**
 * 
 */
package eu.planets_project.tb.gui.backing.data;

import eu.planets_project.ifr.core.common.logging.PlanetsLogger;
import eu.planets_project.services.datatypes.DigitalObject;

import java.net.URI;
import org.apache.myfaces.custom.tree2.TreeNodeBase;

/**
 * @author AnJackson
 *
 */
public class DigitalObjectTreeNode extends TreeNodeBase implements java.io.Serializable {
    static final long serialVersionUID = 82362318283823293l;
    
    static private PlanetsLogger log = PlanetsLogger.getLogger(DigitalObjectTreeNode.class);
    
    private DigitalObject dob;
    private URI uri;
    private String leafname;
    private String owner;
    private String size;
    private String dateAdded;
    private String dateModified;
    private boolean selected;
    private boolean selectable;
    private boolean expanded = false;
    
    /**
     * Constructor based on Digital Object:
     */
    public DigitalObjectTreeNode( URI uri, DigitalObject dob ) {
        log.info("Creating bean for Digital Object at: "+uri);
        this.setUri(uri);
        this.dob = dob;
        this.setType("file");
        this.setLeaf(true);
        this.setSelectable(true);
        this.size = ""+dob.getContent().length();
    }
    
    public DigitalObjectTreeNode( URI uri ) {
        this.setUri(uri);
        this.dob = null;
        this.setType("folder");
        this.setLeaf(false);
        this.setSelectable(false);
        this.size = "-";
    }

    public DigitalObjectTreeNode() {
        this.uri = null;
    }

    /** */
    private void setUri( URI uri ) {
        this.uri = uri;
        this.leafname = uri.getPath();
        if( this.leafname != null ) {
            String[] parts = this.leafname.split("/");
            if( parts != null && parts.length > 0 )
                this.leafname = parts[parts.length-1];
        }
    }
    
    /**
     * @return the dob
     */
    public DigitalObject getDob() {
        return dob;
    }

    /**
     * @return the underlying URI
     */
    public URI getUri() {
        return uri;
    }
    
    /**
     * @return the leafname
     */
    public String getLeafname() {
        return this.leafname;
    }
    
    /**
     * @return the size of the object.
     */
    public String getSize() {
        return size;
    }
    
    /**
     * @return the directory
     */
    public boolean isDirectory() {
        return (dob == null);
    }
    
    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * @return the selectable
     */
    public boolean isSelectable() {
        return selectable;
    }
    
    /**
     * @param selectable the selectable to set
     */
    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    /**
     * @return the expanded
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * @param expanded the expanded to set
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dob == null) ? 0 : dob.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DigitalObjectTreeNode other = (DigitalObjectTreeNode) obj;
        if (dob == null) {
            if (other.dob != null)
                return false;
        } else if (!dob.equals(other.dob))
            return false;
        return true;
    }
 
    
}