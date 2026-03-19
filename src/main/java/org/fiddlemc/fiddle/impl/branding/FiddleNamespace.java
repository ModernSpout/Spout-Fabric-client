package org.fiddlemc.fiddle.impl.branding;

/**
 * Holds the {@link #FIDDLE} namespace.
 */
public final class FiddleNamespace {

    private FiddleNamespace() {
        throw new UnsupportedOperationException();
    }

    /**
      * The namespace for Fiddle namespaced keys.
     *
     * <p>
      * This is for namespaced keys that are defined by and belong to Fiddle itself,
      * not those of packs that are loaded by Fiddle (content in those packs uses its own namespaces).
     * </p>
      */
    public static final String FIDDLE = "fiddle";

}
