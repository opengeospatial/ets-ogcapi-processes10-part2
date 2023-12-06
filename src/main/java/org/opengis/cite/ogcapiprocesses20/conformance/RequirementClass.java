package org.opengis.cite.ogcapiprocesses20.conformance;

/**
 * 
 *
 * Encapsulates all known requirement classes.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public enum RequirementClass {

    DRU( "http://www.opengis.net/spec/ogcapi-processes-2/1.0/conf/deploy-replace-undeploy" );


    private final String conformanceClass;

    private final String mediaTypeFeaturesAndCollections;

    private final String mediaTypeOtherResources;

    RequirementClass( String conformanceClass ) {
        this( conformanceClass, null, null );
    }

    RequirementClass( String conformanceClass, String mediaTypeFeaturesAndCollections, String mediaTypeOtherResources ) {
        this.conformanceClass = conformanceClass;
        this.mediaTypeFeaturesAndCollections = mediaTypeFeaturesAndCollections;
        this.mediaTypeOtherResources = mediaTypeOtherResources;
    }

    /**
     * @return <code>true</code> if the RequirementClass has a media type for features and collections,
     *         <code>true</code> otherwise
     */
    public boolean hasMediaTypeForFeaturesAndCollections() {
        return mediaTypeFeaturesAndCollections != null;
    }

    /**
     * @return media type for features and collections, <code>null</code> if not available
     */
    public String getMediaTypeFeaturesAndCollections() {
        return mediaTypeFeaturesAndCollections;
    }

    /**
     * @return <code>true</code> if the RequirementClass has a media type for other resources,
     *         <code>true</code> otherwise
     */
    public boolean hasMediaTypeForOtherResources() {
        return mediaTypeOtherResources != null;
    }

    /**
     * @return media type of other resources, <code>null</code> if not available
     */
    public String getMediaTypeOtherResources() {
        return mediaTypeOtherResources;
    }

    /**
     * @param conformanceClass
     *            the conformance class of the RequirementClass to return.
     * @return the RequirementClass with the passed conformance class, <code>null</code> if RequirementClass exists
     */
    public static RequirementClass byConformanceClass( String conformanceClass ) {
        for ( RequirementClass requirementClass : values() ) {
            if ( requirementClass.conformanceClass.equals( conformanceClass ) )
                return requirementClass;
        }
        return null;
    }

}
