/**
 * Copyright (c) 2008-2009 Andrey Somov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yaml.snakeyaml.representer;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.MethodProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tags;

/**
 * Represent JavaBeans
 */
public class Representer extends SafeRepresenter {
    public Representer() {
        this.representers.put(null, new RepresentJavaBean());
    }

    protected class RepresentJavaBean implements Represent {
        private final Map<Class<? extends Object>, Set<Property>> propertiesCache = new HashMap<Class<? extends Object>, Set<Property>>();

        public Node representData(Object data) {
            Set<Property> properties;
            Class<? extends Object> clazz = data.getClass();
            properties = propertiesCache.get(clazz);
            if (properties == null) {
                try {
                    properties = getProperties(clazz);
                    propertiesCache.put(clazz, properties);
                } catch (IntrospectionException e) {
                    throw new YAMLException(e);
                }
            }
            Node node = representJavaBean(properties, data);
            return node;
        }
    }

    /**
     * Tag logic:<br/>
     * - explicit root tag is set in serializer <br/>
     * - if there is a predefined class tag it is used<br/>
     * - a global tag with class name is always used as tag. The JavaBean parent
     * of the specified JavaBean may set another tag (tag:yaml.org,2002:map)
     * when the property class is the same as runtime class
     * 
     * @param properties
     *            JavaBean getters
     * @param javaBean
     *            instance for Node
     * @return Node to get serialized
     */
    protected Node representJavaBean(Set<Property> properties, Object javaBean) {
        List<NodeTuple> value = new ArrayList<NodeTuple>(properties.size());
        String tag;
        String customTag = classTags.get(javaBean.getClass());
        tag = customTag != null ? customTag : Tags.getGlobalTagForClass(javaBean.getClass());
        // flow style will be chosen by BaseRepresenter
        MappingNode node = new MappingNode(tag, value, null);
        representedObjects.put(objectToRepresent, node);
        boolean bestStyle = true;
        for (Property property : properties) {
            ScalarNode nodeKey = (ScalarNode) representData(property.getName());
            Object memberValue = property.get(javaBean);
            boolean hasAlias = false;
            if (this.representedObjects.containsKey(memberValue)) {
                // the first occurrence of the node must keep the tag
                hasAlias = true;
            }
            Node nodeValue = representData(memberValue);
            // if possible try to avoid a global tag with a class name
            if (nodeValue instanceof MappingNode && !hasAlias) {
                // the node is a map, set or JavaBean
                if (!Map.class.isAssignableFrom(memberValue.getClass())) {
                    // the node is set or JavaBean
                    if (property.getType() == memberValue.getClass()) {
                        // we do not need global tag because the property
                        // Class is the same as the runtime class
                        nodeValue.setTag(Tags.MAP);
                    }
                }
            } else if (memberValue != null && Enum.class.isAssignableFrom(memberValue.getClass())) {
                nodeValue.setTag(Tags.STR);
            }
            if (nodeValue.getNodeId() != NodeId.scalar && !hasAlias) {
                // generic collections
                checkGlobalTag(property, nodeValue, memberValue);
            }
            if (nodeKey.getStyle() != null) {
                bestStyle = false;
            }
            if (!((nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).getStyle() == null))) {
                bestStyle = false;
            }
            value.add(new NodeTuple(nodeKey, nodeValue));
        }
        if (defaultFlowStyle != null) {
            node.setFlowStyle(defaultFlowStyle);
        } else {
            node.setFlowStyle(bestStyle);
        }
        return node;
    }

    /**
     * Remove redundant global tag for a type safe (generic) collection if it is
     * the same as defined by the JavaBean property
     * 
     * @param property
     *            - JavaBean property
     * @param node
     *            - representation of the property
     * @param object
     *            - instance represented by the node
     */
    @SuppressWarnings("unchecked")
    protected void checkGlobalTag(Property property, Node node, Object object) {
        Type[] arguments = property.getActualTypeArguments();
        if (arguments != null) {
            if (node.getNodeId() == NodeId.sequence) {
                // apply map tag where class is the same
                Class<? extends Object> t = (Class<? extends Object>) arguments[0];
                SequenceNode snode = (SequenceNode) node;
                List<Object> memberList = (List<Object>) object;
                Iterator<Object> iter = memberList.iterator();
                for (Node childNode : snode.getValue()) {
                    Object member = iter.next();
                    if (t.equals(member.getClass()) && childNode.getNodeId() == NodeId.mapping) {
                        childNode.setTag(Tags.MAP);
                    }
                }
            } else if (object instanceof Set) {
                Class t = (Class) arguments[0];
                MappingNode mnode = (MappingNode) node;
                Iterator<NodeTuple> iter = mnode.getValue().iterator();
                Set set = (Set) object;
                for (Object member : set) {
                    NodeTuple tuple = iter.next();
                    if (t.equals(member.getClass())
                            && tuple.getKeyNode().getNodeId() == NodeId.mapping) {
                        tuple.getKeyNode().setTag(Tags.MAP);
                    }
                }
            } else if (node.getNodeId() == NodeId.mapping) {
                Class keyType = (Class) arguments[0];
                Class valueType = (Class) arguments[1];
                MappingNode mnode = (MappingNode) node;
                for (NodeTuple tuple : mnode.getValue()) {
                    resetTag(keyType, tuple.getKeyNode());
                    resetTag(valueType, tuple.getValueNode());
                }
            }
        }
    }

    private void resetTag(Class<? extends Object> type, Node node) {
        String tag = node.getTag();
        if (Tags.getGlobalTagForClass(type).equals(tag)) {
            if (Enum.class.isAssignableFrom(type)) {
                node.setTag(Tags.STR);
            } else {
                node.setTag(Tags.MAP);
            }
        }
    }

    protected Set<Property> getProperties(Class<? extends Object> type)
            throws IntrospectionException {
        Set<Property> properties = new TreeSet<Property>();
        // add JavaBean getters
        for (PropertyDescriptor property : Introspector.getBeanInfo(type).getPropertyDescriptors())
            if (property.getReadMethod() != null
                    && !property.getReadMethod().getName().equals("getClass")) {
                properties.add(new MethodProperty(property));
            }
        // add public fields
        for (Field field : type.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers))
                continue;
            properties.add(new FieldProperty(field));
        }
        if (properties.isEmpty()) {
            throw new YAMLException("No JavaBean properties found in " + type.getName());
        }
        return properties;
    }

}
