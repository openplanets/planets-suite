package eu.planets_project.services.datatypes;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A representation of a digital object. Instances are created using a builder
 * to allow optional named constructor parameters and ensure consistent state
 * during creation. E.g. to create a digital object with only the required
 * argument, you'd use:
 * <p/>
 * {@code DigitalObject o = new DigitalObject.Builder(content).build();}
 * <p/>
 * You can cascade additional calls for optional arguments:
 * <p/>
 * {@code DigitalObject o = new
 * DigitalObject.Builder(content).manifestationOf(abstraction
 * ).title(title).build();}
 * <p/>
 * DigitalObject instances can be serialized to XML. Given such an XML
 * representation, a digital object can be instantiated using the builder:
 * <p/>
 * {@code DigitalObject o = new DigitalObject.Builder(xml).build();}
 * <p/>
 * For usage examples, see the tests in {@link DigitalObjectTests} and web
 * service sample usage in
 * {@link eu.planets_project.ifr.core.simple.impl.PassThruMigrationService#migrate}
 * (pserv/IF/simple).
 * @author Fabian Steeg
 */
@XmlJavaTypeAdapter(DigitalObject.Adapter.class)
public interface DigitalObject {
    /** @return The title of this digital object. */
    String getTitle();

    /** @return The type of this digital object. */
    URI getFormat();

    /** @return The unique identifier. Required. */
    URL getPermanentUrl();

    /** @return The URI that this digital object is a manifestation of. */
    URI getManifestationOf();

    /** @return The checksum for this digital object. */
    Checksum getChecksum();

    /**
     * @return Additional repository-specific metadata. Returns a defensive
     *         copy, changes to the obtained list won't affect this digital
     *         object.
     */
    List<Metadata> getMetadata();

    /**
     * @return The 0..n digital objects contained in this digital object.
     *         Returns a defensive copy, changes to the obtained list won't
     *         affect this digital object.
     */
    List<DigitalObject> getContained();

    /**
     * @return The actual content references. Required. Returns a defensive
     *         copy, changes to the obtained list won't affect this digital
     *         object.
     */
    Content getContent();

    /**
     * @return The 0..n events that happened to this digital object. Returns a
     *         defensive copy, changes to the obtained list won't affect this
     *         digital object.
     */
    List<Event> getEvents();

    /**
     * @return The 0..n fragments this digital object consists of. Returns a
     *         defensive copy, changes to the obtained list won't affect this
     *         digital object.
     */
    List<Fragment> getFragments();

    /**
     * @return An XML representation of this digital object (can be used to
     *         instantiate an object using the builder)
     */
    String toXml();

    @XmlJavaTypeAdapter(DigitalObject.Content.Adapter.class)
    interface Content {
        /**
         * @return An input stream for this content; this is either created for
         *         the actual value (if this is value content) or a stream for
         *         reading the reference (if this is a reference content)
         */
        public InputStream read();

        /*
         * The current solution to be able to pass this into web service
         * methods: We tell JAXB which adapter to use for converting from the
         * interface to the implementation. While this does tightly couple the
         * interface and the implementation, it still allows us to hide the
         * implementation class, e.g. keeping it out of our web service
         * interfaces. Also, for using the IF API outside of a web service stack
         * or JAXB, i.e. as a plain Java library, this is perfectly fine.
         */
        /**
         * Adapter for serialization of DigitalObject.Content interface
         * instances.
         */
        static class Adapter
                extends
                XmlAdapter<eu.planets_project.services.datatypes.Content, DigitalObject.Content> {
            /**
             * {@inheritDoc}
             * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
             */
            public DigitalObject.Content unmarshal(
                    final eu.planets_project.services.datatypes.Content c) {
                return c;
            }

            /**
             * {@inheritDoc}
             * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
             */
            public eu.planets_project.services.datatypes.Content marshal(
                    final DigitalObject.Content c) {
                return (eu.planets_project.services.datatypes.Content) c;
            }
        }
    }

    /* Same approach as above, but for the DigitalObject itself. */
    /** Adapter for serialization of DigitalObject interface instances. */
    static class Adapter extends
            XmlAdapter<ImmutableDigitalObject, DigitalObject> {
        /**
         * {@inheritDoc}
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
         */
        public DigitalObject unmarshal(final ImmutableDigitalObject o) {
            return o;
        }

        /**
         * {@inheritDoc}
         * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
         */
        public ImmutableDigitalObject marshal(final DigitalObject o) {
            return (ImmutableDigitalObject) o;
        }
    }

    /**
     * Builder for DigitalObject instances. Using a builder ensures consistent
     * object state during creation and models optional named constructor
     * parameters.
     * @see eu.planets_project.services.datatypes.DigitalObjectTests
     */
    public static final class Builder {
        /* Required parameter: */
        private Content content;
        /* Optional parameters, initialized to default values: */
        private URL permanentUrl = null;
        private List<Event> events = new ArrayList<Event>();
        private List<Fragment> fragments = new ArrayList<Fragment>();
        private List<DigitalObject> contained = new ArrayList<DigitalObject>();
        private URI manifestationOf = null;
        private Checksum checksum = null;
        private List<Metadata> metadata = null;
        private URI format = null;
        private String title = null;

        /** @return The instance created using this builder. */
        public DigitalObject build() {
            return new ImmutableDigitalObject(this);
        }

        /**
         * Constructs an anonymous (permanentUrl == null) digital object.
         * @param content The content of the digital object.
         */
        public Builder(final Content content) {
            this.content = content;
        }

        /**
         * @param digitalObject An existing digital object to copy into an new
         *        anonymous (permanentUrl == null) digital object.
         */
        public Builder(final DigitalObject digitalObject) {
            content = digitalObject.getContent();
            contained = digitalObject.getContained();
            events = digitalObject.getEvents();
            fragments = digitalObject.getFragments();
            manifestationOf = digitalObject.getManifestationOf();
            title = digitalObject.getTitle();
            checksum = digitalObject.getChecksum();
            metadata = digitalObject.getMetadata();
            format = digitalObject.getFormat();
        }

        /**
         * Creates an builder that will build a digital object identical to the
         * given object, including the permanent URL.
         * @param digitalObjectXml An XML representation of a digital object.
         */
        public Builder(final String digitalObjectXml) {
            /*
             * Besides the adapter, this is the second place where we mention
             * the implementation class, but as before, this is behind the
             * interface.
             */
            ImmutableDigitalObject digitalObject = ImmutableDigitalObject
                    .of(digitalObjectXml);
            permanentUrl = digitalObject.getPermanentUrl();
            content = digitalObject.getContent();
            contained = digitalObject.getContained();
            events = digitalObject.getEvents();
            fragments = digitalObject.getFragments();
            manifestationOf = digitalObject.getManifestationOf();
            title = digitalObject.getTitle();
            checksum = digitalObject.getChecksum();
            metadata = digitalObject.getMetadata();
            format = digitalObject.getFormat();
        }

        /** No-arg constructor for JAXB. API clients should not use this. */
        @SuppressWarnings("unused")
        private Builder() {
        }

        /**
         * @param content The new content for the digital object to be created
         * @return The builder, for cascaded calls
         */
        public Builder content(final Content content) {
            this.content = content;
            return this;
        }

        /**
         * @param permanentUrl The globally unique locator and identifier for
         *        this digital object.
         * @return The builder, for cascaded calls
         */
        public Builder permanentUrl(final URL permanentUrl) {
            this.permanentUrl = permanentUrl;
            return this;
        }

        /**
         * @param events The events of the digital object
         * @return The builder, for cascaded calls
         */
        public Builder events(final Event... events) {
            this.events = new ArrayList<Event>(Arrays.asList(events));
            return this;
        }

        /**
         * @param fragments The fragments the digital object is made of
         * @return The builder, for cascaded calls
         */
        public Builder fragments(final Fragment... fragments) {
            this.fragments = new ArrayList<Fragment>(Arrays.asList(fragments));
            return this;
        }

        /**
         * @param contained The contained digital objects
         * @return The builder, for cascaded calls
         */
        public Builder contains(final DigitalObject... contained) {
            this.contained = new ArrayList<DigitalObject>(Arrays
                    .asList(contained));
            return this;
        }

        /**
         * @param manifestationOf What the digital object is a manifestation of
         * @return The builder, for cascaded calls
         */
        public Builder manifestationOf(final URI manifestationOf) {
            this.manifestationOf = manifestationOf;
            return this;
        }

        /**
         * @param title The title of the digital object
         * @return The builder, for cascaded calls
         */
        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        /**
         * @param checksum The digital object's checksum
         * @return The builder, for cascaded calls
         */
        public Builder checksum(final Checksum checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * @param metadata Additional metadata for the digital object
         * @return The builder, for cascaded calls
         */
        public Builder metadata(final Metadata... metadata) {
            this.metadata = new ArrayList<Metadata>(Arrays.asList(metadata));
            return this;
        }

        /**
         * @param format The type of the digital object
         * @return The builder, for cascaded calls
         */
        public Builder format(final URI format) {
            this.format = format;
            return this;
        }

        /**
         * @return The content
         * @see DigitalObject#getContent()
         */
        public Content getContent() {
            return content;
        }

        /**
         * @return The permanent URL
         * @see DigitalObject#getPermanentUrl()
         */
        public URL getPermanentUrl() {
            return permanentUrl;
        }

        /**
         * @return The events
         * @see DigitalObject#getEvents()
         */
        public List<Event> getEvents() {
            return events;
        }

        /**
         * @return The fragments
         * @see DigitalObject#getFragments()
         */
        public List<Fragment> getFragments() {
            return fragments;
        }

        /**
         * @return The contained objects
         * @see DigitalObject#getContained()
         */
        public List<DigitalObject> getContained() {
            return contained;
        }

        /**
         * @return The abstraction this object is a manifestation of
         * @see DigitalObject#getManifestationOf()
         */
        public URI getManifestationOf() {
            return manifestationOf;
        }

        /**
         * @return The checksum
         * @see DigitalObject#getChecksum()
         */
        public Checksum getChecksum() {
            return checksum;
        }

        /**
         * @return The metadata
         * @see DigitalObject#getMetadata()
         */
        public List<Metadata> getMetadata() {
            return metadata;
        }

        /**
         * @return The format
         * @see DigitalObject#getFormat()
         */
        public URI getFormat() {
            return format;
        }

        /**
         * @return The title
         * @see DigitalObject#getTitle()
         */
        public String getTitle() {
            return title;
        }
    }

    /**
     * A digital object fragment.
     */
    public static final class Fragment {
        /** The fragment ID. */
        @XmlAttribute
        private String id;

        /** No-arg constructor for JAXB. Client should not use this. */
        @SuppressWarnings("unused")
        private Fragment() {
        }

        /**
         * @param id The ID
         */
        public Fragment(final String id) {
            this.id = id;
        }

        /**
         * @return The ID
         */
        public String getId() {
            return id;
        }
    }

}