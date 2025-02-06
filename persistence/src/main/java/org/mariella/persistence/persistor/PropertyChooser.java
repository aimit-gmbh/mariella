package org.mariella.persistence.persistor;

import org.mariella.persistence.schema.PropertyDescription;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public interface PropertyChooser extends Serializable {
    PropertyChooser All = new AllChooser();

    boolean wants(PropertyDescription propertyDescription);

    class AllChooser implements PropertyChooser {
    	private static final long serialVersionUID = 1L;
    	
        public boolean wants(PropertyDescription propertyDescription) {
            return true;
        }
    }

    class Include implements PropertyChooser {
    	private static final long serialVersionUID = 1L;
    	
        private final Collection<String> propertyNames;

        public Include(String... propertyNames) {
            super();
            this.propertyNames = new HashSet<>(Arrays.asList(propertyNames));
        }

        public boolean wants(PropertyDescription propertyDescription) {
            return propertyNames.contains(propertyDescription.getPropertyDescriptor().getName());
        }
    }

    class Exclude implements PropertyChooser {
    	private static final long serialVersionUID = 1L;
    	
        private final Collection<String> propertyNames;

        public Exclude(String... propertyNames) {
            super();
            this.propertyNames = new HashSet<>(Arrays.asList(propertyNames));
        }

        public boolean wants(PropertyDescription propertyDescription) {
            return !propertyNames.contains(propertyDescription.getPropertyDescriptor().getName());
        }
    }
}
