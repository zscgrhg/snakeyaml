/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package org.yaml.snakeyaml.representer;

import org.yaml.snakeyaml.serializer.Serializer;

/**
 * @see PyYAML 3.06 for more information
 */
public class SafeRepresenter extends Representer {
    public SafeRepresenter(final Serializer serializer, final char defaultStyle) {
        super(serializer, defaultStyle);
    }

    protected boolean ignoreAliases(final Object data) {
        return data == null || data instanceof String || data instanceof Boolean
                || data instanceof Integer || data instanceof Float || data instanceof Double;
    }
}
